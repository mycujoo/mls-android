package tv.mycujoo.mls.api

import tv.mycujoo.mls.widgets.PlayerWidget

internal interface MyCujooLiveService {

    fun initializePlayer(
        playerWidget: PlayerWidget
    )

    fun releasePlayer()

    fun getPlayerController(): PlayerController
    fun getPlayerStatus(): PlayerStatus
}
