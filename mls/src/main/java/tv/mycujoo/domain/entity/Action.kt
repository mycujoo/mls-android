package tv.mycujoo.domain.entity

import tv.mycujoo.mcls.enum.C
import tv.mycujoo.mcls.model.ScreenTimerDirection
import tv.mycujoo.mcls.model.ScreenTimerFormat
import java.util.*

/**
 * Annotation Action base sealed class.
 * All annotation action will be of this type.
 * @property id identifier for action
 * @property offset the offset that action should happen at
 * @property absoluteTime the absolute time that action should happen at
 * @property priority priority of the action, compared to other actions
 */
sealed class Action {
    /**region Abstract fields*/
    abstract val id: String
    abstract var offset: Long
    abstract var absoluteTime: Long
    abstract val priority: Int
    /**endregion */

    /**region Abstract functions*/
    abstract fun updateOffset(newOffset: Long): Action

    /**
     * return eligibility of action at given offset.
     * Defaults to true
     */
    open fun isEligible(): Boolean {
        return true
    }

    /**endregion */

    /**
     * Determines if an action belongs to this moment or is within this moment and next 1000ms
     */
    fun isTillNowOrInRange(currentTime: Long): Boolean {
        return currentTime + C.ONE_SECOND_IN_MS > offset
    }


    /**region Overlay related*/
    /**
     * An Annotation action which requires SDK to show an overlay.
     * @property svgData data needed to parse SVG into overlay
     * @property duration duration of current action
     * @property viewSpec position and size of required overlay
     * @property introTransitionSpec intro transition that should be displayed while bringing the overlay into screen
     * @property outroTransitionSpec outro transition that should be displayed while moving the overlay out of screen
     * @property placeHolders variable placeholder in this overlay
     * @property customId custom identifier of overlay
     */
    data class ShowOverlayAction(
        override val id: String,
        override var offset: Long,
        override var absoluteTime: Long,
        val svgData: SvgData? = null,
        val duration: Long? = null,
        val viewSpec: ViewSpec? = null,
        val introTransitionSpec: TransitionSpec? = null,
        val outroTransitionSpec: TransitionSpec? = null,
        val placeHolders: List<String> = emptyList(),
        val customId: String = UUID.randomUUID().toString()
    ) : Action() {
        override val priority: Int = 0

        override fun updateOffset(newOffset: Long): ShowOverlayAction {
            if (offset == -1L) {
                return this
            }
            var newIntroTransitionSpec: TransitionSpec? = null
            var newOutroTransitionSpec: TransitionSpec? = null

            introTransitionSpec?.let {
                newIntroTransitionSpec =
                    TransitionSpec(newOffset, it.animationType, it.animationDuration)
            }
            outroTransitionSpec?.let {
                newOutroTransitionSpec =
                    TransitionSpec(newOffset, it.animationType, it.animationDuration)
            }
            return ShowOverlayAction(
                id = id,
                offset = newOffset,
                absoluteTime = absoluteTime,
                svgData = svgData,
                duration = duration,
                viewSpec = viewSpec,
                introTransitionSpec = newIntroTransitionSpec,
                outroTransitionSpec = newOutroTransitionSpec,
                placeHolders = placeHolders,
                customId = customId
            )
        }

        override fun isEligible(): Boolean {
            return !(offset < 0L && (duration != null || outroTransitionSpec != null))
        }
    }

    /**
     * An Annotation action which requires SDK to remove an existing overlay.
     * @property outroTransitionSpec outro transition that should be displayed while moving the overlay out of screen
     * @property customId custom identifier of overlay
     */
    data class HideOverlayAction(
        override val id: String,
        override var offset: Long,
        override var absoluteTime: Long,
        val outroTransitionSpec: TransitionSpec? = null,
        val customId: String
    ) : Action() {
        override val priority: Int = 0

        override fun updateOffset(newOffset: Long): HideOverlayAction {
            var newOutroTransitionSpec: TransitionSpec? = null
            outroTransitionSpec?.let {
                newOutroTransitionSpec =
                    TransitionSpec(newOffset, it.animationType, it.animationDuration)
            }
            return HideOverlayAction(
                id = id,
                offset = newOffset,
                absoluteTime = absoluteTime,
                outroTransitionSpec = newOutroTransitionSpec,
                customId = customId
            )
        }
    }

    /**
     * An Annotation action which requires SDK to Re-Show an overlay.
     * @property customId custom identifier of overlay
     */
    data class ReshowOverlayAction(
        override val id: String,
        override var offset: Long,
        override var absoluteTime: Long,
        val customId: String
    ) : Action() {
        override val priority: Int = 0

        override fun updateOffset(newOffset: Long): ReshowOverlayAction {
            return ReshowOverlayAction(
                id = id,
                offset = newOffset,
                absoluteTime = absoluteTime,
                customId = customId
            )
        }
    }
    /**endregion */

    /**region Timer related*/
    /**
     * Action to create a Timer
     * @property name name of timer
     * @property format format of time, MM:SS or just SS
     * @property direction time is decreased or increased
     * @property startValue start value of timer
     * @property capValue max value that timer considers valid
     * @see ScreenTimerFormat
     * @see ScreenTimerDirection
     */
    data class CreateTimerAction(
        override val id: String,
        override var offset: Long,
        override var absoluteTime: Long,
        val name: String,
        val format: ScreenTimerFormat = ScreenTimerFormat.MINUTES_SECONDS,
        val direction: ScreenTimerDirection = ScreenTimerDirection.UP,
        val startValue: Long = 0L,
        val capValue: Long
    ) : Action() {
        override val priority: Int = 1000

        override fun updateOffset(newOffset: Long): CreateTimerAction {
            return CreateTimerAction(
                id = id,
                offset = newOffset,
                absoluteTime = absoluteTime,
                name = name,
                format = format,
                direction = direction,
                startValue = startValue,
                capValue = capValue
            )
        }
    }

    /**
     * Action to start a Timer
     * @property name name of the timer that should be started
     */
    data class StartTimerAction(
        override val id: String,
        override var offset: Long,
        override var absoluteTime: Long,
        val name: String
    ) : Action() {
        override val priority: Int = 500

        override fun updateOffset(newOffset: Long): StartTimerAction {
            return StartTimerAction(
                id = id,
                offset = newOffset,
                absoluteTime = absoluteTime,
                name = name
            )
        }
    }

    /**
     * Action to pause a Timer
     * @property name name of the timer that should be started
     */
    data class PauseTimerAction(
        override val id: String,
        override var offset: Long,
        override var absoluteTime: Long,
        val name: String
    ) : Action() {
        override val priority: Int = 400

        override fun updateOffset(newOffset: Long): PauseTimerAction {
            return PauseTimerAction(
                id = id,
                offset = newOffset,
                absoluteTime = absoluteTime,
                name = name
            )
        }
    }

    /**
     * Action to adjust value of a Timer
     * @property name name of the timer that should be started
     * @property value amount of adjustment
     */
    data class AdjustTimerAction(
        override val id: String,
        override var offset: Long,
        override var absoluteTime: Long,
        val name: String,
        val value: Long
    ) : Action() {
        override val priority: Int = 300

        override fun updateOffset(newOffset: Long): AdjustTimerAction {
            return AdjustTimerAction(
                id = id,
                offset = newOffset,
                absoluteTime = absoluteTime,
                name = name,
                value = value
            )
        }
    }

    /**
     * Action to skip value of a Timer
     * @property name name of the timer that should be started
     * @property value amount of skip
     */
    data class SkipTimerAction(
        override val id: String,
        override var offset: Long,
        override var absoluteTime: Long,
        val name: String,
        val value: Long
    ) : Action() {
        override val priority: Int = 0
        override fun updateOffset(newOffset: Long): SkipTimerAction {
            return SkipTimerAction(
                id = id,
                offset = newOffset,
                absoluteTime = absoluteTime,
                name = name,
                value = value
            )
        }
    }

    /**endregion */

    /**region Variable related*/
    /**
     * Action to create a variable to be used in overlays
     * @property variable
     */
    data class CreateVariableAction(
        override val id: String,
        override var offset: Long,
        override var absoluteTime: Long,
        val variable: Variable
    ) : Action() {
        override val priority: Int = 1000
        override fun updateOffset(newOffset: Long): CreateVariableAction {
            return CreateVariableAction(
                id = id,
                offset = newOffset,
                absoluteTime = absoluteTime,
                variable = variable
            )
        }
    }

    /**
     * Action to increase a variable's value
     * @property name name of target variable
     * @property amount amount to increase
     */
    data class IncrementVariableAction(
        override val id: String,
        override var offset: Long,
        override var absoluteTime: Long,
        val name: String,
        val amount: Double
    ) : Action() {
        override val priority: Int = 0
        override fun updateOffset(newOffset: Long): IncrementVariableAction {
            return IncrementVariableAction(
                id = id,
                offset = newOffset,
                absoluteTime = absoluteTime,
                name = name,
                amount = amount
            )
        }
    }
    /**endregion */

    /**region Timeline-marker related*/
    /**
     * Action to mark a point in time-line as indicator of an event
     * @property seekOffset possible seeking offset
     * @property label label on top of marker
     * @property color color of marker
     */
    data class MarkTimelineAction(
        override val id: String,
        override var offset: Long,
        override var absoluteTime: Long,
        val seekOffset: Long,
        val label: String,
        val color: String
    ) : Action() {
        override val priority: Int = 0
        override fun updateOffset(newOffset: Long): MarkTimelineAction {
            if (offset == -1L) {
                return this
            }
            return MarkTimelineAction(
                id = id,
                offset = newOffset,
                absoluteTime = absoluteTime,
                seekOffset = seekOffset,
                label = label,
                color = color
            )
        }
    }
    /**endregion */

    /**region Other actions*/
    /**
     * Action which delete(ignore) another action
     * @param targetActionId target action id that should be deleted
     */
    data class DeleteAction(
        override val id: String,
        override var offset: Long,
        override var absoluteTime: Long,
        val targetActionId: String
    ) : Action() {
        override val priority: Int = 2000
        override fun updateOffset(newOffset: Long): DeleteAction {
            return DeleteAction(
                id = id,
                offset = newOffset,
                absoluteTime = absoluteTime,
                targetActionId = targetActionId
            )
        }
    }

    /**
     * Any action that is not in correct format, and must ignored will be an Invalid action
     */
    data class InvalidAction(
        override val id: String,
        override var offset: Long,
        override var absoluteTime: Long
    ) : Action() {
        override val priority: Int = 0
        override fun updateOffset(newOffset: Long): InvalidAction {
            if (offset == -1L) {
                return this
            }
            return InvalidAction(id = id, offset = newOffset, absoluteTime = absoluteTime)
        }
    }
    /**endregion */
}
