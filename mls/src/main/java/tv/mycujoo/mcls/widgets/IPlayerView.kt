package tv.mycujoo.mcls.widgets

import android.view.View
import tv.mycujoo.mcls.entity.msc.VideoPlayerConfig

interface IPlayerView {

    fun config(config: VideoPlayerConfig)
    fun updateControllerVisibility(isPlaying: Boolean)

    fun updateViewersCounter(count: String)
    fun hideViewersCounter()

    fun showCustomInformationDialog(message: String)
    fun showPreEventInformationDialog()
    fun showStartedEventInformationDialog()
    fun hideInfoDialogs()

    fun showEventInfoButton()
    fun hideEventInfoButton()

    fun setLiveMode(liveState: MLSPlayerView.LiveState)

    fun continueOverlayAnimations()
    fun freezeOverlayAnimations()

    fun showBuffering()
    fun hideBuffering()

    fun getRemotePlayerControllerView(): RemotePlayerControllerView
    fun switchMode(mode: PlayerControllerMode)

    fun addToTopRightContainer(view: View)
    fun removeFromTopRightContainer(view: View)
    fun addToTopLeftContainer(view: View)
    fun removeFromTopLeftContainer(view: View)
}
