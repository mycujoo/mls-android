package tv.mycujoo.domain.entity

data class OverlayObject(
    var id: String,
    var svgData: SvgData?,
    var viewSpec: ViewSpec,
    var introTransitionSpec: TransitionSpec,
    var outroTransitionSpec: TransitionSpec,
    val variablePlaceHolders: Map<String, String>
) {
    fun toOverlayEntity(): OverlayEntity {
        return OverlayEntity(
            id,
            svgData,
            viewSpec,
            introTransitionSpec,
            outroTransitionSpec,
            variablePlaceHolders
        )
    }
}