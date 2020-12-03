package tv.mycujoo.mls.widgets

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.MainThread
import androidx.annotation.Nullable
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.mediarouter.app.MediaRouteButton
import androidx.test.espresso.idling.CountingIdlingResource
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.gms.cast.framework.CastButtonFactory
import kotlinx.android.synthetic.main.dialog_event_info_pre_event_layout.view.*
import kotlinx.android.synthetic.main.dialog_event_info_started_layout.view.*
import kotlinx.android.synthetic.main.main_controls_layout.view.*
import kotlinx.android.synthetic.main.player_view_wrapper.view.*
import tv.mycujoo.domain.entity.HideOverlayActionEntity
import tv.mycujoo.domain.entity.OverlayEntity
import tv.mycujoo.domain.entity.TimelineMarkerEntity
import tv.mycujoo.mls.R
import tv.mycujoo.mls.core.UIEventListener
import tv.mycujoo.mls.entity.msc.VideoPlayerConfig
import tv.mycujoo.mls.helper.DateTimeHelper
import tv.mycujoo.mls.helper.OverlayViewHelper
import tv.mycujoo.mls.manager.TimelineMarkerManager
import tv.mycujoo.mls.manager.contracts.IViewHandler
import tv.mycujoo.mls.widgets.MLSPlayerView.LiveState.*
import tv.mycujoo.mls.widgets.mlstimebar.MLSTimeBar
import tv.mycujoo.mls.widgets.mlstimebar.PointOfInterest
import tv.mycujoo.mls.widgets.mlstimebar.PointOfInterestType
import tv.mycujoo.mls.widgets.mlstimebar.TimelineMarkerView
import java.util.*


class MLSPlayerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), IPlayerView {


    /**region UI Fields*/
    var playerView: PlayerView
    var overlayHost: ConstraintLayout

    private var bufferView: ProgressBar

    private var fullScreenButton: ImageButton
    /**endregion */

    /**region Fields*/
    lateinit var uiEventListener: UIEventListener
    private var isFullScreen = false

    private lateinit var viewHandler: IViewHandler

    private var eventPosterUrl: String? = null
    private lateinit var eventInfoTitle: String
    private lateinit var eventInfoDescription: String
    private lateinit var eventDateTime: String

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


        bufferView = findViewById(R.id.controller_buffering)
        playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT

        findViewById<FrameLayout>(R.id.controller_informationButtonLayout).setOnClickListener {
            showEventInfoForStartedEvents()
        }
        findViewById<ImageButton>(R.id.controller_informationButton).setOnClickListener {
            showEventInfoForStartedEvents()
        }


        val liveBadgeTextView = findViewById<TextView>(R.id.controller_liveBadgeTextView)
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
        setCastButtonAlwaysVisible()

    }

    fun prepare(
        overlayViewHelper: OverlayViewHelper,
        viewHandler: IViewHandler,
        timelineMarkers: List<TimelineMarkerEntity>
    ) {
        this.overlayViewHelper = overlayViewHelper
        this.viewHandler = viewHandler
        initMlsTimeBar(timelineMarkers)

        val mediaRouteButton = findViewById<MediaRouteButton>(R.id.controller_mediaRouteButton)
        CastButtonFactory.setUpMediaRouteButton(context, mediaRouteButton)
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

    fun setTimelineMarker(list: List<TimelineMarkerEntity>) {
        list.map {
            PointOfInterest(
                it.offset,
                it.seekOffset,
                it.label,
                PointOfInterestType(it.color)
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

    private fun setCastButtonAlwaysVisible() {
        findViewById<MediaRouteButton>(R.id.controller_mediaRouteButton).setAlwaysVisible(true)
    }


    private fun removeFullscreenButton() {
        findViewById<ImageButton>(R.id.controller_fullscreenImageButton).visibility = View.GONE
    }
    /**endregion */

    /**region Functionality*/
    fun defaultController(hasDefaultPlayerController: Boolean) {
        playerView.useController = hasDefaultPlayerController
    }

    fun addMarker(longArray: LongArray, booleanArray: BooleanArray) {
        playerView.setExtraAdGroupMarkers(
            longArray,
            booleanArray
        )
    }


    override fun showBuffering() {
        bufferView.visibility = View.VISIBLE
    }

    override fun hideBuffering() {
        bufferView.visibility = View.GONE
    }

    /**region Configuration*/
    override fun config(config: VideoPlayerConfig) {
        try {
            val primaryColor = Color.parseColor(config.primaryColor)
            val secondaryColor = Color.parseColor(config.secondaryColor)

            val mlsTimeBar = findViewById<MLSTimeBar>(R.id.exo_progress)
            mlsTimeBar.setPlayedColor(primaryColor)
            val timelineMarkerView =
                findViewById<TimelineMarkerView>(R.id.exo_timelineMarkerView)
            timelineMarkerView.initialize(config.secondaryColor)

            bufferView.indeterminateTintList = ColorStateList.valueOf(primaryColor)
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

            playerView.player?.playWhenReady = config.autoPlay

            showPlayPauseButtons(config.showPlayPauseButtons)
            showBackForwardsButtons(config.showBackForwardsButtons)
            showSeekBar(config.showSeekBar)
            showFullScreenButton(config.showFullScreenButton)
            showCastButton(config.showCastButton)
            showTimers(config.showTimers)


            if (config.showEventInfoButton) {
                showEventInfoButton()
            } else {
                hideEventInfoButton()
            }

            // enableControls has the highest priority
            if (config.enableControls) {
                playerView.controllerAutoShow = true
                showControlsContainer(true)
            } else {
                playerView.controllerAutoShow = false
                playerView.hideController()
                showPlayPauseButtons(false)
                showBackForwardsButtons(false)
                showSeekBar(false)
                showFullScreenButton(false)
                showTimers(false)
                hideEventInfoButton()
                showControlsContainer(false)
            }


        } catch (e: Exception) {
            Log.e("PlayerViewWrapper", e.message)
        }
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

    private fun showCastButton(showCastButton: Boolean) {
        if (showCastButton) {
            findViewById<FrameLayout>(R.id.controller_castImageButtonContainer).visibility =
                VISIBLE
        } else {
            findViewById<FrameLayout>(R.id.controller_castImageButtonContainer).visibility =
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

    override fun setLiveMode(liveState: LiveState) {
        when (liveState) {
            LIVE_ON_THE_EDGE -> {
                controller_liveBadgeTextView.visibility = View.VISIBLE

                controller_liveBadgeTextView.background =
                    ContextCompat.getDrawable(context, R.drawable.bg_live)
                controller_liveBadgeTextView.isEnabled = false
            }
            LIVE_TRAILING -> {
                controller_liveBadgeTextView.visibility = View.VISIBLE

                controller_liveBadgeTextView.background =
                    ContextCompat.getDrawable(context, R.drawable.bg_live_gray)
                controller_liveBadgeTextView.isEnabled = true
            }
            VOD -> {
                controller_liveBadgeTextView.visibility = View.GONE
            }
        }
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
        positionTextView.text = getStringForTime(time)

        durationTextView.text = getStringForTime(duration)
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
        positionTextView.text = getStringForTime(position)
    }
    /**endregion */

    /**region Private functions*/
    /**
     * Returns the specified millisecond time formatted as a string.
     *
     * @param timeMs The time to format as a string, in milliseconds.
     * @return The time formatted as a string.
     */
    private fun getStringForTime(
        timeMs: Long
    ): String? {
        var timeMs = timeMs
        if (timeMs == C.TIME_UNSET) {
            timeMs = 0
        }
        val totalSeconds = timeMs / 1000
        val seconds = totalSeconds % 60
        val minutes = totalSeconds / 60 % 60
        val hours = totalSeconds / 3600
        this.timeFormatBuilder.setLength(0)
        return if (hours > 0) timeFormatter.format("%d:%02d:%02d", hours, minutes, seconds)
            .toString() else timeFormatter.format("%02d:%02d", minutes, seconds).toString()
    }
    /**endregion */

    /**region New Annotation structure*/
    override fun continueOverlayAnimations() {
        viewHandler.getAnimations().forEach { it.resume() }
    }

    override fun freezeOverlayAnimations() {
        viewHandler.getAnimations().forEach { it.pause() }
    }
    /**endregion */

    /**region Event Info related functions*/
    fun setPosterInfo(posterUrl: String?) {
        eventPosterUrl = posterUrl
    }

    fun setEventInfo(title: String, description: String, startTime: String) {
        eventInfoTitle = title
        eventInfoDescription = description
        eventDateTime = startTime
    }

    override fun showEventInformationPreEventDialog() {
        post {
            playerView.hideController()

            val informationDialog =
                LayoutInflater.from(context)
                    .inflate(R.layout.dialog_event_info_pre_event_layout, this, false)
            eventInfoDialogContainerLayout.addView(informationDialog)

            if (eventPosterUrl != null) {
                informationDialog.eventInfoPreEventDialog_posterView.visibility = View.VISIBLE
                informationDialog.eventInfoPreEventDialog_canvasView.visibility = View.GONE

                Glide.with(informationDialog.eventInfoPreEventDialog_posterView)
                    .load(eventPosterUrl)
                    .into(informationDialog.eventInfoPreEventDialog_posterView as ImageView)
            } else {
                informationDialog.eventInfoPreEventDialog_canvasView.visibility = View.VISIBLE
                informationDialog.eventInfoPreEventDialog_posterView.visibility = View.GONE

                informationDialog.eventInfoPreEventDialog_titleTextView.text = eventInfoTitle
                informationDialog.informationDialog_bodyTextView.text = eventInfoDescription
                informationDialog.informationDialog_dateTimeTextView.text =
                    DateTimeHelper.getDateTime(eventDateTime)
            }


        }


    }

    override fun showEventInfoForStartedEvents() {
        if (this::eventInfoTitle.isInitialized.not() && this::eventInfoDescription.isInitialized.not()) {
            return
        }
        post {
            val eventInfoDialog =
                LayoutInflater.from(context)
                    .inflate(R.layout.dialog_event_info_started_layout, this, false)
            eventInfoDialogContainerLayout.addView(eventInfoDialog)

            eventInfoDialog.eventInfoStartedEventDialog_titleTextView.text = eventInfoTitle
            eventInfoDialog.eventInfoStartedEventDialog_bodyTextView.text =
                eventInfoDescription
            eventInfoDialog.eventInfoStartedEventDialog_dateTimeTextView.text =
                DateTimeHelper.getDateTime(eventDateTime)

            eventInfoDialog.setOnClickListener {
                if (it.parent is ViewGroup) {
                    (it.parent as ViewGroup).removeView(it)
                }
                playerView.showController()
            }
        }


    }

    override fun hideEventInfoDialog() {
        if (eventInfoDialogContainerLayout == null) {
            return
        }
        post {
            eventInfoDialogContainerLayout.children.forEach { child ->
                if (child.tag == "event_info_dialog") {
                    child.visibility = GONE
                    removeView(child)
                }
            }
        }
    }

    override fun showEventInfoButton() {
        post {
            showEventInfoButtonInstantly()
        }
    }

    @MainThread
    fun showEventInfoButtonInstantly() {
        findViewById<FrameLayout>(R.id.controller_informationButtonLayout).visibility =
            View.VISIBLE

    }

    override fun hideEventInfoButton() {
        post {
            hideEventInfoButtonInstantly()
        }
    }

    @MainThread
    fun hideEventInfoButtonInstantly() {
        post {
            findViewById<FrameLayout>(R.id.controller_informationButtonLayout).visibility =
                View.GONE
        }
    }

    /**endregion */

    //regular play-mode
    fun onNewOverlayWithNoAnimation(overlayEntity: OverlayEntity) {
        overlayViewHelper.addViewWithNoAnimation(
            context,
            overlayHost,
            overlayEntity
        )
    }


    fun onNewOverlayWithAnimation(overlayEntity: OverlayEntity) {
        overlayViewHelper.addViewWithAnimation(
            context,
            overlayHost,
            overlayEntity
        )
    }

    fun onOverlayRemovalWithNoAnimation(overlayEntity: OverlayEntity) {
        overlayHost.children.filter { it.tag == overlayEntity.id }
            .forEach {
                if (this::viewHandler.isInitialized) {
                    viewHandler.detachOverlayView(it as ScaffoldView)
                    viewHandler.removeAnimation(overlayEntity.id)
                }
            }

    }

    fun onOverlayRemovalWithNoAnimation(overlayEntity: HideOverlayActionEntity) {
        overlayHost.children.filter { it.tag == overlayEntity.id }
            .forEach {
                if (this::viewHandler.isInitialized) {
                    viewHandler.detachOverlayView(it as ScaffoldView)
                    viewHandler.removeAnimation(overlayEntity.id)
                }
            }

    }

    fun onOverlayRemovalWithAnimation(overlayEntity: OverlayEntity) {
        overlayViewHelper.removeViewWithAnimation(
            overlayHost,
            overlayEntity
        )
    }

    // seek or jump play-mode
    fun addLingeringIntroOverlay(
        overlayEntity: OverlayEntity,
        animationPosition: Long,
        isPlaying: Boolean
    ) {
        overlayViewHelper.addLingeringIntroViewWithAnimation(
            overlayHost,
            overlayEntity,
            animationPosition,
            isPlaying
        )
    }


    fun updateLingeringIntroOverlay(
        overlayEntity: OverlayEntity,
        animationPosition: Long,
        isPlaying: Boolean
    ) {
        overlayViewHelper.updateLingeringIntroOverlay(
            overlayHost,
            overlayEntity,
            animationPosition,
            isPlaying
        )
    }


    fun addLingeringOutroOverlay(
        overlayEntity: OverlayEntity,
        animationPosition: Long,
        isPlaying: Boolean
    ) {
        overlayViewHelper.addLingeringOutroViewWithAnimation(
            overlayHost,
            overlayEntity,
            animationPosition,
            isPlaying
        )
    }


    fun updateLingeringOutroOverlay(
        overlayEntity: OverlayEntity,
        animationPosition: Long,
        isPlaying: Boolean
    ) {
        overlayViewHelper.updateLingeringOutroOverlay(
            overlayHost,
            overlayEntity,
            animationPosition,
            isPlaying
        )
    }

    fun addLingeringMidwayOverlay(overlayEntity: OverlayEntity) {
        overlayViewHelper.addViewWithNoAnimation(
            context,
            overlayHost,
            overlayEntity
        )
    }

    fun updateLingeringMidwayOverlay(overlayEntity: OverlayEntity) {
        overlayViewHelper.updateLingeringMidwayOverlay(
            overlayHost,
            overlayEntity
        )
    }

    fun removeLingeringOverlay(overlayEntity: OverlayEntity) {
        overlayHost.children.filter { it.tag == overlayEntity.id }
            .forEach {
                if (this::viewHandler.isInitialized) {
                    viewHandler.detachOverlayView(it as ScaffoldView)
                    viewHandler.removeAnimation(overlayEntity.id)
                }
            }
    }

    fun clearScreen(idList: List<String>) {
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


    /**region Over-ridden Functions*/
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w != 0 && h != 0) {
            onSizeChangedCallback.invoke()
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