package tv.mycujoo.mcls.core

import tv.mycujoo.mcls.player.IPlayer

data class BuildPoint(
    val currentRelativePosition: Long,
    val currentAbsolutePosition: Long,
    val player: IPlayer,
    val isPlaying: Boolean
)
