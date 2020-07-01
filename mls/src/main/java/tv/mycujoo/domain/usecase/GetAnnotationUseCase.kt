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
//                    getShowOverLayAction(
//                        PositionGuide(trailing = 5F, top = 10F)
//                    ),
//                    getShowOverLayAction(
//                        PositionGuide(leading = 5F, bottom = 10F)
//                    ),
//                    getShowOverLayAction(
//                        PositionGuide(top = 5F, hCenter = 10F)
//                    ),
                    getShowOverLayAction(
                        PositionGuide(leading = 5F, vCenter = 50F)
                    ),
                    getHideOverlayAction()
                )
            )
        }


        private fun getShowOverLayAction(positionGuide: PositionGuide): ActionEntity {
            val offset = 7000L
            return ActionEntity(
                "f43fgaf94j3ofg",
                offset,
                ActionType.SHOW_OVERLAY,
                "scoreboard1",
//                "https://dev.w3.org/SVG/tools/svgweb/samples/svg-files/DroidSans.svg",
                "https://storage.googleapis.com/mycujoo-player-app.appspot.com/announcement_overlay.svg",
                null,
                positionGuide,
                Pair(30F, 0F),
                5000L,
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

        private fun getHideOverlayAction(): ActionEntity {
            return ActionEntity(
                "f43f9ajf9dfjSX",
                15000L,
                ActionType.HIDE_OVERLAY,
                "scoreboard1",
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