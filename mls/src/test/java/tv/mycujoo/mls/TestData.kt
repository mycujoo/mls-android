package tv.mycujoo.mls

import tv.mycujoo.domain.entity.Action
import tv.mycujoo.domain.entity.AnimationType
import tv.mycujoo.domain.entity.TransitionSpec
import tv.mycujoo.domain.entity.ViewSpec
import tv.mycujoo.mls.enum.C.Companion.ONE_SECOND_IN_MS

class TestData {

    companion object {
        fun getSampleShowOverlayAction(
            offset: Long,
            absoluteTime: Long
        ): Action.ShowOverlayAction {
            val viewSpec = ViewSpec(null, null)
            val introTransitionSpec =
                TransitionSpec(0L, AnimationType.NONE, 0L)

            return Action.ShowOverlayAction(
                id = "id_1001",
                offset = 0L,
                absoluteTime = -1L,
                svgData = null,
                duration = 0L,
                viewSpec = viewSpec,
                introTransitionSpec = introTransitionSpec,
                outroTransitionSpec = null,
                placeHolders = emptyList(),
                customId = "cid_1001"
            )
        }

        fun getSampleShowOverlayAction(
            introAnimationType: AnimationType
        ): Action.ShowOverlayAction {
            val viewSpec = ViewSpec(null, null)
            val introTransitionSpec =
                TransitionSpec(0L, introAnimationType, ONE_SECOND_IN_MS)

            return Action.ShowOverlayAction(
                id = "id_1001",
                offset = 0L,
                absoluteTime = -1L,
                svgData = null,
                duration = 0L,
                viewSpec = viewSpec,
                introTransitionSpec = introTransitionSpec,
                outroTransitionSpec = null,
                placeHolders = emptyList()
            )
        }

        fun getSampleShowOverlayActionOutro(
            outroAnimationType: AnimationType
        ): Action.ShowOverlayAction {
            val viewSpec = ViewSpec(null, null)
            val introTransitionSpec =
                TransitionSpec(0L, AnimationType.NONE, 0L)
            val outroTransitionSpec =
                TransitionSpec(0L, outroAnimationType, ONE_SECOND_IN_MS)

            return Action.ShowOverlayAction(
                id = "id_1001",
                offset = 0L,
                absoluteTime = -1L,
                svgData = null,
                duration = 0L,
                viewSpec = viewSpec,
                introTransitionSpec = introTransitionSpec,
                outroTransitionSpec = outroTransitionSpec,
                placeHolders = emptyList()
            )
        }

        fun getSampleHideOverlayAction(
            animationType: AnimationType
        ): Action.HideOverlayAction {

            val outroTransitionSpec =
                TransitionSpec(0L, animationType, ONE_SECOND_IN_MS)

            return Action.HideOverlayAction(
                id = "id_1001",
                offset = 0L,
                absoluteTime = -1L,
                outroAnimationSpec = outroTransitionSpec,
                customId = "id_1001"
            )
        }

        @ExperimentalStdlibApi
        fun samplePosition(): Map<String, Double> {
            return buildMap {
                put("top", 5.toDouble())
                put("leading", 5.toDouble())
            }
        }

        fun getSampleShowOverlayAction(
            introTransitionSpec: TransitionSpec,
            outroOffset: Long
        ): Action.ShowOverlayAction {
            val viewSpec = ViewSpec(null, null)
            val outroTransitionSpec = TransitionSpec(outroOffset, AnimationType.NONE, 0L)
            return Action.ShowOverlayAction(
                id = "id_1001",
                offset = introTransitionSpec.offset,
                absoluteTime = -1L,
                svgData = null,
                duration = 0L,
                viewSpec = viewSpec,
                introTransitionSpec = introTransitionSpec,
                outroTransitionSpec = outroTransitionSpec,
                placeHolders = emptyList()
            )
        }

        fun getSampleShowOverlayAction(
            introTransitionSpec: TransitionSpec,
            outroTransitionSpec: TransitionSpec
        ): Action.ShowOverlayAction {
            val viewSpec = ViewSpec(null, null)
            return Action.ShowOverlayAction(
                id = "id_1001",
                offset = introTransitionSpec.offset,
                absoluteTime = -1L,
                svgData = null,
                duration = 0L,
                viewSpec = viewSpec,
                introTransitionSpec = introTransitionSpec,
                outroTransitionSpec = outroTransitionSpec,
                placeHolders = emptyList()
            )
        }
    }

}