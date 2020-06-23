package tv.mycujoo.mls.widgets

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.Nullable
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.updateLayoutParams
import androidx.test.espresso.idling.CountingIdlingResource
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH
import com.google.android.exoplayer2.ui.PlayerView
import tv.mycujoo.mls.R
import tv.mycujoo.mls.core.UIEventListener
import tv.mycujoo.mls.entity.LayoutPosition
import tv.mycujoo.mls.entity.LayoutType
import tv.mycujoo.mls.entity.OverLayAction
import tv.mycujoo.mls.entity.actions.CommandAction
import tv.mycujoo.mls.entity.actions.ShowAnnouncementOverlayAction
import tv.mycujoo.mls.entity.actions.ShowScoreboardOverlayAction
import tv.mycujoo.mls.extensions.getDisplaySize
import tv.mycujoo.mls.helper.OverlayCommandHelper
import tv.mycujoo.mls.helper.OverlayViewHelper
import tv.mycujoo.mls.helper.TimeBarAnnotationHelper
import tv.mycujoo.mls.manager.HighlightMarkerManager
import tv.mycujoo.mls.manager.ViewIdentifierManager
import tv.mycujoo.mls.widgets.mlstimebar.HighlightMarker
import tv.mycujoo.mls.widgets.mlstimebar.MLSTimeBar
import tv.mycujoo.mls.widgets.mlstimebar.PointOfInterest
import tv.mycujoo.mls.widgets.mlstimebar.PointOfInterestType
import tv.mycujoo.mls.widgets.overlay.AnnouncementOverlayView
import tv.mycujoo.mls.widgets.overlay.ScoreboardOverlayView
import tv.mycujoo.mls.widgets.time_bar.PreviewTimeBarWrapper

class PlayerViewWrapper @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {


    /**region Fields*/
    var playerView: PlayerView
    private var overlayHost: OverlayHost

    lateinit var uiEventListener: UIEventListener
    private var isFullScreen = false
    private lateinit var fullScreenButton: ImageButton

    var previewTimeBarWrapper: PreviewTimeBarWrapper? = null
    private var imageView: ImageView? = null
    private val thumbnailsUrl: String =
        "https://bitdash-a.akamaihd.net/content/MI201109210084_1/thumbnails/f08e80da-bf1d-4e3d-8899-f0f6155f6efa.jpg"
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

        playerView.resizeMode = RESIZE_MODE_FIXED_WIDTH

        fullScreenButton = findViewById<ImageButton>(R.id.fullscreen)
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

//        previewTimeBarWrapper = findViewById(R.id.exo_progress)

//        imageView = findViewById(R.id.previewImageView)
//        previewTitleTextView = findViewById(R.id.previewTitleTextView)


//        previewTimeBarWrapper.delegate.setPreviewLoader(object : PreviewLoader {
//            override fun loadPreview(currentPosition: Long, max: Long) {
////                Glide.with(imageView!!)
////                    .load(thumbnailsUrl)
////                    .override(
////                        Target.SIZE_ORIGINAL,
////                        Target.SIZE_ORIGINAL
////                    )
////                    .into(imageView!!)
//
//                timeBarAnnotationHelper.updateText(currentPosition, previewTitleTextView)
//            }
//        })

//        playerView.post { screenMode(PlayerWidget.ScreenMode.PORTRAIT) }

        initMlsTimeBar()

    }

    private fun initMlsTimeBar() {
        val mlsTimeBar = findViewById<MLSTimeBar>(R.id.exo_progress)
        val highlightMarkerTextView =
            findViewById<HighlightMarker>(R.id.exo_highlight_marker_title_highlight_marker)

        val highlightMarkerManager = HighlightMarkerManager(mlsTimeBar, highlightMarkerTextView)

        val greenPointOfInterestType = PointOfInterestType(Color.GREEN)
        val redPointOfInterestType = PointOfInterestType(Color.RED)

        highlightMarkerManager.addTimeLineHighlight(
            PointOfInterest(
                900 * 1000L,
                "Goal!",
                greenPointOfInterestType
            )
        )
        highlightMarkerManager.addTimeLineHighlight(
            PointOfInterest(
                3600 * 1000L,
                "Goal!",
                redPointOfInterestType
            )
        )

        highlightMarkerManager.addTimeLineHighlight(
            PointOfInterest(
                360 * 1000L,
                "Goal!",
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


    /**endregion */

    enum class ScreenMode {
        PORTRAIT,
        LANDSCAPE
    }


}