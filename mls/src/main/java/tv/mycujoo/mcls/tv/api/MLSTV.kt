package tv.mycujoo.mcls.tv.api

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.caverock.androidsvg.SVG
import tv.mycujoo.mcls.api.DataProvider
import tv.mycujoo.mcls.data.IDataManager
import tv.mycujoo.mcls.enum.C
import tv.mycujoo.mcls.helper.SVGAssetResolver
import tv.mycujoo.mcls.manager.IPrefManager
import tv.mycujoo.mcls.manager.Logger
import tv.mycujoo.mcls.manager.contracts.IViewHandler
import tv.mycujoo.mcls.tv.player.TvVideoPlayer
import tv.mycujoo.mcls.utils.UserPreferencesUtils
import tv.mycujoo.ui.MLSTVFragment
import javax.inject.Inject

open class MLSTV @Inject constructor(
    private val dataManager: IDataManager,
    private val prefManager: IPrefManager,
    private val tvVideoPlayer: TvVideoPlayer,
    private val viewHandler: IViewHandler,
    private val userPreferencesUtils: UserPreferencesUtils,
    private val logger: Logger,
    svgAssetResolver: SVGAssetResolver
) : DefaultLifecycleObserver {

    lateinit var tvBuilder: MLSTvBuilder
    lateinit var mlsTvFragment: MLSTVFragment

    init {
        SVG.registerExternalFileResolver(svgAssetResolver)
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)

        viewHandler.setOverlayHost(mlsTvFragment.overlayHost)
        tvVideoPlayer.initialize(mlsTvFragment, tvBuilder)
    }

    override fun onStop(owner: LifecycleOwner) {
        tvVideoPlayer.release()
        SVG.deregisterExternalFileResolver()
        super.onStop(owner)
    }

    fun initialize(builder: MLSTvBuilder, mlsTvFragment: MLSTVFragment) {
        tvBuilder = builder
        this.logger.setLogLevel(builder.logLevel)
        this.mlsTvFragment = mlsTvFragment

        builder.pseudoUserId?.let {
            userPreferencesUtils.setPseudoUserId(it)
        }

        builder.userId?.let {
            userPreferencesUtils.setUserId(it)
        }

        persistPublicKey(builder.publicKey)

        if(builder.identityToken.isNotEmpty()) {
            persistIdentityToken(builder.identityToken)
        }

        tvVideoPlayer.mlsTVConfiguration = builder.mlsTVConfiguration
    }

    /**
     * Changes User Id Globally
     */
    fun setCustomPseudoUserId(userId: String) {
        userPreferencesUtils.setPseudoUserId(userId)
    }

    /**
     * Changes Pseudo User Id Globally
     */
    fun setUserId(pseudoUserId: String) {
        userPreferencesUtils.setUserId(pseudoUserId)
    }

    /**
     * Removes Pseudo User Id Globally
     */
    fun removeUserId() {
        userPreferencesUtils.removeUserId()
    }

    fun getVideoPlayer(): TvVideoPlayer {
        return tvVideoPlayer
    }

    fun getDataProvider(): DataProvider {
        return dataManager
    }

    fun setIdentityToken(identityToken: String) {
        persistIdentityToken(identityToken)
    }

    fun removeIdentityToken() {
        prefManager.delete(C.IDENTITY_TOKEN_PREF_KEY)
    }

    /**region msc Functions*/
    /**
     * store public key in shared-pref
     * @param publicKey user's public key
     */
    private fun persistPublicKey(publicKey: String) {
        prefManager.persist(C.PUBLIC_KEY_PREF_KEY, publicKey)
    }

    private fun persistIdentityToken(identityToken: String) {
        prefManager.persist(C.IDENTITY_TOKEN_PREF_KEY, identityToken)
    }

    /**endregion */
}