package tv.mycujoo.mls.player

data class DRMMediaData(
    val fullUrl: String,
    val dvrWindowSize: Long = Long.MAX_VALUE,
    val licenseUrl: String,
    val autoPlay: Boolean
)