package tv.mycujoo.mls.api

import android.content.Context
import android.content.res.AssetManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import com.caverock.androidsvg.SVG
import com.google.android.exoplayer2.util.Util
import kotlinx.coroutines.CoroutineScope
import okhttp3.OkHttpClient
import tv.mycujoo.domain.entity.EventEntity
import tv.mycujoo.domain.repository.EventsRepository
import tv.mycujoo.mls.core.ActionBuilder
import tv.mycujoo.mls.core.AnnotationListener
import tv.mycujoo.mls.core.InternalBuilder
import tv.mycujoo.mls.core.VideoPlayerCoordinator
import tv.mycujoo.mls.data.IDataManager
import tv.mycujoo.mls.helper.DownloaderClient
import tv.mycujoo.mls.helper.SVGAssetResolver
import tv.mycujoo.mls.manager.IPrefManager
import tv.mycujoo.mls.manager.contracts.IViewHandler
import tv.mycujoo.mls.mediator.AnnotationMediator
import tv.mycujoo.mls.network.Api
import tv.mycujoo.mls.network.RemoteApi
import tv.mycujoo.mls.player.Player
import tv.mycujoo.mls.player.Player.Companion.createExoPlayer
import tv.mycujoo.mls.player.Player.Companion.createMediaFactory
import tv.mycujoo.mls.widgets.PlayerViewWrapper
import java.util.*
import java.util.concurrent.Executors


class MLS constructor(private val builder: MLSBuilder) : MLSAbstract() {


    /**region MLS fields*/
    private lateinit var eventsRepository: EventsRepository

    private lateinit var dispatcher: CoroutineScope
    private lateinit var okHttpClient: OkHttpClient

    private lateinit var dataManager: IDataManager

    private lateinit var prefManager: IPrefManager
    private var context: Context

    private var api: Api

    private lateinit var playerViewWrapper: PlayerViewWrapper

    private var coordinatorInitialized = false
    private lateinit var videoPlayerCoordinator: VideoPlayerCoordinator
    private lateinit var annotationMediator: AnnotationMediator
    private lateinit var player: Player

    private lateinit var viewHandler: IViewHandler
    /**endregion */

    /**region Initializing*/
    init {
        checkNotNull(builder.activity)
        this.context = builder.activity!!

        api = RemoteApi()

    }

    fun initialize(internalBuilder: InternalBuilder) {
        this.eventsRepository = internalBuilder.eventsRepository
        this.dispatcher = internalBuilder.dispatcher
        this.okHttpClient = internalBuilder.okHttpClient
        this.dataManager = internalBuilder.dataManager
        this.prefManager = internalBuilder.prefManager
        this.viewHandler = internalBuilder.viewHandler

        persistPublicKey(this.builder.publicKey)

        internalBuilder.uuid = prefManager.get("UUID") ?: UUID.randomUUID().toString()
        persistUUIDIfNotStoredAlready(internalBuilder.uuid!!)
        internalBuilder.reactorSocket.setUUID(internalBuilder.uuid!!)

        initSvgRenderingLibrary(internalBuilder.getAssetManager())

        videoPlayerCoordinator = VideoPlayerCoordinator(
            builder.mlsConfiguration.videoPlayerConfig,
            viewHandler,
            internalBuilder.reactorSocket,
            internalBuilder.dispatcher,
            dataManager,
            emptyList()
        )

        player = Player().apply {
            create(
                createMediaFactory(context),
                createExoPlayer(context)
            )
        }
    }

    private fun initSvgRenderingLibrary(assetManager: AssetManager) {
        SVG.registerExternalFileResolver(
            SVGAssetResolver(assetManager)
        )
    }


    private fun initializeCoordinators(
        playerViewWrapper: PlayerViewWrapper
    ) {
        if (coordinatorInitialized) {
            videoPlayerCoordinator.reInitialize(playerViewWrapper)
            return
        }
        coordinatorInitialized = true

        videoPlayerCoordinator.initialize(playerViewWrapper, player, builder)


        val annotationListener = AnnotationListener(playerViewWrapper, viewHandler)
        val actionBuilder = ActionBuilder(
            annotationListener,
            DownloaderClient(okHttpClient),
            viewHandler
        )
        annotationMediator = AnnotationMediator(
            playerViewWrapper,
            actionBuilder,
            videoPlayerCoordinator.getPlayer(),
            Executors.newScheduledThreadPool(1),
            Handler(Looper.getMainLooper())
        )
        annotationMediator.initPlayerView(playerViewWrapper)
    }
    /**endregion */

    /**region Over-ridden Functions*/
    override fun onStart(playerViewWrapper: PlayerViewWrapper) {
        if (Util.SDK_INT >= Build.VERSION_CODES.N) {
            this.playerViewWrapper = playerViewWrapper
            this.viewHandler.setOverlayHost(playerViewWrapper.overlayHost)
            initializeCoordinators(playerViewWrapper)
            videoPlayerCoordinator.attachPlayer(playerViewWrapper)
        }
    }

    override fun onResume(playerViewWrapper: PlayerViewWrapper) {
        if (Util.SDK_INT < Build.VERSION_CODES.N) {
            this.playerViewWrapper = playerViewWrapper
            initializeCoordinators(playerViewWrapper)
            videoPlayerCoordinator.attachPlayer(playerViewWrapper)
        }
    }

    override fun onPause() {
        if (Util.SDK_INT < Build.VERSION_CODES.N) {
            release()
        }
    }

    override fun onStop() {
        if (Util.SDK_INT >= Build.VERSION_CODES.N) {
            release()
        }
    }

    override fun getVideoPlayer(): VideoPlayer {
        return videoPlayerCoordinator.videoPlayer
    }

    override fun getDataProvider(): DataProvider {
        return dataManager
    }

    /**endregion */

    private fun release() {
        videoPlayerCoordinator.release()
        annotationMediator.release()
    }

    /**region msc Functions*/
    private fun persistPublicKey(publicKey: String) {
        prefManager.persist("PUBLIC_KEY", publicKey)
    }

    private fun persistUUIDIfNotStoredAlready(uuid: String) {
        val storedUUID = prefManager.get("UUID")
        if (storedUUID == null) {
            prefManager.persist("UUID", uuid)
        }
    }

    private fun displayPreviewModeWithEventInfo(event: EventEntity) {
        if (!this::playerViewWrapper.isInitialized) {
            return
        }

        playerViewWrapper.hideEventInfoButton()
        playerViewWrapper.displayEventInformationPreEventDialog()
    }

    private fun setEventInfoToPlayerViewWrapper(event: EventEntity) {
        if (!this::playerViewWrapper.isInitialized) {
            return
        }

        playerViewWrapper.setEventInfo(event.title, event.description, event.start_time)
    }

    private fun hidePreviewMode() {
        if (!this::playerViewWrapper.isInitialized) {
            return
        }
        playerViewWrapper.hideEventInfoDialog()
    }
    /**endregion */
}