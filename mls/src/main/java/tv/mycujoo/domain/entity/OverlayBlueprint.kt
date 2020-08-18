package tv.mycujoo.domain.entity

data class OverlayBlueprint(
    var id: String,
    var svgData: SvgData?,
    var viewSpec: ViewSpec,
    var introTransitionSpec: TransitionSpec,
    var outroTransitionSpec: TransitionSpec,
    val variablePlaceHolders: List<String>
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