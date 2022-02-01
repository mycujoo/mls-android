package tv.mycujoo.mcls.api

import android.content.Context
import android.content.res.AssetManager
import android.os.Build
import com.caverock.androidsvg.SVG
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.ui.AdViewProvider
import com.google.android.exoplayer2.util.Util
import dagger.hilt.android.qualifiers.ApplicationContext
import tv.mycujoo.mcls.analytic.VideoAnalyticsCustomData
import tv.mycujoo.mcls.core.VideoPlayerMediator
import tv.mycujoo.mcls.data.IDataManager
import tv.mycujoo.mcls.enum.C.Companion.IDENTITY_TOKEN_PREF_KEY
import tv.mycujoo.mcls.enum.C.Companion.PUBLIC_KEY_PREF_KEY
import tv.mycujoo.mcls.helper.SVGAssetResolver
import tv.mycujoo.mcls.manager.IPrefManager
import tv.mycujoo.mcls.manager.contracts.IViewHandler
import tv.mycujoo.mcls.mediator.AnnotationMediator
import tv.mycujoo.mcls.player.IPlayer
import tv.mycujoo.mcls.utils.UserPreferencesUtils
import tv.mycujoo.mcls.widgets.MLSPlayerView
import javax.inject.Inject
import javax.inject.Singleton

/**
 * main component of MLS(MCLS) SDK.
 * Hosts both VideoPlayerMediator and AnnotationMediator as two main component of SDK.
 * @constructor takes MLSBuilder and returns implementation of MLSAbstract
 * @see MLSAbstract
 */
@Singleton
class MLS @Inject constructor(
    @ApplicationContext private val context: Context,
    private val videoPlayerMediator: VideoPlayerMediator,
    private val dataManager: IDataManager,
    private val viewHandler: IViewHandler,
    private val prefManager: IPrefManager,
    private val annotationMediator: AnnotationMediator,
    private val player: IPlayer,
    private val assetManager: AssetManager,
    private val userPreferencesUtils: UserPreferencesUtils,
    svgAssetResolver: SVGAssetResolver
) : MLSAbstract() {

    init {
        SVG.registerExternalFileResolver(svgAssetResolver)
    }

    /**region Fields*/
    private lateinit var playerView: MLSPlayerView

    private var mediatorInitialized = false

    private lateinit var builder: MLSBuilder
    /**endregion */

    /**
     * initialize component which are prepared by Internal builder in this class
     * for easier access from MLS
     * @param builder ready to use instance of InternalBuilder
     */
    fun initializeComponent(builder: MLSBuilder) {
        this.builder = builder
        videoPlayerMediator.videoPlayerConfig = builder.mlsConfiguration.videoPlayerConfig
        persistPublicKey(builder.publicKey)
        persistIdentityToken(builder.identityToken)

        builder.pseudoUserId?.let {
            userPreferencesUtils.setPseudoUserId(it)
        }

        builder.userId?.let {
            userPreferencesUtils.setUserId(it)
        }

        player.apply {
            create(
                builder.ima,
            )
        }

        player.getDirectInstance()?.let { exoPlayer ->
            builder.ima?.setPlayer(exoPlayer)
        }
    }

    fun setIdentityToken(identityToken: String) {
        persistIdentityToken(identityToken)
    }

    fun removeIdentityToken() {
        prefManager.delete(IDENTITY_TOKEN_PREF_KEY)
    }

    /**
     * Changes User Id Globally
     */
    fun setUserId(userId: String) {
        userPreferencesUtils.setUserId(userId)
    }

    /**
     * Delete User Id Globally
     */
    fun removeUserId() {
        userPreferencesUtils.removeUserId()
    }

    /**
     * Changes Pseudo User Id Globally
     */
    fun setCustomPseudoUserId(pseudoUserId: String) {
        userPreferencesUtils.setPseudoUserId(pseudoUserId)
    }

    /**
     * Init SVGRendering library by providing AssetManager
     * Custom font is used by registering an external fire resolver. Font are used in rendering SVG into view.
     * @param assetManager type of AssetManager that will be used to read fonts
     * @see SVGAssetResolver
     */


    /**
     * Clears playback que without releasing Exoplayer, which makes reinitilizing the player faster
     * and more reliable
     */
    fun clearQue() {
        player.clearQue()
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

    private fun initializeMediatorsIfNeeded(mMLSPlayerView: MLSPlayerView) {
        playerView = mMLSPlayerView
        viewHandler.setOverlayHost(playerView.overlayHost)
        if (mediatorInitialized) {
            mMLSPlayerView.playerView.onResume()
            player.reInit(
                ExoPlayer.Builder(context)
                    .setSeekBackIncrementMs(10000)
                    .setSeekForwardIncrementMs(10000)
                    .build()
            )
            videoPlayerMediator.initialize(mMLSPlayerView, builder)

            builder.ima?.let { ima ->
                ima.setPlayer(player.getDirectInstance()!!)
                ima.setAdViewProvider(mMLSPlayerView.playerView)
            }

            annotationMediator.initPlayerView(playerView)
            return
        }
        mediatorInitialized = true

        builder.ima?.setAdViewProvider(mMLSPlayerView.playerView as AdViewProvider)

        videoPlayerMediator.initialize(
            mMLSPlayerView,
            builder,
            emptyList(),
            cast = this.builder.cast
        )

        annotationMediator.initPlayerView(playerView)
    }

    fun setVideoAnalyticsCustomData(
        videoAnalyticsCustomData: VideoAnalyticsCustomData,
    ) {
        builder.activity?.let {
            videoPlayerMediator.setVideoAnalyticsCustomData(
                it,
                builder.getAnalyticsAccountCode(),
                videoAnalyticsCustomData
            )
        }
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
            initializeMediatorsIfNeeded(MLSPlayerView)
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
        SVG.deregisterExternalFileResolver()
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

    private fun persistIdentityToken(identityToken: String) {
        prefManager.persist(IDENTITY_TOKEN_PREF_KEY, identityToken)
    }

    /**endregion */
}