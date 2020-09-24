package tv.mycujoo.mls.manager

import tv.mycujoo.mls.widgets.mlstimebar.PositionedPointOfInterest

interface SeekBarListener {
    fun onSeekTo(
        position: Int,
        positionedPointOfInterestList: ArrayList<PositionedPointOfInterest>
    )
}