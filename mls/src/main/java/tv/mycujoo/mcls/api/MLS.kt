package tv.mycujoo.mcls.api

import android.content.Context
import android.content.res.AssetManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import com.caverock.androidsvg.SVG
import com.google.android.exoplayer2.ui.AdViewProvider
import com.google.android.exoplayer2.util.Util
import kotlinx.coroutines.CoroutineScope
import okhttp3.OkHttpClient
import tv.mycujoo.domain.repository.IEventsRepository
import tv.mycujoo.mcls.core.InternalBuilder
import tv.mycujoo.mcls.core.VideoPlayerMediator
import tv.mycujoo.mcls.data.IDataManager
import tv.mycujoo.mcls.enum.C.Companion.PUBLIC_KEY_PREF_KEY
import tv.mycujoo.mcls.enum.C.Companion.UUID_PREF_KEY
import tv.mycujoo.mcls.helper.SVGAssetResolver
import tv.mycujoo.mcls.helper.TypeFaceFactory
import tv.mycujoo.mcls.manager.IPrefManager
import tv.mycujoo.mcls.manager.VariableKeeper
import tv.mycujoo.mcls.manager.contracts.IViewHandler
import tv.mycujoo.mcls.mediator.AnnotationMediator
import tv.mycujoo.mcls.mediator.AnnotationMediatorFactory
import tv.mycujoo.mcls.network.Api
import tv.mycujoo.mcls.network.RemoteApi
import tv.mycujoo.mcls.player.MediaOnLoadCompletedListener
import tv.mycujoo.mcls.player.Player
import tv.mycujoo.mcls.player.Player.Companion.createExoPlayer
import tv.mycujoo.mcls.widgets.MLSPlayerView
import java.util.*
import javax.inject.Inject

/**
 * main component of MLS(MCLS) SDK.
 * Hosts both VideoPlayerMediator and AnnotationMediator as two main component of SDK.
 * @constructor takes MLSBuilder and returns implementation of MLSAbstract
 * @see MLSAbstract
 */
class MLS @Inject constructor(
    private val builder: MLSBuilder,
) : MLSAbstract() {


    /**region Fields*/
    private lateinit var eventsRepository: IEventsRepository

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
    private lateinit var variableKeeper: VariableKeeper
    /**endregion */

    /**region Initializing*/
    init {
        checkNotNull(builder.activity, { "given activity can't be null" })
        this.context = builder.activity!!

        api = RemoteApi()
    }

    /**
     * initialize component which are prepared by Internal builder in this class
     * for easier access from MLS
     * @param internalBuilder ready to use instance of InternalBuilder
     */
    fun initializeComponent(internalBuilder: InternalBuilder) {
        this.eventsRepository = internalBuilder.eventsRepository
        this.dispatcher = internalBuilder.dispatcher
        this.okHttpClient = internalBuilder.okHttpClient
        this.dataManager = internalBuilder.dataManager
        this.prefManager = internalBuilder.prefManager
        this.viewHandler = internalBuilder.viewHandler
        this.variableKeeper = internalBuilder.variableKeeper

        persistPublicKey(this.builder.publicKey)

        internalBuilder.uuid = prefManager.get(UUID_PREF_KEY) ?: UUID.randomUUID().toString()
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
            builder.mCast,
            internalBuilder.logger
        )

        player = Player().apply {
            val exoPlayer = createExoPlayer(context)
            create(
                builder.ima,
                builder.internalBuilder.mediaFactory,
                exoPlayer,
                Handler(Looper.myLooper()!!),
                MediaOnLoadCompletedListener(exoPlayer)
            )
        }
        player.getDirectInstance()?.let { exoPlayer ->
            builder.ima?.setPlayer(exoPlayer)
        }

    }

    /**
     * Init SVGRendering library by providing AssetManager
     * Custom font is used by registering an external fire resolver. Font are used in rendering SVG into view.
     * @param assetManager type of AssetManager that will be used to read fonts
     * @see SVGAssetResolver
     */
    private fun initSvgRenderingLibrary(assetManager: AssetManager) {
        SVG.registerExternalFileResolver(
            SVGAssetResolver(TypeFaceFactory(assetManager))
        )
    }

    /**
     * Init video player mediator
     * which mediate video related component and their events, i.e. view-handler to handle view transition & references.
     * @param MLSPlayerView
     */
    private fun initializeMediators(MLSPlayerView: MLSPlayerView) {
        this.playerView = MLSPlayerView
        this.viewHandler.setOverlayHost(MLSPlayerView.overlayHost)
        initializeMediatorsIfNeeded(MLSPlayerView)
        videoPlayerMediator.attachPlayer(MLSPlayerView)
    }

    private fun initializeMediatorsIfNeeded(
        MLSPlayerView: MLSPlayerView
    ) {
        if (mediatorInitialized) {
            MLSPlayerView.playerView.onResume()
            val exoPlayer = createExoPlayer(context)
            player.reInit(exoPlayer)
            videoPlayerMediator.initialize(MLSPlayerView, player, builder)

            builder.ima?.let { ima ->
                ima.setPlayer(player.getDirectInstance()!!)
                ima.setAdViewProvider(MLSPlayerView.playerView)
            }

            annotationMediator = AnnotationMediatorFactory.createAnnotationMediator(
                MLSPlayerView,
                builder.internalBuilder,
                videoPlayerMediator.getPlayer()
            )
            videoPlayerMediator.setAnnotationMediator(annotationMediator)
            return
        }
        mediatorInitialized = true

        builder.ima?.setAdViewProvider(MLSPlayerView.playerView as AdViewProvider)

        videoPlayerMediator.initialize(MLSPlayerView, player, builder)
        annotationMediator = AnnotationMediatorFactory.createAnnotationMediator(
            MLSPlayerView,
            builder.internalBuilder,
            videoPlayerMediator.getPlayer()
        )
        videoPlayerMediator.setAnnotationMediator(annotationMediator)
    }

    /**endregion */

    /**region Over-ridden Functions*/
    /**
     * Attach player to MLSPlayerView.
     * Must be called on the onStart of host component.
     * @param MLSPlayerView
     */
    override fun onStart(MLSPlayerView: MLSPlayerView) {
        if (Util.SDK_INT >= Build.VERSION_CODES.N) {
            initializeMediators(MLSPlayerView)
        }
    }

    /**
     * Attach player to MLSPlayerView.
     * Must be called on the onResume of host component.
     * @param MLSPlayerView
     */
    override fun onResume(MLSPlayerView: MLSPlayerView) {
        if (Util.SDK_INT < Build.VERSION_CODES.N) {
            initializeMediators(MLSPlayerView)
        }
        videoPlayerMediator.onResume()
    }

    /**
     * Detach player from MLSPlayerView and release resources to avoid memory leaks.
     * Must be called on the onPause of host component.
     */
    override fun onPause() {
        videoPlayerMediator.onPause()

        if (Util.SDK_INT < Build.VERSION_CODES.N) {
            release()
        }
    }

    /**
     * Detach player from MLSPlayerView and release resources to avoid memory leaks.
     * Must be called on the onStop of host component.
     */
    override fun onStop() {
        if (Util.SDK_INT >= Build.VERSION_CODES.N) {
            release()
        }
    }

    /**
     * Destroy all view-related components.
     * Must be called on the onViewDestroy of host component if applicable.
     */
    override fun onViewDestroy() {
        videoPlayerMediator.destroy()
    }

    /**
     * Destroy all non-lifecycle aware configs.
     * Must be called on the onDestroy of host component.
     */
    override fun onDestroy() {
        builder.ima?.onDestroy()
    }

    override fun getVideoPlayer(): VideoPlayer {
        return videoPlayerMediator.videoPlayer
    }

    override fun getDataProvider(): DataProvider {
        return dataManager
    }

    /**endregion */

    /**
     * internal use: release resources
     */
    private fun release() {
        builder.ima?.onStop()
        playerView.playerView.onPause()
        videoPlayerMediator.release()
        annotationMediator.release()
    }

    /**region msc Functions*/
    /**
     * store public key in shared-pref
     * @param publicKey user's public key
     */
    private fun persistPublicKey(publicKey: String) {
        prefManager.persist(PUBLIC_KEY_PREF_KEY, publicKey)
    }

    /**
     * store UUID in shared pref, if it's NOT already stored
     * @param uuid user's unique identifier
     */
    private fun persistUUIDIfNotStoredAlready(uuid: String) {
        val storedUUID = prefManager.get(UUID_PREF_KEY)
        if (storedUUID == null) {
            prefManager.persist(UUID_PREF_KEY, uuid)
        }
    }
    /**endregion */
}