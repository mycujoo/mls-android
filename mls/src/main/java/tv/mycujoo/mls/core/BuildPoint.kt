package tv.mycujoo.mls.core

import tv.mycujoo.mls.player.IPlayer

data class BuildPoint(
    val currentRelativePosition: Long,
    val currentAbsolutePosition: Long,
    val player: IPlayer,
    val isPlaying: Boolean,
    val isInterrupted: Boolean
)
