package tv.mycujoo.mcls.entity.msc

/**
 * @property primaryColor determines the color of various visual elements within the video player, e.g. the color of the controls. This should be a hexadecimal color code (e.g. #FFFFFF)
 * @property secondaryColor will be used in the future to determine the color of various visual elements, but is not used yet. This should be a hexadecimal color code (e.g. #000000)
 * @property autoPlay determines whether a video stream should start playing immediately after it is loaded into the VideoPlayer, or if it should wait for the user to press play.
 * @property showBackForwardsButtons Indicates whether the 10s backwards/forwards buttons should be shown (true) or hidden (false).
 * @property showSeekBar Indicates whether the seek-bar should be shown (true) or hidden (false).
 * @property showTimers Indicates whether the timers (elapsed timer & total timer) should be shown (true) or hidden (false).
 * @property showLiveViewers Indicates whether the number of concurrent viewers on a live stream should be shown (true) or hidden (false).
 */
data class TVVideoPlayerConfig(
    val primaryColor: String,
    val secondaryColor: String,
    val autoPlay: Boolean,
    val showBackForwardsButtons: Boolean,
    val showSeekBar: Boolean,
    val showTimers: Boolean,
    val showLiveViewers: Boolean
)