package tv.mycujoo.mls.api

import tv.mycujoo.mls.widgets.PlayerViewWrapper

abstract class MLSAbstract {

    abstract fun onStart(playerViewWrapper: PlayerViewWrapper)
    abstract fun onResume(playerViewWrapper: PlayerViewWrapper)


    abstract fun onPause()
    abstract fun onStop()

    abstract fun getVideoPlayer(): VideoPlayer
    abstract fun getDataProvider(): DataProvider
}
