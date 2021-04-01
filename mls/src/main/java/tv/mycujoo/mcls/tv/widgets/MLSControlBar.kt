package tv.mycujoo.mcls.tv.widgets

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import java.util.*

class MLSControlBar : LinearLayout {
    interface OnChildFocusedListener {
        fun onChildFocusedListener(child: View?, focused: View?)
    }

    private var mChildMarginFromCenter = 0
    private var mOnChildFocusedListener: OnChildFocusedListener? = null
    var mLastFocusIndex = -1
    var mDefaultFocusToMiddle = true

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {}
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {}

    fun setDefaultFocusToMiddle(defaultFocusToMiddle: Boolean) {
        mDefaultFocusToMiddle = defaultFocusToMiddle
    }

    val defaultFocusIndex: Int
        get() = if (mDefaultFocusToMiddle) childCount / 2 else 0

    override fun onRequestFocusInDescendants(direction: Int, previouslyFocusedRect: Rect?): Boolean {
        if (childCount > 0) {
            val index = if (mLastFocusIndex >= 0 && mLastFocusIndex < childCount) mLastFocusIndex else defaultFocusIndex
            if (getChildAt(index).requestFocus(direction, previouslyFocusedRect)) {
                return true
            }
        }
        return super.onRequestFocusInDescendants(direction, previouslyFocusedRect)
    }

    override fun addFocusables(views: ArrayList<View>, direction: Int, focusableMode: Int) {
        if (direction == FOCUS_UP || direction == FOCUS_DOWN) {
            if (mLastFocusIndex >= 0 && mLastFocusIndex < childCount) {
                views.add(getChildAt(mLastFocusIndex))
            } else if (childCount > 0) {
                views.add(getChildAt(defaultFocusIndex))
            }
        } else {
            super.addFocusables(views, direction, focusableMode)
        }
    }

    fun setOnChildFocusedListener(listener: OnChildFocusedListener?) {
        mOnChildFocusedListener = listener
    }

    fun setChildMarginFromCenter(marginFromCenter: Int) {
        mChildMarginFromCenter = marginFromCenter
    }

    override fun requestChildFocus(child: View, focused: View) {
        super.requestChildFocus(child, focused)
        mLastFocusIndex = indexOfChild(child)
        if (mOnChildFocusedListener != null) {
            mOnChildFocusedListener!!.onChildFocusedListener(child, focused)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (mChildMarginFromCenter <= 0) {
            return
        }
        var totalExtraMargin = 0
        for (i in 0 until childCount - 1) {
            val first = getChildAt(i)
            val second = getChildAt(i + 1)
            val measuredWidth = first.measuredWidth + second.measuredWidth
            val marginStart = mChildMarginFromCenter - measuredWidth / 2
            val lp = second.layoutParams as LayoutParams
            val extraMargin = marginStart - lp.marginStart
            lp.marginStart = marginStart
            second.layoutParams = lp
            totalExtraMargin += extraMargin
        }
        setMeasuredDimension(measuredWidth + totalExtraMargin, measuredHeight)
    }
}