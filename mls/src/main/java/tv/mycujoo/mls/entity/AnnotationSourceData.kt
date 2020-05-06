package tv.mycujoo.mls.entity

data class AnnotationSourceData(val streamOffset: Long, val action: Action) {


}

abstract class Action(val id: Int)


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
class HighlightAction(
    id: Int,
    val streamOffset: Long,
    val timeLabel: String,
    val title: String,
    val streamUrl : String
) : Action(id) {

}

class TimeLineAction(
    id: Int,
    val color: String
) : Action(id) {

}

enum class LayoutType {
    BASIC_SINGLE_LINE,
    BASIC_DOUBLE_LINE,
    BASIC_SCORE_BOARD

}

enum class LayoutPosition {
    TOP_LEFT,
    TOP_RIGHT,
    BOTTOM_RIGHT,
    BOTTOM_LEFT,

}