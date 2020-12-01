package tv.mycujoo.mls

import tv.mycujoo.domain.entity.*
import tv.mycujoo.mls.enum.C.Companion.ONE_SECOND_IN_MS

class TestData {
    companion object {
        fun sampleEntityWithIntroAnimation(animationType: AnimationType): OverlayEntity {
            return OverlayEntity(
                "id_0",
                null,
                ViewSpec(null, null),
                TransitionSpec(0L, animationType, ONE_SECOND_IN_MS),
                TransitionSpec(-1L, AnimationType.NONE, -1L),
                emptyList()
            )
        }

        fun sampleEntityWithOutroAnimation(animationType: AnimationType): OverlayEntity {
            return OverlayEntity(
                "id_0",
                null,
                ViewSpec(null, null),
                TransitionSpec(0L, AnimationType.NONE, 0L),
                TransitionSpec(0L, animationType, ONE_SECOND_IN_MS),
                emptyList()
            )
        }

        fun sampleHideOverlayEntity(animationType: AnimationType): HideOverlayActionEntity {
            return HideOverlayActionEntity(
                "id_0",
                null,
                animationType,
                ONE_SECOND_IN_MS
            )
        }
    }


}