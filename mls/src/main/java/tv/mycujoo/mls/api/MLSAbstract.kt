package tv.mycujoo.mls.api

import tv.mycujoo.mls.widgets.MLSPlayerView

abstract class MLSAbstract {

    abstract fun onStart(MLSPlayerView: MLSPlayerView)
    abstract fun onResume(MLSPlayerView: MLSPlayerView)


    abstract fun onPause()
    abstract fun onStop()

    abstract fun getVideoPlayer(): VideoPlayer
    abstract fun getDataProvider(): DataProvider
}
