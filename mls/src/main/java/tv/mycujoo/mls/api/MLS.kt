package tv.mycujoo.mls.api

import android.content.Context
import android.content.res.AssetManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import com.caverock.androidsvg.SVG
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.util.Util
import kotlinx.coroutines.CoroutineScope
import okhttp3.OkHttpClient
import tv.mycujoo.domain.entity.EventEntity
import tv.mycujoo.domain.repository.EventsRepository
import tv.mycujoo.mls.core.AnnotationFactory
import tv.mycujoo.mls.core.AnnotationListener
import tv.mycujoo.mls.core.InternalBuilder
import tv.mycujoo.mls.core.VideoPlayerMediator
import tv.mycujoo.mls.data.IDataManager
import tv.mycujoo.mls.helper.DownloaderClient
import tv.mycujoo.mls.helper.SVGAssetResolver
import tv.mycujoo.mls.manager.IPrefManager
import tv.mycujoo.mls.manager.contracts.IViewHandler
import tv.mycujoo.mls.mediator.AnnotationMediator
import tv.mycujoo.mls.network.Api
import tv.mycujoo.mls.network.RemoteApi
import tv.mycujoo.mls.player.MediaFactory
import tv.mycujoo.mls.player.MediaOnLoadCompletedListener
import tv.mycujoo.mls.player.Player
import tv.mycujoo.mls.player.Player.Companion.createExoPlayer
import tv.mycujoo.mls.player.Player.Companion.createMediaFactory
import tv.mycujoo.mls.widgets.MLSPlayerView
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

    private lateinit var playerView: MLSPlayerView

    private var mediatorInitialized = false
    private lateinit var videoPlayerMediator: VideoPlayerMediator
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

        videoPlayerMediator = VideoPlayerMediator(
            builder.mlsConfiguration.videoPlayerConfig,
            viewHandler,
            internalBuilder.reactorSocket,
            internalBuilder.dispatcher,
            dataManager,
            emptyList(),
            builder.mCaster,
            internalBuilder.logger
        )

        player = Player().apply {
            val exoPlayer = createExoPlayer(context)
            create(
                MediaFactory(createMediaFactory(context), MediaItem.Builder()),
                exoPlayer,
                Handler(),
                MediaOnLoadCompletedListener(exoPlayer)
            )
        }

    }

    private fun initSvgRenderingLibrary(assetManager: AssetManager) {
        SVG.registerExternalFileResolver(
            SVGAssetResolver(assetManager)
        )
    }


    private fun initializeMediators(
        MLSPlayerView: MLSPlayerView
    ) {
        if (mediatorInitialized) {
            videoPlayerMediator.reInitialize(MLSPlayerView, builder)
            return
        }
        mediatorInitialized = true

        videoPlayerMediator.initialize(MLSPlayerView, player, builder)


        val annotationListener =
            AnnotationListener(
                MLSPlayerView,
                builder.internalBuilder.overlayViewHelper,
                DownloaderClient(okHttpClient)
            )
        val annotationFactory = AnnotationFactory(
            annotationListener,
            viewHandler.getVariableKeeper()
        )
        annotationMediator = AnnotationMediator(
            MLSPlayerView,
            annotationFactory,
            dataManager,
            dispatcher,
            videoPlayerMediator.getPlayer(),
            Executors.newScheduledThreadPool(1),
            Handler(Looper.getMainLooper()),
            builder.internalBuilder.logger
        )
        annotationMediator.initPlayerView(MLSPlayerView)

        videoPlayerMediator.setAnnotationMediator(annotationMediator)
    }

    private fun initializePlayerView(MLSPlayerView: MLSPlayerView) {
        this.playerView = MLSPlayerView
        this.viewHandler.setOverlayHost(MLSPlayerView.overlayHost)
        initializeMediators(MLSPlayerView)
        videoPlayerMediator.attachPlayer(MLSPlayerView)
    }
    /**endregion */

    /**region Over-ridden Functions*/
    override fun onStart(MLSPlayerView: MLSPlayerView) {
        if (Util.SDK_INT >= Build.VERSION_CODES.N) {
            initializePlayerView(MLSPlayerView)
        }
    }

    override fun onResume(MLSPlayerView: MLSPlayerView) {
        if (Util.SDK_INT < Build.VERSION_CODES.N) {
            initializePlayerView(MLSPlayerView)
        }
        videoPlayerMediator.onResume()
    }

    override fun onPause() {
        videoPlayerMediator.onPause()

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
        return videoPlayerMediator.videoPlayer
    }

    override fun getDataProvider(): DataProvider {
        return dataManager
    }

    /**endregion */

    private fun release() {
        videoPlayerMediator.release()
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
        playerView.showEventInformationPreEventDialog()
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