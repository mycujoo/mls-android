package tv.mycujoo.mcls.widgets

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.annotation.MainThread
import androidx.annotation.Nullable
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.children
import androidx.test.espresso.idling.CountingIdlingResource
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.PlayerView
import kotlinx.android.synthetic.main.main_controls_layout.view.*
import kotlinx.android.synthetic.main.player_view_wrapper.view.*
import tv.mycujoo.domain.entity.TimelineMarkerEntity
import tv.mycujoo.mcls.R
import tv.mycujoo.mcls.core.UIEventListener
import tv.mycujoo.mcls.entity.msc.VideoPlayerConfig
import tv.mycujoo.mcls.helper.OverlayViewHelper
import tv.mycujoo.mcls.manager.TimelineMarkerManager
import tv.mycujoo.mcls.manager.contracts.IViewHandler
import tv.mycujoo.mcls.utils.ColorUtils
import tv.mycujoo.mcls.utils.StringUtils.Companion.getFormattedTime
import tv.mycujoo.mcls.widgets.PlayerControllerMode.EXO_MODE
import tv.mycujoo.mcls.widgets.PlayerControllerMode.REMOTE_CONTROLLER
import tv.mycujoo.mcls.widgets.mlstimebar.MLSTimeBar
import tv.mycujoo.mcls.widgets.mlstimebar.PointOfInterest
import tv.mycujoo.mcls.widgets.mlstimebar.PointOfInterestType
import tv.mycujoo.mcls.widgets.mlstimebar.TimelineMarkerView
import tv.mycujoo.ui.PlayerViewContract
import java.util.*
import kotlin.collections.ArrayList


class MLSPlayerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), IPlayerView, PlayerViewContract {


    /**region UI Fields*/
    var playerView: PlayerView
    var overlayHost: ConstraintLayout
    private val topRightContainerHolder: LinearLayout
    private val topLeftContainerHolder: FrameLayout
    private val topRightContainer: LinearLayout
    private val topLeftContainer: LinearLayout

    private var bufferingProgressBar: ProgressBar

    private var fullScreenButton: ImageButton
    private val remotePlayerControllerView: RemotePlayerControllerView
    private val externalInformationButtonLayout: FrameLayout
    /**endregion */

    /**region Fields*/
    lateinit var uiEventListener: UIEventListener
    private var isFullScreen = false
    private var enableControls = false

    private lateinit var viewHandler: IViewHandler

    private val dialogs = ArrayList<View>()
    private var uiEvent = UiEvent()

    private var onSizeChangedCallback = {}

    private lateinit var overlayViewHelper: OverlayViewHelper

    private var timeFormatBuilder = StringBuilder()
    private var timeFormatter = Formatter(timeFormatBuilder, Locale.getDefault())
    private var isScrubbing = false

    private lateinit var timelineMarkerManager: TimelineMarkerManager

    @Nullable
    lateinit var idlingResource: CountingIdlingResource
    /**endregion */

    /**region Initializing*/
    init {
        LayoutInflater.from(context).inflate(R.layout.player_view_wrapper, this, true)

        initAttributes(attrs, context)

        playerView = findViewById(R.id.exoPlayerView)
        overlayHost = ConstraintLayout(context)
        playerView.findViewById<AspectRatioFrameLayout>(R.id.exo_content_frame).addView(overlayHost)

        topRightContainerHolder = findViewById(R.id.controller_topRightContainerHolder)
        topLeftContainerHolder = findViewById(R.id.controller_topLeftContainerHolder)
        topRightContainer = findViewById(R.id.controller_topRightContainer)
        topLeftContainer = findViewById(R.id.controller_topLeftContainer)
        playerView.setControllerVisibilityListener { visibility ->
            topRightContainerHolder.visibility = visibility
            topLeftContainerHolder.visibility = visibility
        }

        externalInformationButtonLayout = findViewById(R.id.informationButtonLayout)

        bufferingProgressBar = findViewById(R.id.controller_buffering)
        playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT

        findViewById<LinearLayout>(R.id.controller_informationButtonLayout).setOnClickListener {
            showStartedEventInformationDialog()
        }
        findViewById<ImageButton>(R.id.controller_informationButton).setOnClickListener {
            showStartedEventInformationDialog()
        }
        findViewById<FrameLayout>(R.id.informationButtonLayout).setOnClickListener {
            showPreEventInformationDialog()
        }
        findViewById<ImageButton>(R.id.informationButton).setOnClickListener {
            showPreEventInformationDialog()
        }


        val liveBadgeTextView = findViewById<LiveBadgeView>(R.id.controller_liveBadgeView)
        liveBadgeTextView.setOnClickListener {
            playerView.player?.seekTo(C.TIME_UNSET)
            it.isEnabled = false
        }


        fullScreenButton = findViewById(R.id.controller_fullscreenImageButton)
        fullScreenButton.setOnClickListener {
            if (this::uiEventListener.isInitialized) {
                isFullScreen = !isFullScreen

                updateFullscreenButtonImage()

                uiEventListener.onFullScreenButtonClicked(isFullScreen)
            }
        }
        updateFullscreenButtonImage()

        remotePlayerControllerView = findViewById(R.id.remotePlayerControllerView)

        playerView.hideController()
        playerView.controllerAutoShow = false
        playerView.useController = false
    }

    fun prepare(
        overlayViewHelper: OverlayViewHelper,
        viewHandler: IViewHandler,
        timelineMarkers: List<TimelineMarkerEntity>
    ) {
        this.overlayViewHelper = overlayViewHelper
        this.viewHandler = viewHandler
        initMlsTimeBar(timelineMarkers)
    }

    private fun initAttributes(attrs: AttributeSet?, context: Context) {
        attrs?.let {
            val obtainAttrs =
                context.obtainStyledAttributes(it, R.styleable.MLSPlayerView)
            if (obtainAttrs.hasValue(R.styleable.MLSPlayerView_has_fullscreen_button)) {
                if (!obtainAttrs.getBoolean(
                        R.styleable.MLSPlayerView_has_fullscreen_button,
                        true
                    )
                ) {
                    removeFullscreenButton()
                }
            }

            obtainAttrs.recycle()
        }
    }

    private fun initMlsTimeBar(list: List<TimelineMarkerEntity>) {
        val mlsTimeBar = findViewById<MLSTimeBar>(R.id.exo_progress)
        mlsTimeBar.setPlayedColor(Color.BLUE)
        val timelineMarkerView =
            findViewById<TimelineMarkerView>(R.id.exo_timelineMarkerView)

        timelineMarkerManager = TimelineMarkerManager(mlsTimeBar, timelineMarkerView)


        list.forEach { showTimelineMarkerEntity ->
            timelineMarkerManager.addTimeLineHighlight(
                PointOfInterest(
                    showTimelineMarkerEntity.offset,
                    showTimelineMarkerEntity.seekOffset,
                    showTimelineMarkerEntity.label,
                    PointOfInterestType(showTimelineMarkerEntity.color)
                )
            )
        }
    }

    override fun context(): Context = context

    override fun overlayHost(): ConstraintLayout = overlayHost

    override fun setTimelineMarker(list: List<TimelineMarkerEntity>) {
        list.map {
            PointOfInterest(
                it.offset,
                it.seekOffset,
                it.label,
                PointOfInterestType(ColorUtils.toARGB(it.color))
            )
        }.let {
            timelineMarkerManager.setTimeLineHighlight(it)
        }
    }
    /**endregion */


    /**region UI*/
    /**
     * set different resize mode that video should respect
     * @param resizeMode
     */
    fun setScreenResizeMode(resizeMode: ResizeMode) {
        playerView.resizeMode = resizeMode.value
    }

    /**
     * set current fullscreen state.
     * updates fullscreen button accordingly
     * @param isFullscreen
     */
    fun setFullscreen(isFullscreen: Boolean) {
        this.isFullScreen = isFullscreen
        updateFullscreenButtonImage()
    }

    // internal use only
    fun setOnSizeChangedCallback(onSizeChangedCallback: () -> Unit) {
        this.onSizeChangedCallback = onSizeChangedCallback
    }

    private fun updateFullscreenButtonImage() {
        if (isFullScreen) {
            fullScreenButton.setImageResource(R.drawable.ic_fullscreen_exit_24dp)
        } else {
            fullScreenButton.setImageResource(R.drawable.ic_fullscreen_24dp)
        }
    }

    private fun removeFullscreenButton() {
        findViewById<ImageButton>(R.id.controller_fullscreenImageButton).visibility = View.GONE
    }
    /**endregion */

    /**region Functionality*/
    fun addMarker(longArray: LongArray, booleanArray: BooleanArray) {
        playerView.setExtraAdGroupMarkers(
            longArray,
            booleanArray
        )
    }

    override fun getRemotePlayerControllerView(): RemotePlayerControllerView {
        return findViewById(R.id.remotePlayerControllerView)
    }

    override fun switchMode(mode: PlayerControllerMode) {
        when (mode) {
            EXO_MODE -> {
                playerView.visibility = View.VISIBLE
                remotePlayerControllerView.visibility = View.GONE
                moveUserAddedViewFromRemoteController()

            }
            REMOTE_CONTROLLER -> {
                playerView.visibility = View.GONE
                remotePlayerControllerView.visibility = View.VISIBLE
                moveUserAddedViewToRemoteController()
            }
        }
    }

    private fun moveUserAddedViewFromRemoteController() {
        remotePlayerControllerView.removeViewFromTopRightHolder()?.let { topRightContainer ->
            topRightContainerHolder.addView(topRightContainer)
        }
        remotePlayerControllerView.removeViewFromTopLeftHolder()?.let { topLeftContainer ->
            topLeftContainerHolder.addView(topLeftContainer)
        }
    }

    private fun moveUserAddedViewToRemoteController() {
        topRightContainerHolder.removeView(topRightContainer)
        remotePlayerControllerView.addViewToTopRightHolder(topRightContainer)

        topLeftContainerHolder.removeView(topLeftContainer)
        remotePlayerControllerView.addViewToTopLeftHolder(topLeftContainer)
    }

    override fun showBuffering() {
        bufferingProgressBar.visibility = View.VISIBLE
    }

    override fun hideBuffering() {
        bufferingProgressBar.visibility = View.GONE
    }

    /**region Configuration*/
    override fun config(config: VideoPlayerConfig) {
        try {
            val primaryColor = Color.parseColor(config.primaryColor)
            val secondaryColor = Color.parseColor(config.secondaryColor)

            setTimeBarsColor(secondaryColor)
            setTimelineMarkerColor(config)
            setBufferingProgressBarsColor(primaryColor)
            setPlayerMainButtonsColor(primaryColor)

            // enableControls has the highest priority
            enableControls = config.enableControls

            if (!enableControls) {
                showPlayPauseButtons(false)
                showBackForwardsButtons(false)
                showSeekBar(false)
                showFullScreenButton(false)
                showTimers(false)
                hideEventInfoButton()
                controllerVisibility(false)
            } else {
                showPlayPauseButtons(config.showPlayPauseButtons)
                showBackForwardsButtons(config.showBackForwardsButtons)
                showSeekBar(config.showSeekBar)
                showFullScreenButton(config.showFullScreenButton)
                showTimers(config.showTimers)
                if (config.showEventInfoButton) {
                    showEventInfoButton()
                } else {
                    hideEventInfoButton()
                }
                controllerVisibility(true)
                playerView.player?.playWhenReady = config.autoPlay
                playerView.showController()
            }

        } catch (e: Exception) {
            Log.e("PlayerViewWrapper", e.message ?: "Error in configuring")
        }
    }

    private fun controllerVisibility(isVisible: Boolean) {
        showControlsContainer(isVisible)
        playerView.controllerAutoShow = isVisible
        playerView.useController = isVisible

    }

    override fun updateControllerVisibility(isPlaying: Boolean) {
        if (enableControls && isPlaying) {
            controllerVisibility(true)
            playerView.showController()
        } else {
            controllerVisibility(false)
            playerView.hideController()
        }
    }

    /**
     * Set exo-player & remote-player main buttons [Play, Pause, Fast-Forward & Rewind] color
     */
    private fun setPlayerMainButtonsColor(primaryColor: Int) {
        findViewById<ImageButton>(R.id.exo_play).setColorFilter(
            primaryColor,
            PorterDuff.Mode.SRC_ATOP
        )

        findViewById<ImageButton>(R.id.exo_pause).setColorFilter(
            primaryColor,
            PorterDuff.Mode.SRC_ATOP
        )
        findViewById<ImageButton>(R.id.exo_rew).setColorFilter(
            primaryColor,
            PorterDuff.Mode.SRC_ATOP
        )
        findViewById<ImageButton>(R.id.exo_ffwd).setColorFilter(
            primaryColor,
            PorterDuff.Mode.SRC_ATOP
        )

        remotePlayerControllerView.setPlayerMainButtonsColor(primaryColor)
    }

    /**
     * Set exo-player time-bar & remote-player timer-bar played-color
     */
    private fun setTimeBarsColor(primaryColor: Int) {
        val mlsTimeBar = findViewById<MLSTimeBar>(R.id.exo_progress)
        mlsTimeBar.setPlayedColor(primaryColor)

        remotePlayerControllerView.setTimeBarPlayedColor(primaryColor)
    }

    private fun setTimelineMarkerColor(config: VideoPlayerConfig) {
        val timelineMarkerView =
            findViewById<TimelineMarkerView>(R.id.exo_timelineMarkerView)
        timelineMarkerView.initialize(config.secondaryColor)
    }

    /**
     * Set exo-player & remote-player buffering progress-bar color
     */
    private fun setBufferingProgressBarsColor(primaryColor: Int) {
        bufferingProgressBar.indeterminateTintList = ColorStateList.valueOf(primaryColor)
        remotePlayerControllerView.setBufferingProgressBarsColor(primaryColor)
    }

    private fun showControlsContainer(show: Boolean) {
        if (show) {
            findViewById<ConstraintLayout>(R.id.controlsLayoutContainer).visibility =
                VISIBLE
        } else {
            findViewById<ConstraintLayout>(R.id.controlsLayoutContainer).visibility =
                GONE
        }

    }

    private fun showTimers(showTimers: Boolean) {
        if (showTimers) {
            findViewById<ConstraintLayout>(R.id.controller_timersContainer).visibility =
                VISIBLE
        } else {
            findViewById<ConstraintLayout>(R.id.controller_timersContainer).visibility =
                View.GONE
        }
    }

    private fun showFullScreenButton(showFullScreenButton: Boolean) {
        if (showFullScreenButton) {
            findViewById<FrameLayout>(R.id.controller_fullscreenImageButtonContainer).visibility =
                VISIBLE
        } else {
            findViewById<FrameLayout>(R.id.controller_fullscreenImageButtonContainer).visibility =
                View.GONE
        }
    }

    private fun showSeekBar(showSeekBar: Boolean) {
        if (showSeekBar) {
            findViewById<FrameLayout>(R.id.controller_timeBarLayoutContainer).visibility =
                VISIBLE
        } else {
            findViewById<FrameLayout>(R.id.controller_timeBarLayoutContainer).visibility =
                View.GONE
        }

    }

    private fun showBackForwardsButtons(showBackForwardsButtons: Boolean) {
        if (showBackForwardsButtons) {
            findViewById<ImageButton>(R.id.exo_rew).visibility = VISIBLE
            findViewById<FrameLayout>(R.id.controller_rewButtonContainerLayout).visibility =
                VISIBLE
            findViewById<FrameLayout>(R.id.controller_ffwdButtonContainerLayout).visibility =
                VISIBLE
            findViewById<ImageButton>(R.id.exo_ffwd).visibility = VISIBLE
        } else {
            findViewById<ImageButton>(R.id.exo_rew).visibility = View.GONE
            findViewById<FrameLayout>(R.id.controller_rewButtonContainerLayout).visibility =
                View.GONE
            findViewById<FrameLayout>(R.id.controller_ffwdButtonContainerLayout).visibility =
                View.GONE
            findViewById<ImageButton>(R.id.exo_ffwd).visibility = View.GONE
        }

    }

    private fun showPlayPauseButtons(showPlayPauseButtons: Boolean) {
        if (showPlayPauseButtons) {
            findViewById<FrameLayout>(R.id.controller_playPauseButtonContainerLayout).visibility =
                VISIBLE
        } else {
            findViewById<FrameLayout>(R.id.controller_playPauseButtonContainerLayout).visibility =
                View.GONE
        }
    }

    /**endregion */

    /**
     * Set exo-player & remote-player LIVE badge state
     */
    override fun setLiveMode(liveState: LiveState) {
        controller_liveBadgeView.setLiveMode(liveState)
    }

    override fun updateViewersCounter(count: String) {
        controller_viewersCountLayout.visibility = View.VISIBLE
        controller_viewersCountTextView.text = count
    }

    override fun hideViewersCounter() {
        controller_viewersCountLayout.visibility = View.GONE
    }

    fun getTimeBar(): MLSTimeBar {
        return findViewById(R.id.exo_progress)
    }

    fun updateTime(time: Long, duration: Long) {
        if (isScrubbing) {
            return
        }
        positionTextView.text = getFormattedTime(time, timeFormatBuilder, timeFormatter)
        durationTextView.text = getFormattedTime(duration, timeFormatBuilder, timeFormatter)
    }

    fun scrubStopAt(position: Long) {
        isScrubbing = false
        scrubbedTo(position)
    }

    fun scrubStartedAt(position: Long) {
        isScrubbing = true
        scrubbedTo(position)
    }

    fun scrubbedTo(position: Long) {
        positionTextView.text = getFormattedTime(position, timeFormatBuilder, timeFormatter)
    }
    /**endregion */


    /**region New Annotation structure*/
    override fun continueOverlayAnimations() {
        viewHandler.getAnimations().forEach { it.resume() }
    }

    override fun freezeOverlayAnimations() {
        viewHandler.getAnimations().forEach { it.pause() }
    }

    override fun clearScreen(idList: List<String>) {
        overlayHost.children
            .forEach {
                if (idList.contains(it.tag)) {
                    if (this::viewHandler.isInitialized) {
                        viewHandler.detachOverlayView(it as ScaffoldView)
                        viewHandler.removeAnimation(it.tag as String)
                    }
                }
            }

        viewHandler.clearAll()
    }
    /**endregion */

    /**region Event Info related functions*/
    fun setPosterInfo(posterUrl: String?) {
        uiEvent = uiEvent.copy(posterUrl = posterUrl)
    }

    fun setEventInfo(title: String, description: String?, startTime: String?) {
        uiEvent = UiEvent(title, description, startTime)
    }

    override fun showCustomInformationDialog(message: String) {
        post {
            playerView.hideController()

            val dialog = CustomInformationDialog(
                container = infoDialogContainerLayout,
                uiEvent = uiEvent,
                message = message
            )
            dialogs.add(dialog)
        }
    }

    override fun showPreEventInformationDialog() {
        post {
            playerView.hideController()

            val dialog = PreEventInformationDialog(
                container = infoDialogContainerLayout,
                uiEvent = uiEvent
            )
            dialogs.add(dialog)
        }
    }

    override fun showStartedEventInformationDialog() {
        post {
            val dialog = StartedEventInformationDialog(
                mlsPlayerView = this,
                uiEvent = uiEvent
            )
            dialogs.add(dialog)
        }
    }

    override fun hideInfoDialogs() {
        if (infoDialogContainerLayout == null) {
            return
        }
        post {
            dialogs.forEach { dialog ->
                infoDialogContainerLayout.removeView(dialog)
            }
            dialogs.clear()
        }
    }

    override fun showEventInfoButton() {
        post {
            showEventInfoButtonInstantly()
        }
    }

    @MainThread
    fun showEventInfoButtonInstantly() {
        findViewById<LinearLayout>(R.id.controller_informationButtonLayout).visibility =
            View.VISIBLE
    }

    override fun hideEventInfoButton() {
        post { hideEventInfoButtonInstantly() }
    }

    @MainThread
    fun hideEventInfoButtonInstantly() {
        post {
            findViewById<LinearLayout>(R.id.controller_informationButtonLayout).visibility =
                View.GONE
        }
    }
    /**endregion */


    /**region Over-ridden Functions*/
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w != 0 && h != 0) {
            onSizeChangedCallback.invoke()
        }
    }

    override fun addToTopRightContainer(view: View) {
        topRightContainer.addView(view)
    }

    override fun removeFromTopRightContainer(view: View) {
        topRightContainer.children.any { it == view }.let {
            topRightContainer.removeView(view)
        }
    }

    override fun addToTopLeftContainer(view: View) {
        topLeftContainer.addView(view)
    }

    override fun removeFromTopLeftContainer(view: View) {
        topLeftContainer.children.any { it == view }.let {
            topLeftContainer.removeView(view)
        }
    }

    /**endregion */

    /**region Classes*/
    enum class LiveState {
        LIVE_ON_THE_EDGE,
        LIVE_TRAILING,
        VOD
    }

    /**endregion */

    /**region Inner classes*/

    enum class ResizeMode(val value: Int) {
        RESIZE_MODE_FIT(0),
        RESIZE_MODE_FIXED_WIDTH(1),
        RESIZE_MODE_FIXED_HEIGHT(2),
        RESIZE_MODE_FILL(3);
    }
    /**endregion */


}