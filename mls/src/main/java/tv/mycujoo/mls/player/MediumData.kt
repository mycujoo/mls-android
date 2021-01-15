package tv.mycujoo.mls.player

sealed class MediumData {
    data class MediaData(
        val fullUrl: String,
        val dvrWindowSize: Long = Long.MAX_VALUE,
        val autoPlay: Boolean
    ) : MediumData()

    data class DRMMediaData(
        val fullUrl: String,
        val dvrWindowSize: Long = Long.MAX_VALUE,
        val licenseUrl: String,
        val autoPlay: Boolean
    ) : MediumData()
}
