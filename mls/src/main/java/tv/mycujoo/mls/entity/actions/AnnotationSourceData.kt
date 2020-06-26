package tv.mycujoo.mls.entity.actions

@Deprecated("Use Action instead")
abstract class Action(val id: Int)

@Deprecated("Use Action instead")
class OverLayAction(
    id: Int,
    val duration: Long,
    val layoutType: LayoutType,
    val layoutPosition: LayoutPosition = LayoutPosition.BOTTOM_LEFT,
    val sticky: Boolean,
    val firstText: String,
    val secondText: String,
    val logoUrl: String?,
    vararg val secondLineTexts: String? = emptyArray()

) : Action(id)

// stream url is not provided by api, rather than injected when entities are made
@Deprecated("Use Action instead")
class HighlightAction(
    id: Int,
    val streamOffset: Long,
    val timeLabel: String,
    val title: String,
    val streamUrl: String
) : Action(id) {

}

@Deprecated("Use Action instead")
class TimeLineAction(
    id: Int,
    val color: String,
    val text: String
) : Action(id) {

}

@Deprecated("Use Action instead")
enum class LayoutType {
    BASIC_SINGLE_LINE,
    BASIC_DOUBLE_LINE,
    BASIC_SCORE_BOARD

}

@Deprecated("Use Action instead")
enum class LayoutPosition {
    TOP_LEFT,
    TOP_RIGHT,
    BOTTOM_RIGHT,
    BOTTOM_LEFT,

}