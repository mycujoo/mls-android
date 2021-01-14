package tv.mycujoo.mls.player

data class MediaData(
    val fullUrl: String,
    val dvrWindowSize: Long = Long.MAX_VALUE,
    val autoPlay: Boolean
) {

}
