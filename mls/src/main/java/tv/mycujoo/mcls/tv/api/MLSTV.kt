package tv.mycujoo.mcls.tv.api

import androidx.leanback.app.VideoSupportFragment
import tv.mycujoo.mcls.api.DataProvider
import tv.mycujoo.mcls.data.IDataManager
import tv.mycujoo.mcls.enum.C
import tv.mycujoo.mcls.manager.IPrefManager
import tv.mycujoo.mcls.tv.player.TvVideoPlayer
import tv.mycujoo.ui.MLSTVFragment
import javax.inject.Inject

class MLSTV @Inject constructor(
    private val dataManager: IDataManager,
    private val prefManager: IPrefManager,
    private val tvVideoPlayer: TvVideoPlayer
) {

    fun initialize(builder: MLSTvBuilder, videoSupportFragment: MLSTVFragment) {
        persistPublicKey(builder.publicKey)

        tvVideoPlayer.mlsTVConfiguration = builder.mlsTVConfiguration

        tvVideoPlayer.initialize(videoSupportFragment)
    }


    fun getVideoPlayer(): TvVideoPlayer {
        return tvVideoPlayer
    }

    fun getDataProvider(): DataProvider {
        return dataManager
    }

    /**region msc Functions*/
    /**
     * store public key in shared-pref
     * @param publicKey user's public key
     */
    private fun persistPublicKey(publicKey: String) {
        prefManager.persist(C.PUBLIC_KEY_PREF_KEY, publicKey)
    }

    /**endregion */
}