package tv.mycujoo.mls.api

import android.net.Uri
import android.view.View
import tv.mycujoo.mls.entity.HighlightAction
import tv.mycujoo.mls.model.ConfigParams
import tv.mycujoo.mls.widgets.PlayerViewWrapper

abstract class MLSAbstract {

    abstract fun onConfigurationChanged(
        config: ConfigParams,
        decorView: View,
        actionBar: androidx.appcompat.app.ActionBar?
    )

    abstract fun onStart(playerViewWrapper: PlayerViewWrapper)
    abstract fun onResume(playerViewWrapper: PlayerViewWrapper)


    abstract fun onPause()
    abstract fun onStop()

    abstract fun getVideoPlayer(): VideoPlayer

    abstract fun getHighlightList(): List<HighlightAction>

    abstract fun loadVideo(uri: Uri)
    abstract fun playVideo(uri: Uri)
}
