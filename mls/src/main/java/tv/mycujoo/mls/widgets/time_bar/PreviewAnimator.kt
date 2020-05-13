package tv.mycujoo.mls.widgets.time_bar

import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.widget.FrameLayout

abstract class PreviewAnimator(
    var parent: ViewGroup,
    var previewView: PreviewView,
    var morphView: View,
    var previewFrameLayout: FrameLayout,
    var previewFrameView: View
) {


    abstract fun move()

    abstract fun show()

    abstract fun hide()


    /**
     * Get x position for the preview frame. This method takes into account a margin
     * that'll make the frame not move until the scrub position exceeds half of the frame's width.
     */
    open fun getFrameX(): Float {
        val params = previewFrameLayout.layoutParams as MarginLayoutParams
        val offset = getWidthOffset(previewView.getProgress())
        val low = previewFrameLayout.left.toFloat()
        val high =
            parent.width - params.rightMargin - previewFrameLayout.width.toFloat()
        val startX: Float = getPreviewViewStartX() + previewView.getThumbOffset()
        val endX: Float = getPreviewViewEndX() - previewView.getThumbOffset()
        val center = (endX - startX) * offset + startX
        val nextX = center - previewFrameLayout.width / 2f

        // Don't move if we still haven't reached half of the width
        return when {
            nextX < low -> {
                low
            }
            nextX > high -> {
                high
            }
            else -> {
                nextX
            }
        }
    }

    open fun getPreviewViewStartX(): Float {
        return (previewView as View).x
    }

    open fun getPreviewViewEndX(): Float {
        return getPreviewViewStartX() + (previewView as View).width
    }

    open fun getWidthOffset(progress: Int): Float {
        return progress.toFloat() / previewView.getMax()
    }

}