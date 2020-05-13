package tv.mycujoo.mls.widgets

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Context
import android.os.Handler
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.RelativeLayout.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.updateLayoutParams
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.PlayerView
import kotlinx.android.synthetic.main.player_widget_layout.view.*
import tv.mycujoo.mls.R
import tv.mycujoo.mls.entity.LayoutPosition
import tv.mycujoo.mls.entity.LayoutPosition.*
import tv.mycujoo.mls.entity.LayoutType
import tv.mycujoo.mls.entity.LayoutType.*
import tv.mycujoo.mls.entity.OverLayAction
import tv.mycujoo.mls.extensions.getDisplaySize
import tv.mycujoo.mls.widgets.PlayerWidget.ScreenMode.LANDSCAPE
import tv.mycujoo.mls.widgets.PlayerWidget.ScreenMode.PORTRAIT


class PlayerWidget @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), PlayerWidgetInterface {

    init {
        init(context, attrs, defStyleAttr)
    }


    private lateinit var playerView: PlayerView
    private lateinit var overlayHost: OverlayHost

    private var dismissingHandler = Handler()

    private val overlaySingleLineHashMap = HashMap<LayoutType, BasicSingleLineOverlayView>()
    private val overlayDoubleLineHashMap = HashMap<LayoutType, BasicDoubleLineOverlayView>()


    private fun init(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) {


        val inflate =
            LayoutInflater.from(context).inflate(R.layout.player_widget_layout, this, true)

//        playerView = playerWidget_playerView
        playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH

//        playerView = PlayerView(context)
//        playerView.id = View.generateViewId()
//        playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH
//        addView(playerView)

        overlayHost = OverlayHost(context)
        overlayHost.id = View.generateViewId()
//        addView(overlayHost)


        val constraintSet = ConstraintSet()
        constraintSet.clone(this)


//        constraintSet.connect(playerView.id, ConstraintSet.TOP, id, ConstraintSet.TOP)
//        constraintSet.connect(playerView.id, ConstraintSet.START, id, ConstraintSet.START)
//        constraintSet.connect(playerView.id, ConstraintSet.END, id, ConstraintSet.END)
//
//        constraintSet.constrainHeight(id, ConstraintSet.WRAP_CONTENT)


        constraintSet.connect(overlayHost.id, ConstraintSet.TOP, id, ConstraintSet.TOP)
        constraintSet.connect(overlayHost.id, ConstraintSet.START, id, ConstraintSet.START)
        constraintSet.connect(overlayHost.id, ConstraintSet.END, id, ConstraintSet.END)
        constraintSet.connect(overlayHost.id, ConstraintSet.BOTTOM, id, ConstraintSet.BOTTOM)
        constraintSet.constrainWidth(overlayHost.id, 0)
        constraintSet.constrainHeight(overlayHost.id, 0)


//        constraintSet.applyTo(this)

//        screenMode(PORTRAIT)

        //todo uncomment this
        overlayHost.visibility = View.GONE

    }

    fun screenMode(screenMode: ScreenMode) {
        when (screenMode) {
            PORTRAIT -> {
                val constraintSet = ConstraintSet()
                constraintSet.clone(this)

                val displaySize = context.getDisplaySize()
                constraintSet.constrainWidth(playerView.id, ConstraintSet.MATCH_CONSTRAINT)
                constraintSet.constrainHeight(playerView.id, displaySize.width * 9 / 16)

                constraintSet.applyTo(this)

            }
            LANDSCAPE -> {
                val constraintSet = ConstraintSet()
                constraintSet.clone(this)

                constraintSet.constrainWidth(playerView.id, ConstraintSet.MATCH_CONSTRAINT)
                constraintSet.constrainHeight(playerView.id, ConstraintSet.WRAP_CONTENT)

                constraintSet.applyTo(this)
            }
        }
    }

    enum class ScreenMode {
        PORTRAIT,
        LANDSCAPE
    }

    fun addMarker(longArray: LongArray, booleanArray: BooleanArray) {
        playerView.setExtraAdGroupMarkers(
            longArray,
            booleanArray
        )
    }

    fun setPlayer(player: Player?) {
        playerView.player = player
    }

    override fun setPlayerControllerState(state: Boolean) {
        playerView.hideController()
        if (state) {
            playerView.hideController()
        } else {
            playerView.showController()
        }
    }


    private fun hideController() {
        playerView.hideController()
    }

    fun defaultController(hasDefaultPlayerController: Boolean) {
        playerView.useController = hasDefaultPlayerController
    }

    fun showOverLay(action: OverLayAction) {
        hideController()
        when (action.layoutType) {
            BASIC_SINGLE_LINE -> {
                showBasicSingleLine(action)
            }
            BASIC_DOUBLE_LINE -> {
                showBasicDoubleLine(action)
            }
            BASIC_SCORE_BOARD -> {
                showBasicScoreBoard(action)
            }
        }
    }

    private fun showBasicSingleLine(action: OverLayAction) {

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
                TOP_LEFT,
                BOTTOM_LEFT -> {
                    overlay.dismissIn(dismissingHandler, action.duration)
                }
                BOTTOM_RIGHT,
                TOP_RIGHT -> {
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
        overlayView.visibility = View.INVISIBLE


        when (action.layoutPosition) {
            TOP_LEFT -> {
                val layoutParams = RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                layoutParams.addRule(ALIGN_PARENT_TOP)
                overlayHost.addView(overlayView, layoutParams)
            }
            TOP_RIGHT -> {
                val layoutParams = RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                layoutParams.addRule(ALIGN_PARENT_TOP)
                layoutParams.addRule(ALIGN_PARENT_RIGHT)
                overlayHost.addView(overlayView, layoutParams)
            }
            BOTTOM_RIGHT -> {
                val layoutParams = RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                layoutParams.addRule(ALIGN_PARENT_BOTTOM)
                layoutParams.addRule(ALIGN_PARENT_RIGHT)
                overlayHost.addView(overlayView, layoutParams)
            }
            BOTTOM_LEFT -> {
                val layoutParams = RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                layoutParams.addRule(ALIGN_PARENT_BOTTOM)
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
                    TOP_LEFT -> {
                        marginStart = overlayView.width.unaryMinus()
                        animation = ObjectAnimator.ofFloat(
                            overlayView,
                            "translationX",
                            overlayView.width.toFloat()
                        )
                    }
                    TOP_RIGHT -> {
                        val x = overlayView.width.toFloat()
                        marginEnd = overlayView.width.unaryMinus()

                        animation = ObjectAnimator.ofFloat(
                            overlayView,
                            "translationX",
                            x.unaryMinus()
                        )

                    }
                    BOTTOM_RIGHT -> {
                        val x = overlayView.width.toFloat()
                        marginEnd = overlayView.width.unaryMinus()
                        animation = ObjectAnimator.ofFloat(
                            overlayView,
                            "translationX",
                            x.unaryMinus()
                        )
                    }
                    BOTTOM_LEFT -> {
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
                    TOP_LEFT -> {
                        marginStart = overlayView.width.unaryMinus()
                        animation = ObjectAnimator.ofFloat(
                            overlayView,
                            "translationX",
                            overlayView.width.toFloat()
                        )
                    }
                    TOP_RIGHT -> {
                        val x = overlayView.width.toFloat()
                        marginEnd = overlayView.width.unaryMinus()

                        animation = ObjectAnimator.ofFloat(
                            overlayView,
                            "translationX",
                            x.unaryMinus()
                        )

                    }
                    BOTTOM_RIGHT -> {
                        val x = overlayView.width.toFloat()
                        marginEnd = overlayView.width.unaryMinus()
                        animation = ObjectAnimator.ofFloat(
                            overlayView,
                            "translationX",
                            x.unaryMinus()
                        )
                    }
                    BOTTOM_LEFT -> {
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
                TOP_LEFT,
                BOTTOM_LEFT -> {
                    overlay.dismissIn(dismissingHandler, action.duration)
                }
                BOTTOM_RIGHT,
                TOP_RIGHT -> {
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
            TOP_LEFT -> {
                val layoutParams = RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                layoutParams.addRule(ALIGN_PARENT_TOP)
                overlayHost.addView(overlayView, layoutParams)
            }
            TOP_RIGHT -> {
                val layoutParams = RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                layoutParams.addRule(ALIGN_PARENT_TOP)
                layoutParams.addRule(ALIGN_PARENT_RIGHT)
                overlayHost.addView(overlayView, layoutParams)
            }
            BOTTOM_RIGHT -> {
                val layoutParams = RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                layoutParams.addRule(ALIGN_PARENT_BOTTOM)
                layoutParams.addRule(ALIGN_PARENT_RIGHT)
                overlayHost.addView(overlayView, layoutParams)
            }
            BOTTOM_LEFT -> {
                val layoutParams = RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                layoutParams.addRule(ALIGN_PARENT_BOTTOM)
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
            TOP_LEFT -> {
                val layoutParams = RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                layoutParams.addRule(ALIGN_PARENT_TOP)
                overlayHost.addView(overlayView, layoutParams)
            }
            TOP_RIGHT -> {
                //todo
            }
            BOTTOM_RIGHT -> {
                //todo
            }
            BOTTOM_LEFT -> {
                val layoutParams = RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                layoutParams.addRule(ALIGN_PARENT_BOTTOM)
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

                }

                override fun onAnimationStart(animation: Animator?) {
                }
            })
            animation.start()

            overlayView.dismissIn(dismissingHandler, action.duration)
        }

    }

}