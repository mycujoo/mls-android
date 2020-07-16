package tv.mycujoo.domain.usecase

import tv.mycujoo.domain.entity.ActionEntity
import tv.mycujoo.domain.entity.AnimationType
import tv.mycujoo.domain.entity.AnnotationEntity
import tv.mycujoo.domain.entity.PositionGuide
import tv.mycujoo.domain.entity.models.ActionType

class GetAnnotationUseCase {

    // todo : use real use-case abstract class instead of mocking data

    companion object {

        fun result(): AnnotationEntity {

            val offset = 7000L
            return AnnotationEntity(
                "20001",
                offset,
                "tml_1",
                listOf(
                    getShowOverLayAction(
                        "scoreboard0",
                        7000L,
                        5000L,
                        PositionGuide(left = 5F, bottom = 5F),
                        AnimationType.NONE,
                        0L
                    ),
                    getShowOverLayAction(
                        "scoreboard1",
                        7000L,
                        50000L,
                        PositionGuide(right = 5F, bottom = 5F),
                        AnimationType.NONE,
                        0L
                    ),
                    getShowOverLayAction(
                        "scoreboard2",
                        7000L,
                        5000L,
                        PositionGuide(right = 5F, top = 5F),
                        AnimationType.SLIDE_FROM_LEFT,
                        2000L
                    ),
                    getHideOverlayAction("scoreboard0", 12000L, AnimationType.FADE_OUT, 1000L),
                    getHideOverlayAction("scoreboard1", 12000L, AnimationType.NONE, 1000L),
                    getHideOverlayAction(
                        "scoreboard2",
                        12000L,
                        AnimationType.SLIDE_TO_LEFT,
                        60000L
                    )
//                    getHideOverlayAction("scoreboard5", 128000L, AnimationType.FADE_OUT)
//                    getHideOverlayAction("scoreboard2", 90000L)
                )
            )
        }


        private fun getShowOverLayAction(
            customId: String,
            offset: Long,
            duration: Long,
            positionGuide: PositionGuide,
            animationType: AnimationType,
            animationDuration: Long
        ): ActionEntity {
            return ActionEntity(
                "f43fgaf94j3ofg",
                offset,
                ActionType.SHOW_OVERLAY,
                customId,
//                "https://dev.w3.org/SVG/tools/svgweb/samples/svg-files/DroidSans.svg",
                "https://storage.googleapis.com/mycujoo-player-app.appspot.com/announcement_overlay.svg",
                null,
                positionGuide,
                Pair(30F, 0F),
                duration,
                animationType,
                animationDuration,
                AnimationType.NONE,
                -1L
            )
        }

        private fun getHideOverlayAction(
            customId: String,
            offset: Long,
            animationType: AnimationType,
            animationDuration: Long
        ): ActionEntity {
            return ActionEntity(
                "f43f9ajf9dfjSX",
                offset,
                ActionType.HIDE_OVERLAY,
                customId,
                null,
                null,
                null,
                Pair(300F, 150F),
                null,
                AnimationType.NONE,
                -1L,
                animationType,
                animationDuration
            )
        }
    }
}