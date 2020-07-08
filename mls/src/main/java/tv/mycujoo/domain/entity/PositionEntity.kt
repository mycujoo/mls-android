package tv.mycujoo.domain.entity

/**
 * Guides on how to position an Overlay Action on scree.
 * @property leading percentage distance from Left/Start of screen
 * @property trailing percentage distance from Right/End of screen
 * @property top percentage distance from top of screen
 * @property bottom percentage distance from bottom of screen
 * @property vCenter percentage to move center of this view anchoring to center of parent either to left of right
 * ranges from -50 to 50
 * @property hCenter percentage to move center of this view anchoring to center of parent either to top of bottom
 * ranges from -50 to 50
 */
data class PositionGuide(
    var leading: Float? = null,
    var trailing: Float? = null,
    var top: Float? = null,
    var bottom: Float? = null,
    var vCenter: Float? = null,
    var hCenter: Float? = null
)