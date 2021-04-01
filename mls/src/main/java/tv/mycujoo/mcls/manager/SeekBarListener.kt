package tv.mycujoo.mcls.manager

import tv.mycujoo.mcls.widgets.mlstimebar.PositionedPointOfInterest

interface SeekBarListener {
    fun onSeekTo(
        position: Int,
        positionedPointOfInterestList: ArrayList<PositionedPointOfInterest>
    )
}