package tv.mycujoo.domain.entity

/**
 * SVG related data which draws Overlay background.
 * SVGData either provide an 'url' which the SVG may be downloaded from.
 * Or, the downloaded SVG as String, which is ready to be parsed in to view.
 */
data class SvgData(
    val svgUrl: String?,
    val svgString: String? = null
)