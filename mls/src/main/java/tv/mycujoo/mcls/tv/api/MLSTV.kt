package tv.mycujoo.mcls.tv.api

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import tv.mycujoo.mcls.api.DataProvider
import tv.mycujoo.mcls.data.IDataManager
import tv.mycujoo.mcls.enum.C
import tv.mycujoo.mcls.manager.IPrefManager
import tv.mycujoo.mcls.manager.contracts.IViewHandler
import tv.mycujoo.mcls.tv.player.TvVideoPlayer
import tv.mycujoo.ui.MLSTVFragment
import javax.inject.Inject

class MLSTV @Inject constructor(
    private val dataManager: IDataManager,
    private val prefManager: IPrefManager,
    private val tvVideoPlayer: TvVideoPlayer,
    private val viewHandler: IViewHandler,
) : DefaultLifecycleObserver {

    lateinit var tvBuilder: MLSTvBuilder
    lateinit var mlsTvFragment: MLSTVFragment

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)

        viewHandler.setOverlayHost(mlsTvFragment.overlayHost)
        tvVideoPlayer.initialize(mlsTvFragment, tvBuilder)
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        tvVideoPlayer.release()
    }

    fun initialize(builder: MLSTvBuilder, mlsTvFragment: MLSTVFragment) {
        tvBuilder = builder
        this.mlsTvFragment = mlsTvFragment

        persistPublicKey(builder.publicKey)

        if(builder.identityToken.isNotEmpty()) {
            persistIdentityToken(builder.identityToken)
        }

        tvVideoPlayer.mlsTVConfiguration = builder.mlsTVConfiguration
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