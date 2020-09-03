package tv.mycujoo.mls.tv.api

import android.app.Activity
import androidx.leanback.app.VideoSupportFragment
import tv.mycujoo.mls.api.DataProvider
import tv.mycujoo.mls.data.IDataManager
import tv.mycujoo.mls.manager.IPrefManager
import tv.mycujoo.mls.tv.player.TvVideoPlayer

class MLSTV(val activity: Activity, private val prefManager: IPrefManager, val dataManager: IDataManager) {

    private lateinit var tvVideoPlayer: TvVideoPlayer


    fun preparePlayer(videoSupportFragment: VideoSupportFragment) {
        tvVideoPlayer = TvVideoPlayer(activity, videoSupportFragment)
    }


    fun getVideoPlayer(): TvVideoPlayer {
        return tvVideoPlayer
    }

    fun getDataProvider(): DataProvider {
        return dataManager
    }

    private fun persistPublicKey(publicKey: String) {
        prefManager.persist("PUBLIC_KEY", publicKey)
    }

}