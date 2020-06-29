package tv.mycujoo.domain.usecase

import tv.mycujoo.domain.entity.ActionEntity
import tv.mycujoo.domain.entity.AnnotationEntity
import tv.mycujoo.domain.entity.models.ActionType

class GetAnnotationUseCase {

    // todo : use real use-case abstract class instead of mocking data

    companion object {

        fun result(): AnnotationEntity {

            val offset = 10000L
            return AnnotationEntity(
                "20001",
                offset,
                "tml_1",
                listOf(getShowOverLayAction())
            )
        }


        private fun getShowOverLayAction(): ActionEntity {
            val offset = 10000L
            return ActionEntity(
                "f43fgaf94j3ofg",
                offset,
                ActionType.SHOW_OVERLAY,
                "scoreboard1",
                "https://dev.w3.org/SVG/tools/svgweb/samples/svg-files/DroidSans.svg",
                Pair(50F, -1F),
                5000L
            )
        }

        private fun getHideOverlayAction(): ActionEntity {
            return ActionEntity(
                "f43f9ajf9dfjSX",
                15000L,
                ActionType.HIDE_OVERLAY,
                "scoreboard1",
                null,
                Pair(300F, 150F),
                null
            )
        }
    }
}