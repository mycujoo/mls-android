package tv.mycujoo.mls

import tv.mycujoo.domain.entity.*

class TestData {
    companion object {
        fun sampleEntityWithIntroAnimation(animationType: AnimationType): OverlayEntity {
            return OverlayEntity(
                "id_0",
                null,
                ViewSpec(null, null),
                TransitionSpec(0L, animationType, 0L),
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
                TransitionSpec(-1L, animationType, -1L),
                emptyList()
            )
        }

        fun sampleHideOverlayEntity(animationType: AnimationType): HideOverlayActionEntity {
            return HideOverlayActionEntity(
                "id_0",
                null,
                animationType,
                1000L
            )
        }
    }


}