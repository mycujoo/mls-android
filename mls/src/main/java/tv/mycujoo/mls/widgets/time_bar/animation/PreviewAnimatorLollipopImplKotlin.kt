package tv.mycujoo.mls.widgets.time_bar.animation

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.TargetApi
import android.os.Build
import android.util.Log
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.widget.FrameLayout
import tv.mycujoo.mls.widgets.time_bar.PreviewAnimator
import tv.mycujoo.mls.widgets.time_bar.PreviewView

internal class PreviewAnimatorLollipopImplKotlin(
    parent: ViewGroup,
    previewView: PreviewView,
    morphView: View,
    previewFrameLayout: FrameLayout,
    previewFrameView: View
) : PreviewAnimator(parent, previewView, morphView, previewFrameLayout, previewFrameView) {
    private val showListener: Animator.AnimatorListener =
        object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                morphView.animate().setListener(null)
                startReveal()
                mShowing = false
            }
        }
    private val hideListener: Animator.AnimatorListener =
        object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                morphView.visibility = View.INVISIBLE
                morphView.animate().setListener(null)
            }
        }
    private var mShowing = false

    override fun move() {
        previewFrameLayout.x = getFrameX()
        morphView.animate().x(if (mShowing) morphEndX else morphStartX)
        Log.d(
            "PreviewAnimatorLol",
            "getFrameX() : \${getFrameX()}, (\$mShowing, \$morphEndX OR \$morphStartX"
        )

    }

    override fun show() {
        mShowing = true
        move()
        previewFrameLayout.visibility = View.INVISIBLE
        previewFrameView.visibility = View.INVISIBLE
        morphView.y = (previewView as View).y
        morphView.x = morphStartX
        morphView.scaleX = 0f
        morphView.scaleY = 0f
        morphView.visibility = View.VISIBLE
        morphView.animate()
            .x(morphEndX)
            .y(morphEndY)
            .scaleY(4.0f)
            .scaleX(4.0f)
            .setDuration(MORPH_MOVE_DURATION.toLong())
            .setInterpolator(AccelerateDecelerateInterpolator())
            .setListener(showListener)
    }

    override fun hide() {
        mShowing = false
        previewFrameView.visibility = View.VISIBLE
        previewFrameLayout.visibility = View.VISIBLE
        morphView.x = morphEndX
        morphView.y = morphEndY
        morphView.scaleX = 4.0f
        morphView.scaleY = 4.0f
        morphView.visibility = View.INVISIBLE
        morphView.animate().cancel()
        previewFrameLayout.animate().cancel()
        startUnreveal()
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun startReveal() {
        val animator = ViewAnimationUtils.createCircularReveal(
            previewFrameLayout,
            getCenterX(previewFrameLayout),
            getCenterY(previewFrameLayout),
            morphView.width * 2.toFloat(),
            getRadius(previewFrameLayout).toFloat()
        )
        animator.setTarget(previewFrameLayout)
        animator.duration = MORPH_REVEAL_DURATION.toLong()
        animator.interpolator = AccelerateInterpolator()
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator) {
                super.onAnimationStart(animation)
                previewFrameView.alpha = 1f
                previewFrameLayout.visibility = View.VISIBLE
                previewFrameView.visibility = View.VISIBLE
                morphView.visibility = View.INVISIBLE
                previewFrameView.animate()
                    .alpha(0f).duration = MORPH_REVEAL_DURATION.toLong()
            }

            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                previewFrameLayout.animate().setListener(null)
                previewFrameView.visibility = View.INVISIBLE
            }
        })
        animator.start()
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun startUnreveal() {
        val animator = ViewAnimationUtils.createCircularReveal(
            previewFrameLayout,
            getCenterX(previewFrameLayout),
            getCenterY(previewFrameLayout),
            getRadius(previewFrameLayout).toFloat(), morphView.width * 2.toFloat()
        )
        animator.setTarget(previewFrameLayout)
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                previewFrameLayout.animate().setListener(null)
                previewFrameView.visibility = View.INVISIBLE
                previewFrameLayout.visibility = View.INVISIBLE
                morphView.visibility = View.VISIBLE
                morphView.x = morphEndX
                morphView.animate()
                    .x(morphStartX)
                    .y(morphStartY)
                    .scaleY(0f)
                    .scaleX(0f)
                    .setDuration(UNMORPH_MOVE_DURATION.toLong())
                    .setInterpolator(AccelerateInterpolator())
                    .setListener(hideListener)
            }
        })
        previewFrameView.animate().alpha(1f)
            .setDuration(UNMORPH_UNREVEAL_DURATION.toLong()).interpolator = AccelerateInterpolator()
        animator.setDuration(UNMORPH_UNREVEAL_DURATION.toLong()).interpolator =
            AccelerateInterpolator()
        animator.start()
    }

    private fun getRadius(view: View): Int {
        return Math.hypot(
            view.width / 2.toDouble(),
            view.height / 2.toDouble()
        ).toInt()
    }

    private fun getCenterX(view: View): Int {
        return view.width / 2
    }

    private fun getCenterY(view: View): Int {
        return view.height / 2
    }

    /**
     * Get the x position for the view that'll morph into the preview FrameLayout
     */
    private val morphStartX: Float
        private get() {
            val startX = getPreviewViewStartX() + previewView.getThumbOffset()
            val endX = getPreviewViewEndX() - previewView.getThumbOffset()
            val nextX = (endX - startX) * getWidthOffset(previewView.getProgress())
            +startX - previewView.getThumbOffset()
            return nextX
        }

    private val morphEndX: Float
        private get() = getFrameX() + previewFrameLayout.width / 2f - previewView.getThumbOffset()

    private val morphStartY: Float
        private get() = (previewView as View).y + previewView.getThumbOffset()

    private val morphEndY: Float
        private get() = (previewFrameLayout.y + previewFrameLayout.height / 2f).toInt().toFloat()

    companion object {
        const val MORPH_REVEAL_DURATION = 150
        const val MORPH_MOVE_DURATION = 200
        const val UNMORPH_MOVE_DURATION = 200
        const val UNMORPH_UNREVEAL_DURATION = 150
    }
}