package tv.mycujoo.mls.api

import android.view.View
import tv.mycujoo.mls.entity.HighlightAction
import tv.mycujoo.mls.model.ConfigParams
import tv.mycujoo.mls.widgets.PlayerViewWrapper
import tv.mycujoo.mls.widgets.TimeLineSeekBar

abstract class MLSInterface {

    abstract fun initializePlayer(
        playerViewWrapper: PlayerViewWrapper,
        timeLineSeekBar: TimeLineSeekBar? = null
    )

    abstract fun onConfigurationChanged(
        config: ConfigParams,
        decorView: View,
        actionBar: androidx.appcompat.app.ActionBar?
    )

    abstract fun onStart(playerViewWrapper: PlayerViewWrapper)
    abstract fun onResume(playerViewWrapper: PlayerViewWrapper)


    abstract fun onPause()
    abstract fun onStop()

    abstract fun getPlayerController(): PlayerController
    abstract fun getPlayerStatus(): PlayerStatus

    abstract fun getHighlightList(): List<HighlightAction>
}
