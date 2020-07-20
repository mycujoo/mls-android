package tv.mycujoo.domain.entity


data class Scaffold(
    var id: String,
    var svgData: SvgData?,
    var viewSpec: ViewSpec,
    var introTransitionSpec: TransitionSpec,
    var outroTransitionSpec: TransitionSpec,
    var svgString: String?,
    val keywords: ArrayList<String>
)