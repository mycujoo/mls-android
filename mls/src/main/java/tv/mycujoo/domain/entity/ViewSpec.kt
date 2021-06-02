package tv.mycujoo.domain.entity

/**
 * Specification on where an Overlay view must be displayed at.
 * @property positionGuide position guidelines relative to left, top, right, bottom of parent view
 * @property size width & height of view
 *
 * @see PositionGuide
 */
data class ViewSpec(
    val positionGuide: PositionGuide?,
    val size: Pair<Float, Float>?
)