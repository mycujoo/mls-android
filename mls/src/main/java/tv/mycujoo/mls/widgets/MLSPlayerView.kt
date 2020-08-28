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
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.Nullable
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.test.espresso.idling.CountingIdlingResource
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ui.PlayerView
import kotlinx.android.synthetic.main.dialog_event_info_pre_event_layout.view.*
import kotlinx.android.synthetic.main.dialog_event_info_started_layout.view.*
import kotlinx.android.synthetic.main.main_controls_layout.view.*
import kotlinx.android.synthetic.main.player_view_wrapper.view.*
import tv.mycujoo.domain.entity.OverlayEntity
import tv.mycujoo.domain.entity.TimelineMarkerEntity
import tv.mycujoo.mls.R
import tv.mycujoo.mls.core.UIEventListener
import tv.mycujoo.mls.entity.msc.VideoPlayerConfig
import tv.mycujoo.mls.extensions.getDisplaySize
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
) : ConstraintLayout(context, attrs, defStyleAttr) {


    /**region UI Fields*/
    var playerView: PlayerView
    var overlayHost: OverlayHost

    private var bufferView: ProgressBar

    private var fullScreenButton: ImageButton
    /**endregion */

    /**region Fields*/
    lateinit var uiEventListener: UIEventListener
    private var isFullScreen = false

    private lateinit var viewHandler: IViewHandler

    private lateinit var eventInfoTitle: String
    private lateinit var eventInfoDescription: String
    private lateinit var eventDateTime: String

    private var onSizeChangedCallback = {}

    private lateinit var overlayViewHelper: OverlayViewHelper

    private var timeFormatBuilder = StringBuilder()
    private var timeFormatter = Formatter(timeFormatBuilder, Locale.getDefault())
    private var isScrubbing = false

    @Nullable
    lateinit var idlingResource: CountingIdlingResource
    /**endregion */

    /**region Initializing*/
    init {
        LayoutInflater.from(context).inflate(R.layout.player_view_wrapper, this, true)

        initAttributes(attrs, context)

        playerView = findViewById(R.id.playerView)
        overlayHost = OverlayHost(context)
        playerView.addView(overlayHost, 1)


        bufferView = findViewById(R.id.controller_buffering)
        playerView.resizeMode = RESIZE_MODE_FIXED_WIDTH

        findViewById<FrameLayout>(R.id.controller_informationButtonLayout).setOnClickListener {
            displayEventInfoForStartedEvents()
        }
        findViewById<ImageButton>(R.id.controller_informationButton).setOnClickListener {
            displayEventInfoForStartedEvents()
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

                if (isFullScreen) {
                    fullScreenButton.setImageResource(R.drawable.ic_fullscreen_exit_24dp)
                } else {
                    fullScreenButton.setImageResource(R.drawable.ic_fullscreen_24dp)
                }

                uiEventListener.onFullScreenButtonClicked(isFullScreen)
            }
        }


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

    private fun removeFullscreenButton() {
        findViewById<ImageButton>(R.id.controller_fullscreenImageButton).visibility = View.GONE
    }

    private fun initMlsTimeBar(list: List<TimelineMarkerEntity>) {
        val mlsTimeBar = findViewById<MLSTimeBar>(R.id.exo_progress)
        mlsTimeBar.setPlayedColor(Color.BLUE)
        val timelineMarkerView =
            findViewById<TimelineMarkerView>(R.id.exo_timelineMarkerView)

        val timelineMarkerManager = TimelineMarkerManager(mlsTimeBar, timelineMarkerView)


        list.forEach { showTimelineMarkerEntity ->
            timelineMarkerManager.addTimeLineHighlight(
                PointOfInterest(
                    showTimelineMarkerEntity.offset,
                    showTimelineMarkerEntity.seekOffset,
                    listOf(showTimelineMarkerEntity.label),
                    PointOfInterestType(showTimelineMarkerEntity.color)
                )
            )
        }
    }
    /**endregion */

    /**region UI*/
    fun screenMode(screenMode: ScreenMode) {
        when (screenMode) {
            is ScreenMode.Portrait -> {
                playerView.resizeMode = screenMode.resizeMode

                val displaySize = context.getDisplaySize()
                val layoutParams = layoutParams as ViewGroup.LayoutParams
                layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                layoutParams.height = displaySize.width * 9 / 16

                setLayoutParams(layoutParams)
            }
            is ScreenMode.Landscape -> {
                playerView.resizeMode = screenMode.resizeMode

                val layoutParams = layoutParams as ViewGroup.LayoutParams

                when (screenMode.resizeMode) {
                    RESIZE_MODE_FIT -> {
                        layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
                        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                    }
                    RESIZE_MODE_FIXED_WIDTH -> {
                        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                    }
                    RESIZE_MODE_FIXED_HEIGHT -> {
                        layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
                        layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
                    }

                    RESIZE_MODE_FILL -> {
                        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                        layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
                    }
                }

                setLayoutParams(layoutParams)
            }
        }
    }

    // internal use only
    fun setOnSizeChangedCallback(onSizeChangedCallback: () -> Unit) {
        this.onSizeChangedCallback = onSizeChangedCallback
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


    fun showBuffering() {
        bufferView.visibility = View.VISIBLE
    }

    fun hideBuffering() {
        bufferView.visibility = View.GONE
    }

    fun config(config: VideoPlayerConfig) {
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

            if (config.backForwardButtons) {
                findViewById<ImageButton>(R.id.exo_rew).visibility = View.VISIBLE
                findViewById<ImageButton>(R.id.exo_ffwd).visibility = View.VISIBLE
            } else {
                findViewById<ImageButton>(R.id.exo_rew).visibility = View.GONE
                findViewById<ImageButton>(R.id.exo_ffwd).visibility = View.GONE
            }

            if (config.eventInfoButton) {
                showEventInfoButton()
            } else {
                hideEventInfoButton()
            }


        } catch (e: Exception) {
            Log.e("PlayerViewWrapper", e.message)
        } finally {


        }
    }

    fun setLiveMode(liveState: LiveState) {
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

    fun updateViewersCounter(count: String) {
        controller_viewersCountLayout.visibility = View.VISIBLE
        controller_viewersCountTextView.text = count
    }

    fun hideViewersCounter() {
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

    fun getTimeSvgString(): String {
        return "<svg height=\"30\" width=\"200\"><rect width=\"200\" height=\"30\" style=\"fill:rgb(211,211,211);stroke-width:3;stroke:rgb(128, 128, 128)\" /><text x=\"0\" y=\"15\" fill=\"red\">Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.</text></svg>"
    }

    fun continueOverlayAnimations() {
        viewHandler.getAnimations().forEach { it.resume() }
    }

    fun freezeOverlayAnimations() {
        viewHandler.getAnimations().forEach { it.pause() }
    }
    /**endregion */

    /**region Event Info related functions*/
    fun setEventInfo(title: String, description: String, startTime: String) {
        eventInfoTitle = title
        eventInfoDescription = description
        eventDateTime = startTime
    }

    fun displayEventInformationPreEventDialog() {
        post {
            playerView.hideController()

            val informationDialog =
                LayoutInflater.from(context).inflate(R.layout.dialog_event_info_pre_event_layout, this, false)
            eventInfoDialogContainerLayout.addView(informationDialog)

            informationDialog.eventInfoPreEventDialog_titleTextView.text = eventInfoTitle
            informationDialog.informationDialog_bodyTextView.text = eventInfoDescription
            informationDialog.informationDialog_dateTimeTextView.text = DateTimeHelper.getDateTime(eventDateTime)

        }


    }

    fun displayEventInfoForStartedEvents() {
        if (this::eventInfoTitle.isInitialized.not() && this::eventInfoDescription.isInitialized.not()) {
            return
        }
        post {
            val eventInfoDialog =
                LayoutInflater.from(context).inflate(R.layout.dialog_event_info_started_layout, this, false)
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

    fun hideEventInfoDialog() {
        children.forEach { child ->
            if (child.tag == "event_info_dialog") {
                removeView(child)
            }
        }
    }

    fun showEventInfoButton() {
        findViewById<ImageButton>(R.id.controller_informationButtonLayout).visibility =
            View.VISIBLE
    }

    fun hideEventInfoButton() {
        findViewById<ImageButton>(R.id.controller_informationButtonLayout).visibility =
            View.GONE
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

    sealed class ScreenMode {
        data class Portrait(val resizeMode: Int = 0) :
            ScreenMode()

        data class Landscape(val resizeMode: Int = 0) : ScreenMode()
    }

    enum class LiveState {
        LIVE_ON_THE_EDGE,
        LIVE_TRAILING,
        VOD
    }

    /**endregion */

    companion object {
        /**
         * Either the width or height is decreased to obtain the desired aspect ratio.
         */
        const val RESIZE_MODE_FIT = 0

        /**
         * The width is fixed and the height is increased or decreased to obtain the desired aspect ratio.
         */
        const val RESIZE_MODE_FIXED_WIDTH = 1

        /**
         * The height is fixed and the width is increased or decreased to obtain the desired aspect ratio.
         */
        const val RESIZE_MODE_FIXED_HEIGHT = 2

        /**
         * The specified aspect ratio is ignored.
         */
        const val RESIZE_MODE_FILL = 3
    }


}