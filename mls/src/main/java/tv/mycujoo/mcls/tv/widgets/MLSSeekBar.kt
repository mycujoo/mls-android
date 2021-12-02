package tv.mycujoo.mcls.tv.widgets

import android.content.Context
import android.graphics.*
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import android.widget.SeekBar
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import tv.mycujoo.mcls.R
import tv.mycujoo.mcls.manager.SeekBarListener
import tv.mycujoo.mcls.widgets.mlstimebar.PointOfInterest
import tv.mycujoo.mcls.widgets.mlstimebar.PositionedPointOfInterest
import java.util.*

class MLSSeekBar(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    abstract class AccessibilitySeekListener {
        /**
         * Called to perform AccessibilityNodeInfoCompat.ACTION_SCROLL_FORWARD
         */
        abstract fun onAccessibilitySeekForward(): Boolean

        /**
         * Called to perform AccessibilityNodeInfoCompat.ACTION_SCROLL_BACKWARD
         */
        abstract fun onAccessibilitySeekBackward(): Boolean
    }

    private val mProgressRect = RectF()
    private val mSecondProgressRect = RectF()
    private val mBackgroundRect = RectF()
    private val mSecondProgressPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mProgressPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mBackgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mKnobPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mMarkerPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var mProgress = 0

    /**
     * Get secondary progress within 0 and [.getMax]
     */
    private var secondProgress = 0
        private set
    private var mMax = 0
    private var mKnobx = 0
    private var mActiveRadius: Int
    private var mBarHeight: Int
    private var mActiveBarHeight: Int
    private var mAccessibilitySeekListener: AccessibilitySeekListener? = null

    private var seekBarListener: SeekBarListener? = null
    private val poiArrayList = ArrayList<PointOfInterest>()
    private val poiPositionsOnScreen = ArrayList<PositionedPointOfInterest>()


    fun addTimeLineHighlight(poi: PointOfInterest) {
        poiArrayList.add(poi)
        poiPositionsOnScreen.add(PositionedPointOfInterest(-1, poi))
    }

    fun clearTimeLineMarker() {
        poiArrayList.clear()
        poiPositionsOnScreen.clear()
    }

    fun setSeekbarListener(seekBarListener: SeekBarListener) {
        this.seekBarListener = seekBarListener
    }

    /**
     * Set radius in pixels for thumb when SeekBar is focused.
     */
    fun setActiveRadius(radius: Int) {
        mActiveRadius = radius
        calculate()
    }

    /**
     * Set horizontal bar height in pixels when SeekBar is not focused.
     */
    fun setBarHeight(barHeight: Int) {
        mBarHeight = barHeight
        calculate()
    }

    /**
     * Set horizontal bar height in pixels when SeekBar is focused.
     */
    fun setActiveBarHeight(activeBarHeight: Int) {
        mActiveBarHeight = activeBarHeight
        calculate()
    }

    override fun onFocusChanged(
        gainFocus: Boolean,
        direction: Int, previouslyFocusedRect: Rect?
    ) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect)
        calculate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        calculate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val radius = if (isFocused) mActiveRadius else mBarHeight / 2
        canvas.drawRoundRect(mBackgroundRect, radius.toFloat(), radius.toFloat(), mBackgroundPaint)
        if (mSecondProgressRect.right > mSecondProgressRect.left) {
            canvas.drawRoundRect(
                mSecondProgressRect,
                radius.toFloat(),
                radius.toFloat(),
                mSecondProgressPaint
            )
        }
        canvas.drawRoundRect(mProgressRect, radius.toFloat(), radius.toFloat(), mProgressPaint)

        poiArrayList.forEachIndexed { index, poi ->
            val poiOffsetAdjusted: Long
            if (poi.offset + poi.seekOffset in 0L..totalTime) {
                poiOffsetAdjusted = poi.offset + poi.seekOffset
            } else {
                poiPositionsOnScreen[index].positionOnScreen = -1
                return
            }

            mMarkerPaint.color =
                Color.parseColor(poi.poiType.color)

            val markerPositionOffset: Int =
                (width * poiOffsetAdjusted / totalTime).toInt()

            val markerLeft = mProgressRect.left + (mBackgroundRect.width()
                .toInt() - 4).coerceAtMost(0.coerceAtLeast(markerPositionOffset)).toFloat()
            val markerRight = markerLeft + 4

            canvas.drawRect(
                markerLeft,
                mSecondProgressRect.top,
                markerRight,
                mSecondProgressRect.bottom,
                mMarkerPaint
            )

            poiPositionsOnScreen[index].positionOnScreen =
                ((markerLeft + markerRight) / 2).toInt()
        }

        canvas.drawCircle(mKnobx.toFloat(), height / 2.toFloat(), radius.toFloat(), mKnobPaint)
    }

    /**
     * Set secondary progress within 0 and [.getMax]
     */
    fun setSecondaryProgress(progress: Int) {
        var progress = progress
        if (progress > mMax) {
            progress = mMax
        } else if (progress < 0) {
            progress = 0
        }
        secondProgress = progress
        calculate()
    }

    /**
     * Get progress within 0 and [.getMax]
     * Set progress within 0 and [.getMax]
     */
    fun getProgress(): Int {
        return mProgress
    }

    fun setProgress(progress: Int) {
        var progress = progress
        if (progress > mMax) {
            progress = mMax
        } else if (progress < 0) {
            progress = 0
        }
        mProgress = progress
        calculate()
    }

    fun setCurrentTimeBySeek(pos: Long) {
        val ratio = pos.toDouble() / totalTime
        seekBarListener?.onSeekTo((ratio * mBackgroundRect.width()).toInt(), poiPositionsOnScreen)
    }
    /**
     * Get max value.
     */
    /**
     * Set max value.
     */
    var max: Int
        get() = mMax
        set(max) {
            mMax = max
            calculate()
        }

    private var totalTime: Long = 0L
    fun setTotalTime(totalTime: Long) {
        this.totalTime = totalTime
    }

    /**
     * Set color for progress.
     */
    fun setProgressColor(color: Int) {
        mProgressPaint.color = color
    }
    /**
     * Set color for second progress which is usually for buffering indication.
     */
    /**
     * Set color for second progress which is usually for buffering indication.
     */
    var secondaryProgressColor: Int
        get() = mSecondProgressPaint.color
        set(color) {
            mSecondProgressPaint.color = color
        }

    private fun calculate() {
        val barHeight = if (isFocused) mActiveBarHeight else mBarHeight
        val width = width
        val height = height
        val verticalPadding = (height - barHeight) / 2
        mBackgroundRect[mBarHeight / 2.toFloat(), verticalPadding.toFloat(), width - mBarHeight / 2.toFloat()] =
            height - verticalPadding.toFloat()
        val radius = if (isFocused) mActiveRadius else mBarHeight / 2
        val progressWidth = width - radius * 2
        val progressPixels = mProgress / mMax.toFloat() * progressWidth
        mProgressRect[mBarHeight / 2.toFloat(), verticalPadding.toFloat(), mBarHeight / 2 + progressPixels] =
            height - verticalPadding.toFloat()
        val secondProgressPixels = secondProgress / mMax.toFloat() * progressWidth
        mSecondProgressRect[mProgressRect.right, verticalPadding.toFloat(), mBarHeight / 2 + secondProgressPixels] =
            height - verticalPadding.toFloat()
        mKnobx = radius + progressPixels.toInt()
        invalidate()
    }

    override fun getAccessibilityClassName(): CharSequence {
        return SeekBar::class.java.name
    }

    fun setAccessibilitySeekListener(listener: AccessibilitySeekListener?) {
        mAccessibilitySeekListener = listener
    }

    override fun performAccessibilityAction(action: Int, arguments: Bundle): Boolean {
        if (mAccessibilitySeekListener != null) {
            when (action) {
                AccessibilityNodeInfoCompat.ACTION_SCROLL_FORWARD -> return mAccessibilitySeekListener!!.onAccessibilitySeekForward()
                AccessibilityNodeInfoCompat.ACTION_SCROLL_BACKWARD -> return mAccessibilitySeekListener!!.onAccessibilitySeekBackward()
            }
        }
        return super.performAccessibilityAction(action, arguments)
    }

    init {
        setWillNotDraw(false)
        mBackgroundPaint.color = Color.GRAY
        mSecondProgressPaint.color = Color.LTGRAY
        mProgressPaint.color = Color.RED
        mKnobPaint.color = Color.WHITE
        mMarkerPaint.color = Color.RED
        mBarHeight = context.resources.getDimensionPixelSize(
            R.dimen.lb_playback_transport_progressbar_bar_height
        )
        mActiveBarHeight = context.resources.getDimensionPixelSize(
            R.dimen.lb_playback_transport_progressbar_active_bar_height
        )
        mActiveRadius = context.resources.getDimensionPixelSize(
            R.dimen.lb_playback_transport_progressbar_active_radius
        )
    }
}