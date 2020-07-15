package tv.mycujoo.domain.entity

import java.io.InputStream

data class SvgData(
    val svgUrl: String?,
    val svgInputStream: InputStream?
)