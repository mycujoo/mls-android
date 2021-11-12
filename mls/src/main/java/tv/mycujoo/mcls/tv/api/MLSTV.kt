package tv.mycujoo.mcls.tv.api

import android.app.Activity
import androidx.leanback.app.VideoSupportFragment
import kotlinx.coroutines.CoroutineScope
import okhttp3.OkHttpClient
import tv.mycujoo.mcls.api.DataProvider
import tv.mycujoo.mcls.api.MLSTVConfiguration
import tv.mycujoo.mcls.data.IDataManager
import tv.mycujoo.mcls.ima.IIma
import tv.mycujoo.mcls.manager.Logger
import tv.mycujoo.mcls.network.socket.IReactorSocket
import tv.mycujoo.mcls.player.MediaFactory
import tv.mycujoo.mcls.tv.player.TvVideoPlayer
import javax.inject.Inject

class MLSTV @Inject constructor(
    private val dataManager: IDataManager,
    private val tvVideoPlayer: TvVideoPlayer,
) {


    fun preparePlayer(videoSupportFragment: VideoSupportFragment) {

    }


    fun getVideoPlayer(): TvVideoPlayer {
        return tvVideoPlayer
    }

    fun getDataProvider(): DataProvider {
        return dataManager
    }
}