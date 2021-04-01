package tv.mycujoo.mcls.api

import tv.mycujoo.mcls.widgets.MLSPlayerView

abstract class MLSAbstract {

    abstract fun onStart(MLSPlayerView: MLSPlayerView)
    abstract fun onResume(MLSPlayerView: MLSPlayerView)


    abstract fun onPause()
    abstract fun onStop()
    abstract fun onViewDestroy()
    abstract fun onDestroy()

    abstract fun getVideoPlayer(): VideoPlayer
    abstract fun getDataProvider(): DataProvider
}
