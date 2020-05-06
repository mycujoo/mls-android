package tv.mycujoo.mls.api

import android.view.View
import tv.mycujoo.mls.entity.HighlightAction
import tv.mycujoo.mls.model.ConfigParams
import tv.mycujoo.mls.widgets.PlayerWidget
import tv.mycujoo.mls.widgets.TimeLineSeekBar

internal interface MyCujooLiveService {

    fun initializePlayer(
        playerWidget: PlayerWidget,
        timeLineSeekBar: TimeLineSeekBar? = null
    )

    fun onConfigurationChanged(
        config: ConfigParams,
        decorView: View,
        actionBar: androidx.appcompat.app.ActionBar?
    )

    fun releasePlayer()

    fun getPlayerController(): PlayerController
    fun getPlayerStatus(): PlayerStatus

    fun getHighlightList(): List<HighlightAction>
}
