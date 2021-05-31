package tv.mycujoo.mcls.core

/**
 * Callback to dispatch UI interaction of Video player to user
 * This may be used to observe clicks on Fullscreen/Minimize screen toggle.
 */
interface UIEventListener {
    /**
     * indicates click on Fullscreen/Minimize screen button in video-player
     * @param fullScreen true if video-player latest state (including the click event) is Fullscreen,
     * false otherwise
     */
    fun onFullScreenButtonClicked(fullScreen: Boolean)
}
