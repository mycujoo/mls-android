package tv.mycujoo.mls.player

sealed class MediaDatum(open val fullUrl: String, open val dvrWindowSize: Long) {
    data class MediaData(
        override val fullUrl: String,
        override val dvrWindowSize: Long = Long.MAX_VALUE,
        val autoPlay: Boolean
    ) : MediaDatum(fullUrl, dvrWindowSize)

    data class DRMMediaData(
        override val fullUrl: String,
        override val dvrWindowSize: Long = Long.MAX_VALUE,
        val licenseUrl: String,
        val autoPlay: Boolean
    ) : MediaDatum(fullUrl, dvrWindowSize)
}
