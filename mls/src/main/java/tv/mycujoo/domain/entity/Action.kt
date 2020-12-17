package tv.mycujoo.domain.entity

import tv.mycujoo.mls.model.ScreenTimerDirection
import tv.mycujoo.mls.model.ScreenTimerFormat

sealed class Action {
    /**region Abstract fields*/
    abstract val id: String
    abstract var offset: Long
    abstract var absoluteTime: Long
    abstract val priority: Int

    /**endregion */

    /**region Overlay related*/
    data class ShowOverlayAction(
        override val id: String, override var offset: Long,
        override var absoluteTime: Long,
        val svgData: SvgData? = null,
        val duration: Long? = null,
        val viewSpec: ViewSpec? = null,
        val introAnimationSpec: TransitionSpec? = null,
        val outroAnimationSpec: TransitionSpec? = null,
        val placeHolders: List<String> = emptyList()
    ) : Action() {
        override val priority: Int = 0
    }


    data class HideOverlayAction(
        override val id: String, override var offset: Long,
        override var absoluteTime: Long,
        val outroAnimationSpec: TransitionSpec? = null,
        val customId: String? = null
    ) : Action() {
        override val priority: Int = 0
    }
    /**endregion */

    /**region Timer related*/
    data class CreateTimerAction(
        override val id: String, override var offset: Long,
        override var absoluteTime: Long,
        val name: String,
        val format: ScreenTimerFormat = ScreenTimerFormat.MINUTES_SECONDS,
        val direction: ScreenTimerDirection = ScreenTimerDirection.UP,
        val startValue: Long = 0L,
        val capValue: Long
    ) : Action() {
        override val priority: Int = 1000
    }

    data class StartTimerAction(
        override val id: String, override var offset: Long,
        override var absoluteTime: Long,
        val name: String
    ) : Action() {
        override val priority: Int = 500
    }

    data class PauseTimerAction(
        override val id: String, override var offset: Long,
        override var absoluteTime: Long,
        val name: String
    ) : Action() {
        override val priority: Int = 400
    }

    data class AdjustTimerAction(
        override val id: String, override var offset: Long,
        override var absoluteTime: Long,
        val name: String,
        val value: Long
    ) : Action() {
        override val priority: Int = 300
    }

    data class SkipTimerAction(
        override val id: String, override var offset: Long,
        override var absoluteTime: Long,
        val name: String,
        val value: Long
    ) : Action() {
        override val priority: Int = 0
    }

    /**endregion */

    /**region Variable related*/

    data class CreateVariableAction(
        override val id: String, override var offset: Long,
        override var absoluteTime: Long,
        val variable: Variable
    ) : Action() {
        override val priority: Int = 1000
    }

    data class IncrementVariableAction(
        override val id: String, override var offset: Long,
        override var absoluteTime: Long,
        val name: String,
        val amount: Double
    ) : Action() {
        override val priority: Int = 0
    }
    /**endregion */

    /**region Timeline-marker related*/
    data class MarkTimelineAction(
        override val id: String, override var offset: Long,
        override var absoluteTime: Long
    ) : Action() {
        override val priority: Int = 0
    }
    /**endregion */


    /**region Other actions*/
    data class DeleteAction(
        override val id: String, override var offset: Long,
        override var absoluteTime: Long
    ) : Action() {
        override val priority: Int = 2000
    }

    data class InvalidAction(
        override val id: String, override var offset: Long,
        override var absoluteTime: Long
    ) : Action() {
        override val priority: Int = 0
    }
    /**endregion */

}
