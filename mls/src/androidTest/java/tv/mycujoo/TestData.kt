package tv.mycujoo

import tv.mycujoo.domain.entity.*
import tv.mycujoo.mls.enum.C
import tv.mycujoo.mls.helper.sampleSvgString

class TestData {
    companion object {
        fun getSampleShowOverlayAction(
        ): Action.ShowOverlayAction {
            val viewSpec = ViewSpec(PositionGuide(left = 10F, top = 10F), Pair(30F, 0F))
            val introTransitionSpec = TransitionSpec(C.ONE_SECOND_IN_MS, AnimationType.NONE, 0L)
            val outroTransitionSpec = TransitionSpec(2000L, AnimationType.NONE, 0L)
            val svgData = SvgData(null, sampleSvgString)

            return Action.ShowOverlayAction(
                id = "id_1001",
                offset = introTransitionSpec.offset,
                absoluteTime = -1L,
                svgData = svgData,
                duration = 0L,
                viewSpec = viewSpec,
                introTransitionSpec = introTransitionSpec,
                outroTransitionSpec = outroTransitionSpec,
                placeHolders = emptyList()
            )
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