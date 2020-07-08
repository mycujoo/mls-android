package tv.mycujoo.mls.api

import android.net.Uri
import tv.mycujoo.mls.model.Event
import tv.mycujoo.mls.widgets.PlayerViewWrapper

abstract class MLSAbstract {

    abstract fun onStart(playerViewWrapper: PlayerViewWrapper)
    abstract fun onResume(playerViewWrapper: PlayerViewWrapper)


    abstract fun onPause()
    abstract fun onStop()

    abstract fun getVideoPlayer(): VideoPlayer

    abstract fun loadVideo(uri: Uri)
    abstract fun playVideo(uri: Uri)
    abstract fun loadVideo(event: Event)
    abstract fun playVideo(event: Event)
}
