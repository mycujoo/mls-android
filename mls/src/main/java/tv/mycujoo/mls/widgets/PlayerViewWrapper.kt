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
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.Nullable
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.test.espresso.idling.CountingIdlingResource
import com.caverock.androidsvg.SVG
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH
import com.google.android.exoplayer2.ui.PlayerView
import kotlinx.android.synthetic.main.dialog_information_layout.view.*
import kotlinx.android.synthetic.main.main_controls_layout.view.*
import tv.mycujoo.domain.entity.ActionEntity
import tv.mycujoo.domain.entity.HideOverlayActionEntity
import tv.mycujoo.domain.entity.ShowOverlayActionEntity
import tv.mycujoo.domain.entity.models.ActionType
import tv.mycujoo.domain.usecase.GetAnnotationFromJSONUseCase
import tv.mycujoo.mls.R
import tv.mycujoo.mls.core.UIEventListener
import tv.mycujoo.mls.entity.msc.VideoPlayerConfig
import tv.mycujoo.mls.extensions.getDisplaySize
import tv.mycujoo.mls.helper.AnimationFactory
import tv.mycujoo.mls.helper.OverlayFactory
import tv.mycujoo.mls.helper.OverlayViewHelper
import tv.mycujoo.mls.manager.TimelineMarkerManager
import tv.mycujoo.mls.manager.ViewIdentifierManager
import tv.mycujoo.mls.widgets.PlayerViewWrapper.LiveState.*
import tv.mycujoo.mls.widgets.mlstimebar.MLSTimeBar
import tv.mycujoo.mls.widgets.mlstimebar.PointOfInterest
import tv.mycujoo.mls.widgets.mlstimebar.PointOfInterestType
import tv.mycujoo.mls.widgets.mlstimebar.TimelineMarker


class PlayerViewWrapper @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {


    /**region UI Fields*/
    var playerView: PlayerView
    private var overlayHost: OverlayHost

    private var bufferView: ProgressBar

    private var fullScreenButton: ImageButton
    /**endregion */

    /**region Fields*/
    lateinit var uiEventListener: UIEventListener
    private var isFullScreen = false

    private val viewIdentifierManager = ViewIdentifierManager()

    private lateinit var eventInfoTitle: String
    private lateinit var eventInfoDescription: String

    @Nullable
    val idlingResource = CountingIdlingResource("displaying_overlays")
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

        findViewById<ImageButton>(R.id.controller_informationButton).setOnClickListener {
            if (this::eventInfoTitle.isInitialized && this::eventInfoDescription.isInitialized) {
                displayEventInformationDialog(eventInfoTitle, eventInfoDescription, true)
            }
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

        initMlsTimeBar()

    }

    private fun initAttributes(attrs: AttributeSet?, context: Context) {
        attrs?.let {
            val obtainAttrs =
                context.obtainStyledAttributes(it, R.styleable.PlayerViewWrapper)
            if (obtainAttrs.hasValue(R.styleable.PlayerViewWrapper_has_fullscreen_button)) {
                if (!obtainAttrs.getBoolean(
                        R.styleable.PlayerViewWrapper_has_fullscreen_button,
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

    private fun initMlsTimeBar() {
        val mlsTimeBar = findViewById<MLSTimeBar>(R.id.exo_progress)
        mlsTimeBar.setPlayedColor(Color.BLUE)
        val highlightMarkerTextView =
            findViewById<TimelineMarker>(R.id.exo_highlight_marker_title_highlight_marker)

        val timelineMarkerManager = TimelineMarkerManager(mlsTimeBar, highlightMarkerTextView)

        val greenPointOfInterestType = PointOfInterestType(Color.GREEN)
        val redPointOfInterestType = PointOfInterestType(Color.RED)


        GetAnnotationFromJSONUseCase.mappedResult()
            .forEach {
                it.actions.filter { it.type == ActionType.SHOW_TIMELINE_MARKER }
                    .forEach { showTimelineMarkerEntity ->
                        timelineMarkerManager.addTimeLineHighlight(
                            PointOfInterest(
                                showTimelineMarkerEntity.offset,
                                listOf(showTimelineMarkerEntity.label!!),
                                redPointOfInterestType
                            )
                        )
                    }
            }


    }
    /**endregion */

    /**region UI*/

    fun screenMode(screenMode: ScreenMode) {
        when (screenMode) {
            ScreenMode.PORTRAIT -> {

                val displaySize = context.getDisplaySize()
                val layoutParams = layoutParams as ViewGroup.LayoutParams
                layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                layoutParams.height = displaySize.width * 9 / 16

                setLayoutParams(layoutParams)

            }
            ScreenMode.LANDSCAPE -> {

                val layoutParams = layoutParams as ViewGroup.LayoutParams
                layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT

                setLayoutParams(layoutParams)
            }
        }
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


    fun hideOverlay(viewId: String) {
        viewIdentifierManager.getViewId(viewId)?.let {
            findViewById<ViewGroup>(it)?.visibility = View.INVISIBLE

        }
    }

    fun removeOverlay(viewId: String) {
        viewIdentifierManager.getViewId(viewId)?.let {
            findViewById<ViewGroup>(it)?.let { overlayView -> overlayHost.removeView(overlayView) }
        }
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
            val highlightMarkerTextView =
                findViewById<TimelineMarker>(R.id.exo_highlight_marker_title_highlight_marker)
            highlightMarkerTextView.initialize(config.secondaryColor)

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


    fun getTimeBar(): MLSTimeBar {
        return findViewById(R.id.exo_progress)
    }

    fun action(actionEntity: ActionEntity) {
        actionEntity.type
    }


    /**endregion */

    /**region New Annotation structure*/

    fun showOverlay(overlayEntity: ShowOverlayActionEntity) {
        val proportionalImageView = OverlayFactory.create(context, overlayEntity.size)
        try {
            val svg: SVG
            if (overlayEntity.svgInputStream != null) {
                svg = SVG.getFromInputStream(overlayEntity.svgInputStream)
            } else {
                svg = SVG.getFromString(getTimeSvgString())
            }
            svg.setDocumentWidth("100%")
            svg.setDocumentHeight("100%")
            proportionalImageView.setSVG(svg)

            val animation = AnimationFactory.createStaticAnimation(
                proportionalImageView,
                overlayEntity.introAnimationType,
                overlayEntity.introAnimationDuration
            )

            OverlayViewHelper.addView(
                overlayHost,
                proportionalImageView,
                overlayEntity.positionGuide,
                overlayEntity,
                animation,
                viewIdentifierManager,
                idlingResource
            )


            viewIdentifierManager.storeViewId(proportionalImageView, overlayEntity.customId!!)

        } catch (e: Exception) {
            Log.e("PlayerView", "e: ${e.message}")
        }
    }

    fun hideOverlay(hideOverlayActionEntity: HideOverlayActionEntity) {
        idlingResource.increment()
        OverlayViewHelper.removeView(
            overlayHost,
            viewIdentifierManager.getViewId(hideOverlayActionEntity.customId!!),
            hideOverlayActionEntity,
            idlingResource
        )
        //todo: remove animation too
    }


    fun getTimeSvgString(): String {
        return "<svg height=\"30\" width=\"200\"><rect width=\"200\" height=\"30\" style=\"fill:rgb(211,211,211);stroke-width:3;stroke:rgb(128, 128, 128)\" /><text x=\"0\" y=\"15\" fill=\"red\">Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.</text></svg>"
    }

    fun showLingeringOverlay(overlayEntity: ShowOverlayActionEntity) {
        val proportionalImageView = OverlayFactory.create(context, overlayEntity.size)
        try {
            val svg: SVG
            if (overlayEntity.svgInputStream != null) {
                svg = SVG.getFromInputStream(overlayEntity.svgInputStream)
            } else {
                svg = SVG.getFromString(getTimeSvgString())
            }
            svg.setDocumentWidth("100%")
            svg.setDocumentHeight("100%")
            proportionalImageView.setSVG(svg)

            OverlayViewHelper.addViewWithNoAnimation(
                overlayHost,
                proportionalImageView,
                overlayEntity.positionGuide,
                overlayEntity,
                idlingResource
            )



            viewIdentifierManager.storeViewId(proportionalImageView, overlayEntity.customId!!)

        } catch (e: Exception) {
            Log.e("PlayerView", "e: ${e.message}")
        }
    }


    fun onLingeringIntroAnimationAvailable(
        overlayEntity: ShowOverlayActionEntity,
        animationPosition: Long,
        isPlaying: Boolean
    ) {
        val proportionalImageView = OverlayFactory.create(context, overlayEntity.size)
        try {
            val svg: SVG
            if (overlayEntity.svgInputStream != null) {
                svg = SVG.getFromInputStream(overlayEntity.svgInputStream)
            } else {
                svg = SVG.getFromString(getTimeSvgString())
            }
            svg.setDocumentWidth("100%")
            svg.setDocumentHeight("100%")
            proportionalImageView.setSVG(svg)

            OverlayViewHelper.addViewWithLingeringIntroAnimation(
                overlayHost,
                proportionalImageView,
                overlayEntity.positionGuide,
                overlayEntity,
                animationPosition,
                isPlaying,
                viewIdentifierManager,
                idlingResource
            )



            viewIdentifierManager.storeViewId(proportionalImageView, overlayEntity.customId!!)

        } catch (e: Exception) {
            Log.e("PlayerView", "e: ${e.message}")
        }
    }

    fun onLingeringOutroAnimationAvailable(
        overlayEntity: ShowOverlayActionEntity,
        hideOverlayActionEntity: HideOverlayActionEntity,
        animationPosition: Long,
        isPlaying: Boolean
    ) {
        val proportionalImageView = OverlayFactory.create(context, overlayEntity.size)
        try {
            val svg: SVG
            if (overlayEntity.svgInputStream != null) {
                svg = SVG.getFromInputStream(overlayEntity.svgInputStream)
            } else {
                svg = SVG.getFromString(getTimeSvgString())
            }
            svg.setDocumentWidth("100%")
            svg.setDocumentHeight("100%")
            proportionalImageView.setSVG(svg)

            OverlayViewHelper.addViewWithLingeringOutroAnimation(
                overlayHost,
                proportionalImageView,
                overlayEntity.positionGuide,
                overlayEntity,
                hideOverlayActionEntity,
                animationPosition,
                isPlaying,
                viewIdentifierManager,
                idlingResource
            )

            viewIdentifierManager.storeViewId(proportionalImageView, overlayEntity.customId!!)

        } catch (e: Exception) {
            Log.e("PlayerView", "e: ${e.message}")
        }
    }

    fun onLingeringOutroAnimationAvailableFromSameCommand(
        overlayEntity: ShowOverlayActionEntity,
        animationPosition: Long,
        isPlaying: Boolean
    ) {
        val proportionalImageView = OverlayFactory.create(context, overlayEntity.size)
        try {
            val svg: SVG
            if (overlayEntity.svgInputStream != null) {
                svg = SVG.getFromInputStream(overlayEntity.svgInputStream)
            } else {
                svg = SVG.getFromString(getTimeSvgString())
            }
            svg.setDocumentWidth("100%")
            svg.setDocumentHeight("100%")
            proportionalImageView.setSVG(svg)

            OverlayViewHelper.addViewWithLingeringOutroAnimationFromSameCommand(
                overlayHost,
                proportionalImageView,
                overlayEntity.positionGuide,
                overlayEntity,
                animationPosition,
                isPlaying,
                viewIdentifierManager,
                idlingResource
            )

            viewIdentifierManager.storeViewId(proportionalImageView, overlayEntity.customId!!)

        } catch (e: Exception) {
            Log.e("PlayerView", "e: ${e.message}")
        }
    }

    fun onNewOutroAnimationAvailable(
        relatedActionEntity: ActionEntity,
        hideActionEntity: ActionEntity
    ) {

        OverlayViewHelper.addOutroAnimationToCurrentOverlay(
            overlayHost,
            relatedActionEntity.customId,
            hideActionEntity,
            viewIdentifierManager
        )
    }

    fun onNewOutroAnimationAvailable(
        relatedActionEntity: ActionEntity
    ) {

        OverlayViewHelper.runOutroAnimationOfCurrentOverlaySameCommand(
            overlayHost,
            relatedActionEntity,
            viewIdentifierManager
        )
    }

    fun clearScreen(customIdList: List<String>) {
        val viewIdentifierToBeCleared =
            customIdList.map { viewIdentifierManager.getViewId(it) }

        OverlayViewHelper.clearScreen(
            overlayHost,
            viewIdentifierToBeCleared,
            idlingResource
        )

    }

    fun continueOverlayAnimations() {
        viewIdentifierManager.getAnimations().forEach { it.resume() }
    }

    fun freezeOverlayAnimations() {
        viewIdentifierManager.getAnimations().forEach { it.pause() }
    }
    /**endregion */

    /**region Event Info related functions*/
    fun displayEventInformationDialog(title: String, description: String, cancelable: Boolean) {
        playerView.hideController()

        val informationDialog =
            LayoutInflater.from(context).inflate(R.layout.dialog_information_layout, this, false)
        addView(informationDialog)

        val constraintSet = ConstraintSet()
        constraintSet.clone(this)

        constraintSet.connect(
            informationDialog.id,
            ConstraintSet.TOP,
            ConstraintSet.PARENT_ID,
            ConstraintSet.TOP
        )
        constraintSet.connect(
            informationDialog.id,
            ConstraintSet.BOTTOM,
            ConstraintSet.PARENT_ID,
            ConstraintSet.BOTTOM
        )
        constraintSet.connect(
            informationDialog.id,
            ConstraintSet.START,
            ConstraintSet.PARENT_ID,
            ConstraintSet.START
        )
        constraintSet.connect(
            informationDialog.id,
            ConstraintSet.END,
            ConstraintSet.PARENT_ID,
            ConstraintSet.END
        )
        constraintSet.applyTo(this)

        informationDialog.informationDialog_titleTextView.text = title
        informationDialog.informationDialog_bodyTextView.text = description

        if (cancelable) {
            informationDialog.setOnClickListener {
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
        findViewById<ImageButton>(R.id.controller_informationButton).visibility =
            View.VISIBLE
    }

    fun hideEventInfoButton() {
        findViewById<ImageButton>(R.id.controller_informationButton).visibility =
            View.GONE
    }

    /**endregion */

    /**region Classes*/

    enum class ScreenMode {
        PORTRAIT,
        LANDSCAPE
    }

    enum class LiveState {
        LIVE_ON_THE_EDGE,
        LIVE_TRAILING,
        VOD
    }
    /**endregion */


}