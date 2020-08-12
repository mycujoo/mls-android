package tv.mycujoo.mls.api

import android.content.Context
import android.content.res.AssetManager
import android.os.Build
import com.caverock.androidsvg.SVG
import com.google.android.exoplayer2.util.Util
import kotlinx.coroutines.CoroutineScope
import okhttp3.OkHttpClient
import tv.mycujoo.domain.entity.EventEntity
import tv.mycujoo.domain.repository.EventsRepository
import tv.mycujoo.domain.usecase.GetActionsFromJSONUseCase
import tv.mycujoo.mls.cordinator.Coordinator
import tv.mycujoo.mls.core.InternalBuilder
import tv.mycujoo.mls.core.VideoPlayerCoordinator
import tv.mycujoo.mls.data.DataHolder
import tv.mycujoo.mls.helper.SVGAssetResolver
import tv.mycujoo.mls.manager.IPrefManager
import tv.mycujoo.mls.manager.ViewIdentifierManager
import tv.mycujoo.mls.network.Api
import tv.mycujoo.mls.network.RemoteApi
import tv.mycujoo.mls.widgets.PlayerViewWrapper


class MLS constructor(private val builder: MLSBuilder) : MLSAbstract() {


    /**region MLS fields*/
    private lateinit var eventsRepository: EventsRepository

    private lateinit var dispatcher: CoroutineScope
    private lateinit var okHttpClient: OkHttpClient

    private lateinit var dataProvider: DataProviderImpl

    private lateinit var prefManager: IPrefManager
    private var context: Context

    private var api: Api

    private lateinit var playerViewWrapper: PlayerViewWrapper

    private lateinit var videoPlayerCoordinator: VideoPlayerCoordinator
    private lateinit var coordinator: Coordinator

    private val dataHolder = DataHolder()
    private lateinit var viewIdentifierManager: ViewIdentifierManager
    /**endregion */

    /**region Initializing*/
    init {
        checkNotNull(builder.activity)
        this.dataHolder
        this.context = builder.activity!!

        api = RemoteApi()

    }

    fun initialize(internalBuilder: InternalBuilder) {
        this.eventsRepository = internalBuilder.eventsRepository
        this.dispatcher = internalBuilder.dispatcher
        this.okHttpClient = internalBuilder.okHttpClient
        this.dataProvider = internalBuilder.dataProvider
        this.prefManager = internalBuilder.prefManager
        this.viewIdentifierManager = internalBuilder.viewIdentifierManager

        persistPublicKey(this.builder.publicKey)
        initSvgRenderingLibrary(internalBuilder.getAssetManager())

        videoPlayerCoordinator = VideoPlayerCoordinator(
            builder.mlsConfiguration.VideoPlayerConfig,
            viewIdentifierManager,
            internalBuilder.dispatcher,
            internalBuilder.eventsRepository,
            dataHolder,
            GetActionsFromJSONUseCase.mappedActionCollections().timelineMarkerActionList
        )
    }

    private fun initSvgRenderingLibrary(assetManager: AssetManager) {
        SVG.registerExternalFileResolver(
            SVGAssetResolver(assetManager)
        )
    }


    private fun initializeCoordinators(
        playerViewWrapper: PlayerViewWrapper
    ) {


        videoPlayerCoordinator.initialize(playerViewWrapper, builder)
        coordinator = Coordinator(viewIdentifierManager, videoPlayerCoordinator.getPlayer()!!, okHttpClient)
        coordinator.initPlayerView(playerViewWrapper)


    }
    /**endregion */

    /**region Over-ridden Functions*/
    override fun onStart(playerViewWrapper: PlayerViewWrapper) {
        if (Util.SDK_INT >= Build.VERSION_CODES.N) {
            this.playerViewWrapper = playerViewWrapper
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

//    override fun loadVideo(event: EventEntity) {
//        event.streams.firstOrNull()?.fullUrl?.let {
//            playVideo(Uri.parse(it), false)
//        } ?: displayPreviewModeWithEventInfo(event)
//
//        setEventInfoToPlayerViewWrapper(event)
//    }
//
//    override fun playVideo(event: EventEntity) {
//        event.streams.firstOrNull()?.fullUrl?.let {
//            playVideo(Uri.parse(it), true)
//        } ?: displayPreviewModeWithEventInfo(event)
//    }

    override fun getDataProvider(): DataProvider {
        return dataProvider
    }

    /**endregion */

    private fun release() {
        videoPlayerCoordinator.release()
        coordinator.release()
    }

    /**region msc Functions*/
    private fun persistPublicKey(publicKey: String) {
        prefManager.persist("PUBLIC_KEY", publicKey)
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