package tv.mycujoo.mcls.entity.msc

/**
 * @property primaryColor determines the color of various visual elements within the video player, e.g. the color of the controls. This should be a hexadecimal color code (e.g. #FFFFFF)
 * @property secondaryColor will be used in the future to determine the color of various visual elements, but is not used yet. This should be a hexadecimal color code (e.g. #000000)
 * @property autoPlay determines whether a video stream should start playing immediately after it is loaded into the VideoPlayer, or if it should wait for the user to press play.
 * @property enableControls Indicates whether the set of controls buttons should be shown (true) or hidden (false). This will over-ride other properties.
 * @property showPlayPauseButtons Indicates whether the play/pause buttons should be shown (true) or hidden (false).
 * @property showBackForwardsButtons Indicates whether the 10s backwards/forwards buttons should be shown (true) or hidden (false).
 * @property showSeekBar Indicates whether the seek-bar should be shown (true) or hidden (false).
 * @property showTimers Indicates whether the timers (elapsed timer & total timer) should be shown (true) or hidden (false).
 * @property showFullScreenButton Indicates whether the full-screen button should be shown (true) or hidden (false).
 * @property showLiveViewers Indicates whether the number of concurrent viewers on a live stream should be shown (true) or hidden (false).
 * @property showEventInfoButton Indicates whether the "info" button in the top-right corner of the video player should be shown (true) or hidden (false).
 */
data class VideoPlayerConfig(
    val primaryColor: String,
    val secondaryColor: String,
    val autoPlay: Boolean,
    val enableControls: Boolean,
    val showPlayPauseButtons: Boolean,
    val showBackForwardsButtons: Boolean,
    val showSeekBar: Boolean,
    val showTimers: Boolean,
    val showFullScreenButton: Boolean,
    val showLiveViewers: Boolean,
    val showEventInfoButton: Boolean
) {

    companion object{
        fun default(): VideoPlayerConfig {
            return VideoPlayerConfig(
                primaryColor = "#FFFFFF",
                secondaryColor = "#000000",
                autoPlay = true,
                enableControls = true,
                showPlayPauseButtons = true,
                showBackForwardsButtons = true,
                showSeekBar = true,
                showTimers = true,
                showFullScreenButton = false,
                showLiveViewers = true,
                showEventInfoButton = true
            )
        }
    }

    fun copy(
        primaryColor: String? = null,
        secondaryColor: String? = null,
        autoPlay: Boolean? = null,
        enableControls: Boolean? = null,
        showPlayPauseButtons: Boolean? = null,
        showBackForwardsButtons: Boolean? = null,
        showSeekBar: Boolean? = null,
        showTimers: Boolean? = null,
        showFullScreenButton: Boolean? = null,
        showLiveViewers: Boolean? = null,
        showEventInfoButton: Boolean? = null
    ): VideoPlayerConfig {

        val primaryColorOverwritten = primaryColor ?: this.primaryColor
        val secondaryColorOverwritten = secondaryColor ?: this.secondaryColor
        val autoPlayOverwritten = autoPlay ?: this.autoPlay
        val enableControlsOverwritten = enableControls ?: this.enableControls
        val showPlayPauseButtonsOverwritten = showPlayPauseButtons ?: this.showPlayPauseButtons
        val showBackForwardsButtonsOverwritten = showBackForwardsButtons ?: this.showBackForwardsButtons
        val showSeekBarOverwritten = showSeekBar ?: this.showSeekBar
        val showTimersOverwritten = showTimers ?: this.showTimers
        val showFullScreenButtonOverwritten = showFullScreenButton ?: this.showFullScreenButton
        val showLiveViewersOverwritten = showLiveViewers ?: this.showLiveViewers
        val showEventInfoButtonOverwritten = showEventInfoButton ?: this.showEventInfoButton

        return VideoPlayerConfig(
            primaryColorOverwritten,
            secondaryColorOverwritten,
            autoPlayOverwritten,
            enableControlsOverwritten,
            showPlayPauseButtonsOverwritten,
            showBackForwardsButtonsOverwritten,
            showSeekBarOverwritten,
            showTimersOverwritten,
            showFullScreenButtonOverwritten,
            showLiveViewersOverwritten,
            showEventInfoButtonOverwritten
        )
    }
}