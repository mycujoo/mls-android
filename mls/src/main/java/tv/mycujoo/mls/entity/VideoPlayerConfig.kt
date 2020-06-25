package tv.mycujoo.mls.entity

data class VideoPlayerConfig(
    val primaryColor: String,
    val secondaryColor: String,
    val autoPlay: Boolean,
    val defaultVolume: Float,
    val backForwardButtons: Boolean,
    val liveViewers: Boolean,
    val eventInfoButton: Boolean
)