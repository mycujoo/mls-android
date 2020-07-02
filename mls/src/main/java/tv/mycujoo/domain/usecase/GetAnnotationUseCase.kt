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
                        "scoreboard1",
                        7000L,
                        10000L,
                        PositionGuide(leading = 5F, vCenter = 50F)
                    ),
                    getShowOverLayAction(
                        "scoreboard2",
                        50000L,
                        40000L,
                        PositionGuide(leading = 5F, vCenter = 50F)
                    ),
                    getHideOverlayAction("scoreboard1", 15000L),
                    getHideOverlayAction("scoreboard2", 90000L)
                )
            )
        }


        private fun getShowOverLayAction(
            customId: String,
            offset: Long,
            duration: Long,
            positionGuide: PositionGuide
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
                AnimationType.FADE_IN,
                1000L
            )
        }

        private fun getShowOverLayAction(offset: Long): ActionEntity {
            return ActionEntity(
                "f43fgaf94j3ofg",
                offset,
                ActionType.SHOW_OVERLAY,
                "scoreboard1",
//                "https://dev.w3.org/SVG/tools/svgweb/samples/svg-files/DroidSans.svg",
                "https://storage.googleapis.com/mycujoo-player-app.appspot.com/announcement_overlay.svg",
                null,
                PositionGuide(trailing = 10F),
                Pair(0F, 40F),
                5000L,
                AnimationType.FADE_IN,
                1000L
            )
        }

        private fun getHideOverlayAction(customId: String, offset: Long): ActionEntity {
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
                AnimationType.FADE_OUT,
                1000L
            )
        }
    }
}