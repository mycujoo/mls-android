package tv.mycujoo.mls.widgets

import android.view.View
import tv.mycujoo.mls.entity.msc.VideoPlayerConfig

interface IPlayerView {

    fun config(config: VideoPlayerConfig)

    fun updateViewersCounter(count: String)
    fun hideViewersCounter()

    fun showEventInformationForPreEvent()
    fun showEventInfoForStartedEvents()
    fun hideEventInfoDialog()

    fun showEventInfoButton()
    fun hideEventInfoButton()

    fun setLiveMode(liveState: MLSPlayerView.LiveState)

    fun continueOverlayAnimations()
    fun freezeOverlayAnimations()

    fun showBuffering()
    fun hideBuffering()

    fun getRemotePlayerControllerView() : RemotePlayerControllerView
    fun switchMode(mode: PlayerControllerMode)
    fun setCastButtonVisibility(showButton: Boolean)

    fun addToTopContainer(view: View)
}
