package tv.mycujoo.mls.tv.api

import android.app.Activity
import androidx.leanback.app.VideoSupportFragment
import kotlinx.coroutines.CoroutineScope
import tv.mycujoo.mls.api.DataProvider
import tv.mycujoo.mls.data.IDataManager
import tv.mycujoo.mls.network.socket.IReactorSocket
import tv.mycujoo.mls.tv.player.TvVideoPlayer

class MLSTV(
    val activity: Activity,
    private val reactorSocket: IReactorSocket,
    private val dispatcher: CoroutineScope,
    private val dataManager: IDataManager
) {

    private lateinit var tvVideoPlayer: TvVideoPlayer


    fun preparePlayer(videoSupportFragment: VideoSupportFragment) {
        tvVideoPlayer = TvVideoPlayer(activity, videoSupportFragment, reactorSocket, dispatcher, dataManager)
    }


    fun getVideoPlayer(): TvVideoPlayer {
        return tvVideoPlayer
    }

    fun getDataProvider(): DataProvider {
        return dataManager
    }
}