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

    /**region Abstract functions*/
    abstract fun updateOffset(newOffset: Long): Action
    /**endregion */

    /**region Overlay related*/
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
        val customId: String? = null
    ) : Action() {
        override val priority: Int = 0


        override fun updateOffset(newOffset: Long): ShowOverlayAction {
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

    }


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

    data class InvalidAction(
        override val id: String,
        override var offset: Long,
        override var absoluteTime: Long
    ) : Action() {
        override val priority: Int = 0
        override fun updateOffset(newOffset: Long): InvalidAction {
            return InvalidAction(id = id, offset = newOffset, absoluteTime = absoluteTime)
        }
    }
    /**endregion */
}
