package tv.mycujoo.mls.widgets

import tv.mycujoo.mls.entity.msc.VideoPlayerConfig

interface IPlayerView {

    fun config(config: VideoPlayerConfig)

    fun updateViewersCounter(count: String)
    fun hideViewersCounter()

    fun showEventInformationPreEventDialog()
    fun showEventInfoForStartedEvents()
    fun hideEventInfoDialog()

    fun showEventInfoButton()
    fun hideEventInfoButton()

    fun setLiveMode(liveState: MLSPlayerView.LiveState)

    fun continueOverlayAnimations()
    fun freezeOverlayAnimations()

    fun showBuffering()
    fun hideBuffering()
}
