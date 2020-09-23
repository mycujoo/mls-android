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
import tv.mycujoo.mls.core.AnnotationFactory
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
import tv.mycujoo.mls.widgets.MLSPlayerView
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.locks.ReentrantLock


class MLS constructor(private val builder: MLSBuilder) : MLSAbstract() {


    /**region MLS fields*/
    private lateinit var eventsRepository: EventsRepository

    private lateinit var dispatcher: CoroutineScope
    private lateinit var okHttpClient: OkHttpClient

    private lateinit var dataManager: IDataManager

    private lateinit var prefManager: IPrefManager
    private var context: Context

    private var api: Api

    private lateinit var playerView: MLSPlayerView

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
            emptyList(),
            internalBuilder.logger
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
        MLSPlayerView: MLSPlayerView
    ) {
        if (coordinatorInitialized) {
            videoPlayerCoordinator.reInitialize(MLSPlayerView)
            return
        }
        coordinatorInitialized = true

        videoPlayerCoordinator.initialize(MLSPlayerView, player, builder)


        val annotationListener = AnnotationListener(MLSPlayerView, viewHandler)
        val lock = ReentrantLock()
        val annotationFactory = AnnotationFactory(
            annotationListener,
            DownloaderClient(okHttpClient),
            viewHandler,
            lock,
            lock.newCondition()
        )
        annotationMediator = AnnotationMediator(
            MLSPlayerView,
            annotationFactory,
            dataManager,
            dispatcher,
            videoPlayerCoordinator.getPlayer(),
            Executors.newScheduledThreadPool(1),
            Handler(Looper.getMainLooper()),
            builder.internalBuilder.logger
        )
        annotationMediator.initPlayerView(MLSPlayerView)

        videoPlayerCoordinator.setAnnotationMediator(annotationMediator)
    }
    /**endregion */

    /**region Over-ridden Functions*/
    override fun onStart(MLSPlayerView: MLSPlayerView) {
        if (Util.SDK_INT >= Build.VERSION_CODES.N) {
            this.playerView = MLSPlayerView
            this.viewHandler.setOverlayHost(MLSPlayerView.overlayHost)
            initializeCoordinators(MLSPlayerView)
            videoPlayerCoordinator.attachPlayer(MLSPlayerView)
        }
    }

    override fun onResume(MLSPlayerView: MLSPlayerView) {
        if (Util.SDK_INT < Build.VERSION_CODES.N) {
            this.playerView = MLSPlayerView
            initializeCoordinators(MLSPlayerView)
            videoPlayerCoordinator.attachPlayer(MLSPlayerView)
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
        if (!this::playerView.isInitialized) {
            return
        }

        playerView.hideEventInfoButton()
        playerView.displayEventInformationPreEventDialog()
    }

    private fun setEventInfoToPlayerViewWrapper(event: EventEntity) {
        if (!this::playerView.isInitialized) {
            return
        }

        playerView.setEventInfo(event.title, event.description, event.start_time)
    }

    private fun hidePreviewMode() {
        if (!this::playerView.isInitialized) {
            return
        }
        playerView.hideEventInfoDialog()
    }
    /**endregion */
}