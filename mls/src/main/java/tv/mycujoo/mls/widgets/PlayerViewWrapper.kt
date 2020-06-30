package tv.mycujoo.mls.widgets

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Handler
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.Nullable
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.view.updateLayoutParams
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
import tv.mycujoo.mls.R
import tv.mycujoo.mls.core.UIEventListener
import tv.mycujoo.mls.entity.actions.*
import tv.mycujoo.mls.entity.msc.VideoPlayerConfig
import tv.mycujoo.mls.extensions.getDisplaySize
import tv.mycujoo.mls.helper.OverlayCommandHelper
import tv.mycujoo.mls.helper.OverlayFactory
import tv.mycujoo.mls.helper.OverlayViewHelper
import tv.mycujoo.mls.helper.TimeBarAnnotationHelper
import tv.mycujoo.mls.manager.HighlightMarkerManager
import tv.mycujoo.mls.manager.ViewIdentifierManager
import tv.mycujoo.mls.widgets.PlayerViewWrapper.LiveState.*
import tv.mycujoo.mls.widgets.mlstimebar.HighlightMarker
import tv.mycujoo.mls.widgets.mlstimebar.MLSTimeBar
import tv.mycujoo.mls.widgets.mlstimebar.PointOfInterest
import tv.mycujoo.mls.widgets.mlstimebar.PointOfInterestType
import tv.mycujoo.mls.widgets.overlay.AnnouncementOverlayView
import tv.mycujoo.mls.widgets.overlay.ScoreboardOverlayView


class PlayerViewWrapper @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {


    /**region UI Fields*/
    private var bufferView: ProgressBar
    /**endregion */

    /**region Fields*/
    var playerView: PlayerView
    private var overlayHost: OverlayHost

    lateinit var uiEventListener: UIEventListener
    private var isFullScreen = false
    private var fullScreenButton: ImageButton

    lateinit var timeBarAnnotationHelper: TimeBarAnnotationHelper


    private var dismissingHandler = Handler()

    private val overlaySingleLineHashMap = HashMap<LayoutType, BasicSingleLineOverlayView>()
    private val overlayDoubleLineHashMap = HashMap<LayoutType, BasicDoubleLineOverlayView>()

    private val viewIdentifierManager = ViewIdentifierManager()

    @Nullable
    val idlingResource = CountingIdlingResource("displaying_overlays")
    /**endregion */

    /**region Initializing*/
    init {
        LayoutInflater.from(context).inflate(R.layout.player_widget_layout, this, true)

        playerView = findViewById(R.id.playerView)
        overlayHost = findViewById(R.id.playerWidget_overlayHost)

        bufferView = findViewById(R.id.controller_buffering)
        playerView.resizeMode = RESIZE_MODE_FIXED_WIDTH

        findViewById<ImageButton>(R.id.controller_informationButton).setOnClickListener {
            displayInformation()
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

    private fun displayInformation() {
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

        informationDialog_titleTextView.text = "OK!"

        informationDialog.setOnClickListener {
            if (it.parent is ViewGroup) {
                (it.parent as ViewGroup).removeView(it)
            }
        }
    }

    private fun initMlsTimeBar() {
        val mlsTimeBar = findViewById<MLSTimeBar>(R.id.exo_progress)
        mlsTimeBar.setPlayedColor(Color.BLUE)
        val highlightMarkerTextView =
            findViewById<HighlightMarker>(R.id.exo_highlight_marker_title_highlight_marker)
        highlightMarkerTextView.initialize(Color.BLUE)

        val highlightMarkerManager = HighlightMarkerManager(mlsTimeBar, highlightMarkerTextView)

        val greenPointOfInterestType = PointOfInterestType(Color.GREEN)
        val redPointOfInterestType = PointOfInterestType(Color.RED)

        highlightMarkerManager.addTimeLineHighlight(
            PointOfInterest(
                360 * 1000L,
                listOf("Goal!"),
                redPointOfInterestType
            )
        )
        highlightMarkerManager.addTimeLineHighlight(
            PointOfInterest(
                900 * 1000L,
                listOf("Goal!", "Card"),
                greenPointOfInterestType
            )
        )
        highlightMarkerManager.addTimeLineHighlight(
            PointOfInterest(
                3600 * 1000L,
                listOf("Goal!"),
                redPointOfInterestType
            )
        )

        highlightMarkerManager.addTimeLineHighlight(
            PointOfInterest(
                4600 * 1000L,
                listOf("Again Goal!", "Offside", "Card"),
                redPointOfInterestType
            )
        )


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


    fun showOverLay(action: OverLayAction) {
        playerView.hideController()
        when (action.layoutType) {
            LayoutType.BASIC_SINGLE_LINE -> {
                showBasicSingleLine(action)
            }
            LayoutType.BASIC_DOUBLE_LINE -> {
                showBasicDoubleLine(action)
            }
            LayoutType.BASIC_SCORE_BOARD -> {
                showBasicScoreBoard(action)
            }
        }
    }

    private fun showBasicSingleLine(action: OverLayAction) {

        val textView = TextView(context)
        textView.id = View.generateViewId()
        textView.text = "126723452673452736482734823642"

//        playerView.addView(textView)

        val overlay: BasicSingleLineOverlayView

        if (isRenewedOverlay(action.layoutType)) {
            overlay = updateBasicSingleLineOverLayView(action)
        } else {
            overlay = createBasicSingleLineOverLayView(action)
            attachStartingAnimation(overlay, action.layoutPosition)
        }


        if (action.sticky) {
            overlaySingleLineHashMap[action.layoutType] = overlay
        } else {
            when (action.layoutPosition) {
                LayoutPosition.TOP_LEFT,
                LayoutPosition.BOTTOM_LEFT -> {
                    overlay.dismissIn(dismissingHandler, action.duration)
                }
                LayoutPosition.BOTTOM_RIGHT,
                LayoutPosition.TOP_RIGHT -> {
                    overlay.dismissToRightIn(dismissingHandler, action.duration)
                }
            }
        }


    }

    private fun updateBasicSingleLineOverLayView(action: OverLayAction): BasicSingleLineOverlayView {
        val overlayView = overlaySingleLineHashMap[action.layoutType]!!

        overlayView.setData(action)

        return overlayView
    }


    private fun createBasicSingleLineOverLayView(action: OverLayAction): BasicSingleLineOverlayView {
        val overlayView = BasicSingleLineOverlayView(context)
        overlayView.setData(action)

        overlayView.id = View.generateViewId()
//        overlayView.visibility = View.INVISIBLE


        when (action.layoutPosition) {
            LayoutPosition.TOP_LEFT -> {
                val layoutParams = RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP)
                overlayHost.addView(overlayView, layoutParams)
            }
            LayoutPosition.TOP_RIGHT -> {
                val layoutParams = RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP)
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
                overlayHost.addView(overlayView, layoutParams)
            }
            LayoutPosition.BOTTOM_RIGHT -> {
                val layoutParams = RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
                overlayHost.addView(overlayView, layoutParams)
            }
            LayoutPosition.BOTTOM_LEFT -> {
                val layoutParams = RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
                overlayHost.addView(overlayView, layoutParams)
            }
        }

        return overlayView

    }

    private fun attachStartingAnimation(
        overlayView: BasicSingleLineOverlayView,
        layoutPosition: LayoutPosition
    ) {
        overlayView.post {
            var animation: ObjectAnimator? = null

            overlayView.updateLayoutParams<RelativeLayout.LayoutParams> {
                when (layoutPosition) {
                    LayoutPosition.TOP_LEFT -> {
                        marginStart = overlayView.width.unaryMinus()
                        animation = ObjectAnimator.ofFloat(
                            overlayView,
                            "translationX",
                            overlayView.width.toFloat()
                        )
                    }
                    LayoutPosition.TOP_RIGHT -> {
                        val x = overlayView.width.toFloat()
                        marginEnd = overlayView.width.unaryMinus()

                        animation = ObjectAnimator.ofFloat(
                            overlayView,
                            "translationX",
                            x.unaryMinus()
                        )

                    }
                    LayoutPosition.BOTTOM_RIGHT -> {
                        val x = overlayView.width.toFloat()
                        marginEnd = overlayView.width.unaryMinus()
                        animation = ObjectAnimator.ofFloat(
                            overlayView,
                            "translationX",
                            x.unaryMinus()
                        )
                    }
                    LayoutPosition.BOTTOM_LEFT -> {
                        marginStart = overlayView.width.unaryMinus()
                        animation = ObjectAnimator.ofFloat(
                            overlayView,
                            "translationX",
                            overlayView.width.toFloat()
                        )
                    }
                }
            }


            animation?.duration = 1000L
            animation?.addListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator?) {
                }

                override fun onAnimationEnd(animation: Animator?, isReverse: Boolean) {
                }

                override fun onAnimationEnd(animation: Animator?) {
                }

                override fun onAnimationCancel(animation: Animator?) {
                }

                override fun onAnimationStart(animation: Animator?, isReverse: Boolean) {
                    overlayView.visibility = View.VISIBLE
                    overlayView.bringToFront()

                }

                override fun onAnimationStart(animation: Animator?) {
                }
            })
            animation?.start()

        }

    }


    private fun attachStartingAnimation(
        overlayView: BasicDoubleLineOverlayView,
        layoutPosition: LayoutPosition
    ) {
        overlayView.post {
            var animation: ObjectAnimator? = null

            overlayView.updateLayoutParams<RelativeLayout.LayoutParams> {
                when (layoutPosition) {
                    LayoutPosition.TOP_LEFT -> {
                        marginStart = overlayView.width.unaryMinus()
                        animation = ObjectAnimator.ofFloat(
                            overlayView,
                            "translationX",
                            overlayView.width.toFloat()
                        )
                    }
                    LayoutPosition.TOP_RIGHT -> {
                        val x = overlayView.width.toFloat()
                        marginEnd = overlayView.width.unaryMinus()

                        animation = ObjectAnimator.ofFloat(
                            overlayView,
                            "translationX",
                            x.unaryMinus()
                        )

                    }
                    LayoutPosition.BOTTOM_RIGHT -> {
                        val x = overlayView.width.toFloat()
                        marginEnd = overlayView.width.unaryMinus()
                        animation = ObjectAnimator.ofFloat(
                            overlayView,
                            "translationX",
                            x.unaryMinus()
                        )
                    }
                    LayoutPosition.BOTTOM_LEFT -> {
                        marginStart = overlayView.width.unaryMinus()
                        animation = ObjectAnimator.ofFloat(
                            overlayView,
                            "translationX",
                            overlayView.width.toFloat()
                        )
                    }
                }
            }


            animation?.duration = 1000L
            animation?.addListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator?) {
                }

                override fun onAnimationEnd(animation: Animator?, isReverse: Boolean) {
                }

                override fun onAnimationEnd(animation: Animator?) {
                }

                override fun onAnimationCancel(animation: Animator?) {
                }

                override fun onAnimationStart(animation: Animator?, isReverse: Boolean) {
                    overlayView.visibility = View.VISIBLE
                    overlayView.bringToFront()

                }

                override fun onAnimationStart(animation: Animator?) {
                }
            })
            animation?.start()

        }

    }

    private fun isRenewedOverlay(id: LayoutType): Boolean {
        return overlaySingleLineHashMap.containsKey(id) || overlayDoubleLineHashMap.containsKey(id)
    }

    private fun showBasicDoubleLine(action: OverLayAction) {
        val overlay: BasicDoubleLineOverlayView

        if (isRenewedOverlay(action.layoutType)) {
            overlay = updateBasicDoubleLineOverLayView(action)
        } else {
            overlay = createBasicDoubleLineOverLayView(action)
            attachStartingAnimation(overlay, action.layoutPosition)
        }

        if (action.sticky) {
            overlayDoubleLineHashMap[action.layoutType] = overlay
        } else {
            when (action.layoutPosition) {
                LayoutPosition.TOP_LEFT,
                LayoutPosition.BOTTOM_LEFT -> {
                    overlay.dismissIn(dismissingHandler, action.duration)
                }
                LayoutPosition.BOTTOM_RIGHT,
                LayoutPosition.TOP_RIGHT -> {
                    overlay.dismissToRightIn(dismissingHandler, action.duration)
                }
            }
        }

    }

    private fun createBasicDoubleLineOverLayView(action: OverLayAction): BasicDoubleLineOverlayView {
        val overlayView = BasicDoubleLineOverlayView(context)
        overlayView.setData(action)

        overlayView.id = View.generateViewId()
        overlayView.visibility = View.INVISIBLE

        when (action.layoutPosition) {
            LayoutPosition.TOP_LEFT -> {
                val layoutParams = RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP)
                overlayHost.addView(overlayView, layoutParams)
            }
            LayoutPosition.TOP_RIGHT -> {
                val layoutParams = RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP)
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
                overlayHost.addView(overlayView, layoutParams)
            }
            LayoutPosition.BOTTOM_RIGHT -> {
                val layoutParams = RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
                overlayHost.addView(overlayView, layoutParams)
            }
            LayoutPosition.BOTTOM_LEFT -> {
                val layoutParams = RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
                overlayHost.addView(overlayView, layoutParams)
            }
        }

        return overlayView
    }

    private fun updateBasicDoubleLineOverLayView(action: OverLayAction): BasicDoubleLineOverlayView {
        val overlayView = overlayDoubleLineHashMap[action.layoutType]!!

        overlayView.setData(action)

        return overlayView
    }

    private fun showBasicScoreBoard(action: OverLayAction) {
        val overlayView = BasicScoreBoardOverlayView(context)
        overlayView.setData(action)

        overlayView.id = View.generateViewId()
        overlayView.visibility = View.INVISIBLE

        val constraintSetForAnimatingView = ConstraintSet()
        constraintSetForAnimatingView.clone(this)

        when (action.layoutPosition) {
            LayoutPosition.TOP_LEFT -> {
                val layoutParams = RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP)
                overlayHost.addView(overlayView, layoutParams)
            }
            LayoutPosition.TOP_RIGHT -> {
                //todo
            }
            LayoutPosition.BOTTOM_RIGHT -> {
                //todo
            }
            LayoutPosition.BOTTOM_LEFT -> {
                val layoutParams = RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
                overlayHost.addView(overlayView, layoutParams)
            }
        }

        constraintSetForAnimatingView.applyTo(this)


        overlayView.post {
            overlayView.updateLayoutParams<RelativeLayout.LayoutParams> {
                marginStart = overlayView.width.unaryMinus()
            }

            val animation = ObjectAnimator.ofFloat(
                overlayView,
                "translationX",
                overlayView.width.toFloat()
            )
            animation.duration = 1000L
            animation.addListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator?) {
                }

                override fun onAnimationEnd(animation: Animator?, isReverse: Boolean) {
                }

                override fun onAnimationEnd(animation: Animator?) {
                }

                override fun onAnimationCancel(animation: Animator?) {
                }

                override fun onAnimationStart(animation: Animator?, isReverse: Boolean) {
                    overlayView.visibility = View.VISIBLE
                    overlayView.bringToFront()

                }

                override fun onAnimationStart(animation: Animator?) {
                }
            })
            animation.start()

            overlayView.dismissIn(dismissingHandler, action.duration)
        }

    }

    fun showAnnouncementOverLay(action: ShowAnnouncementOverlayAction) {
        val announcementOverlayView = AnnouncementOverlayView(context)
        announcementOverlayView.id = View.generateViewId()
        announcementOverlayView.viewAction(action)

        viewIdentifierManager.storeViewId(announcementOverlayView, action.viewId)

        if (action.dismissible) {

            idlingResource.increment()
            OverlayViewHelper.removeInFuture(
                overlayHost,
                announcementOverlayView,
                action.dismissIn,
                idlingResource
            )
        }

        idlingResource.increment()
        OverlayViewHelper.addView(
            overlayHost,
            announcementOverlayView,
            action.position,
            idlingResource
        )

    }

    fun showScoreboardOverlay(action: ShowScoreboardOverlayAction) {
        val scoreboardOverlayView = ScoreboardOverlayView(context)
        scoreboardOverlayView.id = View.generateViewId()
        scoreboardOverlayView.viewAction(action)

        viewIdentifierManager.storeViewId(scoreboardOverlayView, action.viewId)

//        if (action.dismissible) {
//            idlingResource.increment()
//            OverlayViewHelper.removeInFuture(
//                overlayHost,
//                scoreboardOverlayView,
//                action.dismissIn,
//                idlingResource
//            )
//        }

        idlingResource.increment()
        OverlayViewHelper.addView(
            overlayHost,
            scoreboardOverlayView,
            action.position,
            idlingResource
        )
    }

    fun hideOverlay(viewId: String) {
        viewIdentifierManager.getViewIdentifier(viewId)?.let {
            findViewById<ViewGroup>(it)?.visibility = View.INVISIBLE

        }
    }

    fun removeOverlay(viewId: String) {
        viewIdentifierManager.getViewIdentifier(viewId)?.let {
            findViewById<ViewGroup>(it)?.let { overlayView -> overlayHost.removeView(overlayView) }
        }
    }


    fun executeCommand(commandAction: CommandAction) {
        idlingResource.increment()
        OverlayCommandHelper.executeInFuture(
            overlayHost,
            commandAction,
            viewIdentifierManager.getViewIdentifier(commandAction.targetViewId),
            idlingResource
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
            val highlightMarkerTextView =
                findViewById<HighlightMarker>(R.id.exo_highlight_marker_title_highlight_marker)
            highlightMarkerTextView.initialize(secondaryColor)

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
                findViewById<ImageButton>(R.id.controller_informationButton).visibility =
                    View.VISIBLE
            } else {
                findViewById<ImageButton>(R.id.controller_informationButton).visibility = View.GONE
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
            val svg = SVG.getFromInputStream(overlayEntity.svgInputStream)
            svg.setDocumentWidth("100%")
            svg.setDocumentHeight("100%")
            proportionalImageView.setSVG(svg)

            OverlayViewHelper.addView(overlayHost, proportionalImageView, overlayEntity.size, idlingResource)

            viewIdentifierManager.storeViewId(proportionalImageView, overlayEntity.customId!!)

        } catch (e: Exception) {
            Log.e("PlayerView", "e: ${e.message}")
        }
    }

    fun hideOverlay(hideOverlayActionEntity: HideOverlayActionEntity) {
        idlingResource.increment()
        OverlayCommandHelper.removeView(
            overlayHost,
            viewIdentifierManager.getViewIdentifier(hideOverlayActionEntity.customId!!),
            idlingResource
        )
    }


    fun getTimeSvgString(): String {
        return "<svg height=\"30\" width=\"200\"><rect width=\"200\" height=\"30\" style=\"fill:rgb(211,211,211);stroke-width:3;stroke:rgb(128, 128, 128)\" /><text x=\"0\" y=\"15\" fill=\"red\">Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.</text></svg>"
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