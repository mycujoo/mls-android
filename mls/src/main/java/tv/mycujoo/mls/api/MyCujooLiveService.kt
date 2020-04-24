package tv.mycujoo.mls.api

import tv.mycujoo.mls.widgets.PlayerWidget
import tv.mycujoo.mls.widgets.TimeLineSeekBar

internal interface MyCujooLiveService {

    fun initializePlayer(
        playerWidget: PlayerWidget,
        timeLineSeekBar: TimeLineSeekBar? = null
    )

    fun releasePlayer()

    fun getPlayerController(): PlayerController
    fun getPlayerStatus(): PlayerStatus
//    fun getTimeBar() : TimeBar
}
