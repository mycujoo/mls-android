package tv.mycujoo.mcls.widgets.mlstimebar

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.annotation.ColorInt
import androidx.annotation.RequiresApi
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ui.R
import com.google.android.exoplayer2.ui.TimeBar
import com.google.android.exoplayer2.ui.TimeBar.OnScrubListener
import com.google.android.exoplayer2.util.Assertions
import com.google.android.exoplayer2.util.Util
import timber.log.Timber
import java.lang.IllegalArgumentException
import java.util.*
import java.util.concurrent.CopyOnWriteArraySet

class MLSTimeBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    timebarAttrs: AttributeSet? = attrs
) : View(context, attrs, defStyleAttr), TimeBar {
    private val seekBounds: Rect = Rect()
    private val progressBar: Rect = Rect()
    private val bufferedBar: Rect = Rect()
    private val scrubberBar: Rect = Rect()
    private val playedPaint: Paint = Paint()
    private val bufferedPaint: Paint = Paint()
    private val unplayedPaint: Paint = Paint()
    private val adMarkerPaint: Paint = Paint()
    private val playedAdMarkerPaint: Paint = Paint()
    private val scrubberPaint: Paint = Paint()
    private var scrubberDrawable: Drawable? = null
    private var barHeight = 0
    private var touchTargetHeight = 0
    private var adMarkerWidth = 0
    private var scrubberEnabledSize = 0
    private var scrubberDisabledSize = 0
    private var scrubberDraggedSize = 0
    private var scrubberPadding = 0
    private val fineScrubYThreshold: Int
    private val formatBuilder: StringBuilder
    private val formatter: Formatter
    private val stopScrubbingRunnable: Runnable
    private val listeners: CopyOnWriteArraySet<OnScrubListener>
    private val locationOnScreen: IntArray
    private val touchPosition: Point
    private val density: Float
    private var keyCountIncrement: Int
    private var keyTimeIncrement: Long
    private var lastCoarseScrubXPosition = 0

    //    @MonotonicNonNull
    private var lastExclusionRectangle: Rect? = null
    private var scrubbing = false
    private var scrubPosition: Long = 0
    private var duration: Long
    private var position: Long = 0
    private var bufferedPosition: Long = 0
    private var adGroupCount = 0
    private var adGroupTimesMs: LongArray? = null
    private var playedAdGroups: BooleanArray? = null
    private val poiArrayList = ArrayList<PointOfInterest>()
    private val poiPositionsOnScreen = ArrayList<PositionedPointOfInterest>()
    private lateinit var timelineMarkerPositionListener: TimelineMarkerPosition


    /**
     * Sets the color for the portion of the time bar representing media before the playback position.
     *
     * @param playedColor The color for the portion of the time bar representing media before the
     * playback position.
     */
    fun setPlayedColor(@ColorInt playedColor: Int) {
        playedPaint.color = playedColor
        invalidate()
    }

    /**
     * Sets the color for the scrubber handle.
     *
     * @param scrubberColor The color for the scrubber handle.
     */
    fun setScrubberColor(@ColorInt scrubberColor: Int) {
        scrubberPaint.color = scrubberColor
        invalidate()
    }

    /**
     * Sets the color for the portion of the time bar after the current played position up to the
     * current buffered position.
     *
     * @param bufferedColor The color for the portion of the time bar after the current played
     * position up to the current buffered position.
     */
    fun setBufferedColor(@ColorInt bufferedColor: Int) {
        bufferedPaint.color = bufferedColor
        invalidate()
    }

    /**
     * Sets the color for the portion of the time bar after the current played position.
     *
     * @param unplayedColor The color for the portion of the time bar after the current played
     * position.
     */
    fun setUnplayedColor(@ColorInt unplayedColor: Int) {
        unplayedPaint.color = unplayedColor
        invalidate()
    }

    /**
     * Sets the color for unplayed ad markers.
     *
     * @param adMarkerColor The color for unplayed ad markers.
     */
    fun setAdMarkerColor(@ColorInt adMarkerColor: Int) {
        adMarkerPaint.color = adMarkerColor
        invalidate()
    }

    /**
     * Sets the color for played ad markers.
     *
     * @param playedAdMarkerColor The color for played ad markers.
     */
    fun setPlayedAdMarkerColor(@ColorInt playedAdMarkerColor: Int) {
        playedAdMarkerPaint.color = playedAdMarkerColor
        invalidate()
    }

    fun setTimelineMarkerPositionListener(listener: TimelineMarkerPosition) {
        timelineMarkerPositionListener = listener
    }

    // TimeBar implementation.
    override fun addListener(listener: OnScrubListener) {
        listeners.add(listener)
    }

    override fun removeListener(listener: OnScrubListener) {
        listeners.remove(listener)
    }

    override fun setKeyTimeIncrement(time: Long) {
        Assertions.checkArgument(time > 0)
        keyCountIncrement = C.INDEX_UNSET
        keyTimeIncrement = time
    }

    override fun setKeyCountIncrement(count: Int) {
        Assertions.checkArgument(count > 0)
        keyCountIncrement = count
        keyTimeIncrement = C.TIME_UNSET
    }

    override fun setPosition(position: Long) {
        this.position = position
        contentDescription = progressText

        update()
    }

    override fun setBufferedPosition(bufferedPosition: Long) {
        this.bufferedPosition = bufferedPosition
        update()
    }

    override fun setDuration(duration: Long) {
        this.duration = duration
        if (scrubbing && duration == C.TIME_UNSET) {
            stopScrubbing( /* canceled= */true)
        }
        update()
    }

    override fun getPreferredUpdateDelay(): Long {
        val timeBarWidthDp =
            pxToDp(
                density,
                progressBar.width()
            )
        return if (timeBarWidthDp == 0 || duration == 0L || duration == C.TIME_UNSET) Long.MAX_VALUE else duration / timeBarWidthDp
    }

    override fun setAdGroupTimesMs(
        adGroupTimesMs: LongArray?, playedAdGroups: BooleanArray?,
        adGroupCount: Int
    ) {
        Assertions.checkArgument(
            adGroupCount == 0
                    || adGroupTimesMs != null && playedAdGroups != null
        )
        this.adGroupCount = adGroupCount
        this.adGroupTimesMs = adGroupTimesMs
        this.playedAdGroups = playedAdGroups
        update()
    }

    fun addTimeLineHighlight(poi: PointOfInterest) {
        poiArrayList.add(poi)
        poiPositionsOnScreen.add(PositionedPointOfInterest(-1, poi))
    }

    fun clearTimeLineMarker() {
        poiArrayList.clear()
        poiPositionsOnScreen.clear()
    }

    // View methods.
    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        if (scrubbing && !enabled) {
            stopScrubbing( /* canceled= */true)
        }
    }

    public override fun onDraw(canvas: Canvas) {
        canvas.save()
        drawTimeBar(canvas)
        drawPlayhead(canvas)
        canvas.restore()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled || duration <= 0) {
            return false
        }

        val touchPosition = resolveRelativeTouchPosition(event)
        val x = touchPosition.x
        val y = touchPosition.y
        when (event.action) {
            MotionEvent.ACTION_DOWN -> if (isInSeekBar(event.x, event.y)) {
                positionScrubber(x.toFloat())
                startScrubbing(scrubberPosition)
                update()
                invalidate()
                return true
            }
            MotionEvent.ACTION_MOVE -> if (scrubbing) {
                if (y < fineScrubYThreshold) {
                    val relativeX = x - lastCoarseScrubXPosition
                    positionScrubber(lastCoarseScrubXPosition + relativeX / FINE_SCRUB_RATIO.toFloat())
                    notifyScrubbingUpdated(x.toFloat())
                } else {
                    lastCoarseScrubXPosition = x
                    positionScrubber(x.toFloat())
                    notifyScrubbingUpdated(x.toFloat())
                }
                updateScrubbing(scrubberPosition)
                update()
                invalidate()
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> if (scrubbing) {
                stopScrubbing( /* canceled= */event.action == MotionEvent.ACTION_CANCEL)
                return true
            }
            else -> {
            }
        }
        return false
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (isEnabled) {
            var positionIncrement = positionIncrement
            when (keyCode) {
                KeyEvent.KEYCODE_DPAD_LEFT -> {
                    positionIncrement = -positionIncrement
                    if (scrubIncrementally(positionIncrement)) {
                        removeCallbacks(stopScrubbingRunnable)
                        postDelayed(
                            stopScrubbingRunnable,
                            STOP_SCRUBBING_TIMEOUT_MS
                        )
                        return true
                    }
                }
                KeyEvent.KEYCODE_DPAD_RIGHT -> if (scrubIncrementally(positionIncrement)) {
                    removeCallbacks(stopScrubbingRunnable)
                    postDelayed(
                        stopScrubbingRunnable,
                        STOP_SCRUBBING_TIMEOUT_MS
                    )
                    return true
                }
                KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> if (scrubbing) {
                    stopScrubbing( /* canceled= */false)
                    return true
                }
                else -> {
                }
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onFocusChanged(
        gainFocus: Boolean, direction: Int, previouslyFocusedRect: Rect?
    ) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect)
        if (scrubbing && !gainFocus) {
            stopScrubbing( /* canceled= */false)
        }
    }

    override fun drawableStateChanged() {
        super.drawableStateChanged()
        updateDrawableState()
    }

    override fun jumpDrawablesToCurrentState() {
        super.jumpDrawablesToCurrentState()
        scrubberDrawable?.jumpToCurrentState()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val height =
            when (heightMode) {
                MeasureSpec.UNSPECIFIED -> touchTargetHeight
                MeasureSpec.EXACTLY -> heightSize
                else -> touchTargetHeight.coerceAtMost(heightSize)
            }
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), height)
        updateDrawableState()
    }

    override fun onLayout(
        changed: Boolean,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int
    ) {
        val width = right - left
        val height = bottom - top
        val barY = (height - touchTargetHeight) / 2
        val seekLeft = paddingLeft
        val seekRight = width - paddingRight
        val progressY = barY + (touchTargetHeight - barHeight) / 2
        seekBounds[seekLeft, barY, seekRight] = barY + touchTargetHeight
        progressBar[seekBounds.left + scrubberPadding, progressY, seekBounds.right - scrubberPadding] =
            progressY + barHeight
        if (Util.SDK_INT >= 29) {
            setSystemGestureExclusionRectsV29(width, height)
        }
        update()
    }

    override fun onRtlPropertiesChanged(layoutDirection: Int) {
        if (scrubberDrawable != null && setDrawableLayoutDirection(
                scrubberDrawable!!,
                layoutDirection
            )
        ) {
            invalidate()
        }
    }

    override fun onInitializeAccessibilityEvent(event: AccessibilityEvent) {
        super.onInitializeAccessibilityEvent(event)
        if (event.eventType == AccessibilityEvent.TYPE_VIEW_SELECTED) {
            event.text.add(progressText)
        }
        event.className =
            ACCESSIBILITY_CLASS_NAME
    }

    @TargetApi(21)
    override fun onInitializeAccessibilityNodeInfo(info: AccessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(info)
        info.className =
            ACCESSIBILITY_CLASS_NAME
        info.contentDescription = progressText
        if (duration <= 0) {
            return
        }
        if (Util.SDK_INT >= 21) {
            info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_FORWARD)
            info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_BACKWARD)
        } else {
            info.addAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
            info.addAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD)
        }
    }

    override fun performAccessibilityAction(action: Int, args: Bundle?): Boolean {
        if (super.performAccessibilityAction(action, args)) {
            return true
        }
        if (duration <= 0) {
            return false
        }
        if (action == AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD) {
            if (scrubIncrementally(-positionIncrement)) {
                stopScrubbing( /* canceled= */false)
            }
        } else if (action == AccessibilityNodeInfo.ACTION_SCROLL_FORWARD) {
            if (scrubIncrementally(positionIncrement)) {
                stopScrubbing( /* canceled= */false)
            }
        } else {
            return false
        }
        sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_SELECTED)
        return true
    }

    // Internal methods.
    private fun startScrubbing(scrubPosition: Long) {
        this.scrubPosition = scrubPosition
        scrubbing = true
        isPressed = true
        val parent = parent
        parent?.requestDisallowInterceptTouchEvent(true)
        for (listener in listeners) {
            listener.onScrubStart(this, scrubPosition)
        }
    }

    private fun updateScrubbing(scrubPosition: Long) {
        if (this.scrubPosition == scrubPosition) {
            return
        }
        this.scrubPosition = scrubPosition
        for (listener in listeners) {
            listener.onScrubMove(this, scrubPosition)
        }
    }

    private fun stopScrubbing(canceled: Boolean) {
        removeCallbacks(stopScrubbingRunnable)
        scrubbing = false
        isPressed = false
        val parent = parent
        parent?.requestDisallowInterceptTouchEvent(false)
        invalidate()
        for (listener in listeners) {
            listener.onScrubStop(this, scrubPosition, canceled)
        }
        if (this::timelineMarkerPositionListener.isInitialized) {
            timelineMarkerPositionListener.onScrubStop()
        }
    }

    /**
     * Incrementally scrubs the position by `positionChange`.
     *
     * @param positionChange The change in the scrubber position, in milliseconds. May be negative.
     * @return Returns whether the scrubber position changed.
     */
    private fun scrubIncrementally(positionChange: Long): Boolean {
        if (duration <= 0) {
            return false
        }
        val previousPosition = if (scrubbing) scrubPosition else position
        val scrubPosition = Util.constrainValue(
            previousPosition + positionChange,
            0,
            duration
        )
        if (scrubPosition == previousPosition) {
            return false
        }
        if (!scrubbing) {
            startScrubbing(scrubPosition)
        } else {
            updateScrubbing(scrubPosition)
        }
        update()
        return true
    }

    private fun notifyScrubbingUpdated(xPosition: Float) {
        if (this::timelineMarkerPositionListener.isInitialized) {
            timelineMarkerPositionListener.onScrubMove(
                xPosition,
                poiPositionsOnScreen
            )
        }
    }

    private fun update() {
        bufferedBar.set(progressBar)
        scrubberBar.set(progressBar)
        val newScrubberTime = if (scrubbing) scrubPosition else position
        if (duration > 0) {
            val bufferedPixelWidth =
                (progressBar.width() * bufferedPosition / duration).toInt()
            bufferedBar.right =
                (progressBar.left + bufferedPixelWidth).coerceAtMost(progressBar.right)
            val scrubberPixelPosition =
                (progressBar.width() * newScrubberTime / duration).toInt()
            scrubberBar.right =
                (progressBar.left + scrubberPixelPosition).coerceAtMost(progressBar.right)
        } else {
            bufferedBar.right = progressBar.left
            scrubberBar.right = progressBar.left
        }
        invalidate()
    }

    private fun positionScrubber(xPosition: Float) {
        scrubberBar.right = Util.constrainValue(
            xPosition.toInt(),
            progressBar.left,
            progressBar.right
        )
    }

    private fun resolveRelativeTouchPosition(motionEvent: MotionEvent): Point {
        getLocationOnScreen(locationOnScreen)
        touchPosition[motionEvent.rawX.toInt() - locationOnScreen[0]] =
            motionEvent.rawY.toInt() - locationOnScreen[1]
        return touchPosition
    }

    private val scrubberPosition: Long
        get() = if (progressBar.width() <= 0 || duration == C.TIME_UNSET) {
            0
        } else scrubberBar.width() * duration / progressBar.width()

    private fun isInSeekBar(x: Float, y: Float): Boolean {
        return seekBounds.contains(x.toInt(), y.toInt())
    }

    private fun drawTimeBar(canvas: Canvas) {
        val progressBarHeight = progressBar.height()
        val barTop = progressBar.centerY() - progressBarHeight / 2
        val barBottom = barTop + progressBarHeight
        if (duration <= 0) {
            canvas.drawRect(
                progressBar.left.toFloat(),
                barTop.toFloat(),
                progressBar.right.toFloat(),
                barBottom.toFloat(),
                unplayedPaint
            )
            return
        }
        var bufferedLeft = bufferedBar.left
        val bufferedRight = bufferedBar.right
        val progressLeft =
            progressBar.left.coerceAtLeast(bufferedRight).coerceAtLeast(scrubberBar.right)
        if (progressLeft < progressBar.right) {
            canvas.drawRect(
                progressLeft.toFloat(),
                barTop.toFloat(),
                progressBar.right.toFloat(),
                barBottom.toFloat(),
                unplayedPaint
            )
        }
        bufferedLeft = bufferedLeft.coerceAtLeast(scrubberBar.right)
        if (bufferedRight > bufferedLeft) {
            canvas.drawRect(
                bufferedLeft.toFloat(),
                barTop.toFloat(),
                bufferedRight.toFloat(),
                barBottom.toFloat(),
                bufferedPaint
            )
        }
        if (scrubberBar.width() > 0) {
            canvas.drawRect(
                scrubberBar.left.toFloat(),
                barTop.toFloat(),
                scrubberBar.right.toFloat(),
                barBottom.toFloat(),
                playedPaint
            )
        }


        val paint = Paint()
        val timelineMarkerWidth = adMarkerWidth / 2

        poiArrayList.forEachIndexed { index, poi ->

            val poiOffsetAdjusted: Long
            if (poi.offset + poi.seekOffset in 0L..duration) {
                poiOffsetAdjusted = poi.offset + poi.seekOffset
            } else {
                poiPositionsOnScreen[index].positionOnScreen = -1
                return
            }

            try {
                paint.color = Color.parseColor(poi.poiType.color)
            } catch (e: IllegalArgumentException) {
                Timber.e("Unknown Color ${poi.poiType.color}")
                paint.color = Color.WHITE
            }


            val poiTimeMs = Util.constrainValue(
                poiOffsetAdjusted,
                0,
                duration
            )

            val markerPositionOffset: Int =
                (progressBar.width() * poiTimeMs / duration).toInt() - timelineMarkerWidth

            val markerLeft = progressBar.left + (progressBar.width() - adMarkerWidth).coerceAtMost(
                0.coerceAtLeast(
                    markerPositionOffset
                )
            )
            val markerRight = markerLeft + adMarkerWidth.toFloat()

            canvas.drawRect(
                markerLeft.toFloat(),
                (barTop - 6).toFloat(),
                markerRight,
                (barBottom + 6).toFloat(),
                paint
            )

            poiPositionsOnScreen[index].positionOnScreen =
                ((markerLeft + markerRight) / 2).toInt()
        }

        if (adGroupCount == 0) {
            return
        }
        val adGroupTimesMs = Assertions.checkNotNull(adGroupTimesMs)
        val playedAdGroups =
            Assertions.checkNotNull(playedAdGroups)
        val adMarkerOffset = adMarkerWidth / 2
        for (i in 0 until adGroupCount) {
            val adGroupTimeMs = Util.constrainValue(
                adGroupTimesMs[i],
                0,
                duration
            )
            val markerPositionOffset =
                (progressBar.width() * adGroupTimeMs / duration).toInt() - adMarkerOffset
            val markerLeft = progressBar.left + (progressBar.width() - adMarkerWidth).coerceAtMost(
                0.coerceAtLeast(
                    markerPositionOffset
                )
            )
            val playedPaint =
                if (playedAdGroups[i]) playedAdMarkerPaint else adMarkerPaint
            canvas.drawRect(
                markerLeft.toFloat(),
                barTop.toFloat(),
                markerLeft + adMarkerWidth.toFloat(),
                barBottom.toFloat(),
                playedPaint
            )
        }


    }

    private fun drawPlayhead(canvas: Canvas) {
        if (duration <= 0) {
            return
        }
        val playheadX = Util.constrainValue(
            scrubberBar.right,
            scrubberBar.left,
            progressBar.right
        )
        val playheadY = scrubberBar.centerY()
        if (scrubberDrawable == null) {
            val scrubberSize =
                if (scrubbing || isFocused) scrubberDraggedSize else if (isEnabled) scrubberEnabledSize else scrubberDisabledSize
            val playheadRadius = scrubberSize / 2
            canvas.drawCircle(
                playheadX.toFloat(),
                playheadY.toFloat(),
                playheadRadius.toFloat(),
                scrubberPaint
            )
        } else {
            scrubberDrawable?.let {
                val scrubberDrawableWidth = it.intrinsicWidth
                val scrubberDrawableHeight = it.intrinsicHeight
                it.setBounds(
                    playheadX - scrubberDrawableWidth / 2,
                    playheadY - scrubberDrawableHeight / 2,
                    playheadX + scrubberDrawableWidth / 2,
                    playheadY + scrubberDrawableHeight / 2
                )
                it.draw(canvas)
            }

        }
    }

    private fun updateDrawableState() {
        if (scrubberDrawable != null && scrubberDrawable!!.isStateful
            && scrubberDrawable!!.setState(drawableState)
        ) {
            invalidate()
        }
    }

    @RequiresApi(29)
    private fun setSystemGestureExclusionRectsV29(width: Int, height: Int) {
        if (lastExclusionRectangle != null && lastExclusionRectangle!!.width() == width && lastExclusionRectangle!!.height() == height
        ) {
            // Allocating inside onLayout is considered a DrawAllocation lint error, so avoid if possible.
            return
        }
        lastExclusionRectangle = Rect( /* left= */0,  /* top= */0, width, height)
        systemGestureExclusionRects = listOf(lastExclusionRectangle!!)
    }

    private val progressText: String
        get() = Util.getStringForTime(
            formatBuilder,
            formatter,
            position
        )

    private val positionIncrement: Long
        get() = if (keyTimeIncrement == C.TIME_UNSET) if (duration == C.TIME_UNSET) 0 else duration / keyCountIncrement else keyTimeIncrement

    private fun setDrawableLayoutDirection(drawable: Drawable): Boolean {
        return Util.SDK_INT >= 23 && setDrawableLayoutDirection(
            drawable,
            layoutDirection
        )
    }

    companion object {
        /**
         * Default height for the time bar, in dp.
         */
        const val DEFAULT_BAR_HEIGHT_DP = 4

        /**
         * Default height for the touch target, in dp.
         */
        const val DEFAULT_TOUCH_TARGET_HEIGHT_DP = 26

        /**
         * Default width for ad markers, in dp.
         */
        const val DEFAULT_AD_MARKER_WIDTH_DP = 4

        /**
         * Default diameter for the scrubber when enabled, in dp.
         */
        const val DEFAULT_SCRUBBER_ENABLED_SIZE_DP = 12

        /**
         * Default diameter for the scrubber when disabled, in dp.
         */
        const val DEFAULT_SCRUBBER_DISABLED_SIZE_DP = 0

        /**
         * Default diameter for the scrubber when dragged, in dp.
         */
        const val DEFAULT_SCRUBBER_DRAGGED_SIZE_DP = 16

        /**
         * Default color for the played portion of the time bar.
         */
        const val DEFAULT_PLAYED_COLOR = -0x1

        /**
         * Default color for the unplayed portion of the time bar.
         */
        const val DEFAULT_UNPLAYED_COLOR = 0x33FFFFFF

        /**
         * Default color for the buffered portion of the time bar.
         */
        const val DEFAULT_BUFFERED_COLOR = -0x33000001

        /**
         * Default color for the scrubber handle.
         */
        const val DEFAULT_SCRUBBER_COLOR = -0x1

        /**
         * Default color for ad markers.
         */
        const val DEFAULT_AD_MARKER_COLOR = -0x4d000100

        /**
         * Default color for played ad markers.
         */
        const val DEFAULT_PLAYED_AD_MARKER_COLOR = 0x33FFFF00

        /**
         * The threshold in dps above the bar at which touch events trigger fine scrub mode.
         */
        private const val FINE_SCRUB_Y_THRESHOLD_DP = -50

        /**
         * The ratio by which times are reduced in fine scrub mode.
         */
        private const val FINE_SCRUB_RATIO = 3

        /**
         * The time after which the scrubbing listener is notified that scrubbing has stopped after
         * performing an incremental scrub using key input.
         */
        private const val STOP_SCRUBBING_TIMEOUT_MS: Long = 1000
        private const val DEFAULT_INCREMENT_COUNT = 20

        /**
         * The name of the Android SDK view that most closely resembles this custom view. Used as the
         * class name for accessibility.
         */
        private const val ACCESSIBILITY_CLASS_NAME = "android.widget.SeekBar"
        private fun setDrawableLayoutDirection(
            drawable: Drawable,
            layoutDirection: Int
        ): Boolean {
            return Util.SDK_INT >= 23 && drawable.setLayoutDirection(
                layoutDirection
            )
        }

        private fun dpToPx(density: Float, dps: Int): Int {
            return (dps * density + 0.5f).toInt()
        }

        private fun pxToDp(density: Float, px: Int): Int {
            return (px / density).toInt()
        }
    }

    // Suppress warnings due to usage of View methods in the constructor.
    // the constructor does not initialize fields: adGroupTimesMs, playedAdGroups
    init {
        scrubberPaint.isAntiAlias = true
        listeners = CopyOnWriteArraySet()
        locationOnScreen = IntArray(2)
        touchPosition = Point()

        // Calculate the dimensions and paints for drawn elements.
        val res = context.resources
        val displayMetrics = res.displayMetrics
        density = displayMetrics.density
        fineScrubYThreshold =
            dpToPx(
                density,
                FINE_SCRUB_Y_THRESHOLD_DP
            )
        val defaultBarHeight =
            dpToPx(
                density,
                DEFAULT_BAR_HEIGHT_DP
            )
        var defaultTouchTargetHeight =
            dpToPx(
                density,
                DEFAULT_TOUCH_TARGET_HEIGHT_DP
            )
        val defaultAdMarkerWidth =
            dpToPx(
                density,
                DEFAULT_AD_MARKER_WIDTH_DP
            )
        val defaultScrubberEnabledSize =
            dpToPx(
                density,
                DEFAULT_SCRUBBER_ENABLED_SIZE_DP
            )
        val defaultScrubberDisabledSize =
            dpToPx(
                density,
                DEFAULT_SCRUBBER_DISABLED_SIZE_DP
            )
        val defaultScrubberDraggedSize =
            dpToPx(
                density,
                DEFAULT_SCRUBBER_DRAGGED_SIZE_DP
            )
        if (timebarAttrs != null) {
            val a = context.theme
                .obtainStyledAttributes(timebarAttrs, R.styleable.DefaultTimeBar, 0, 0)
            try {
                scrubberDrawable = a.getDrawable(R.styleable.DefaultTimeBar_scrubber_drawable)
                scrubberDrawable?.let {
                    setDrawableLayoutDirection(it)
                    defaultTouchTargetHeight =
                        it.minimumHeight.coerceAtLeast(defaultTouchTargetHeight)
                }
                barHeight = a.getDimensionPixelSize(
                    R.styleable.DefaultTimeBar_bar_height,
                    defaultBarHeight
                )
                touchTargetHeight = a.getDimensionPixelSize(
                    R.styleable.DefaultTimeBar_touch_target_height,
                    defaultTouchTargetHeight
                )
                adMarkerWidth = a.getDimensionPixelSize(
                    R.styleable.DefaultTimeBar_ad_marker_width,
                    defaultAdMarkerWidth
                )
                scrubberEnabledSize = a.getDimensionPixelSize(
                    R.styleable.DefaultTimeBar_scrubber_enabled_size, defaultScrubberEnabledSize
                )
                scrubberDisabledSize = a.getDimensionPixelSize(
                    R.styleable.DefaultTimeBar_scrubber_disabled_size, defaultScrubberDisabledSize
                )
                scrubberDraggedSize = a.getDimensionPixelSize(
                    R.styleable.DefaultTimeBar_scrubber_dragged_size, defaultScrubberDraggedSize
                )
                val playedColor = a.getInt(
                    R.styleable.DefaultTimeBar_played_color,
                    DEFAULT_PLAYED_COLOR
                )
                val scrubberColor = a.getInt(
                    R.styleable.DefaultTimeBar_scrubber_color,
                    DEFAULT_SCRUBBER_COLOR
                )
                val bufferedColor = a.getInt(
                    R.styleable.DefaultTimeBar_buffered_color,
                    DEFAULT_BUFFERED_COLOR
                )
                val unplayedColor = a.getInt(
                    R.styleable.DefaultTimeBar_unplayed_color,
                    DEFAULT_UNPLAYED_COLOR
                )
                val adMarkerColor = a.getInt(
                    R.styleable.DefaultTimeBar_ad_marker_color,
                    DEFAULT_AD_MARKER_COLOR
                )
                val playedAdMarkerColor = a.getInt(
                    R.styleable.DefaultTimeBar_played_ad_marker_color,
                    DEFAULT_PLAYED_AD_MARKER_COLOR
                )
                playedPaint.color = playedColor
                scrubberPaint.color = scrubberColor
                bufferedPaint.color = bufferedColor
                unplayedPaint.color = unplayedColor
                adMarkerPaint.color = adMarkerColor
                playedAdMarkerPaint.color = playedAdMarkerColor
            } finally {
                a.recycle()
            }
        } else {
            barHeight = defaultBarHeight
            touchTargetHeight = defaultTouchTargetHeight
            adMarkerWidth = defaultAdMarkerWidth
            scrubberEnabledSize = defaultScrubberEnabledSize
            scrubberDisabledSize = defaultScrubberDisabledSize
            scrubberDraggedSize = defaultScrubberDraggedSize
            playedPaint.color =
                DEFAULT_PLAYED_COLOR
            scrubberPaint.color =
                DEFAULT_SCRUBBER_COLOR
            bufferedPaint.color =
                DEFAULT_BUFFERED_COLOR
            unplayedPaint.color =
                DEFAULT_UNPLAYED_COLOR
            adMarkerPaint.color =
                DEFAULT_AD_MARKER_COLOR
            playedAdMarkerPaint.color =
                DEFAULT_PLAYED_AD_MARKER_COLOR
            scrubberDrawable = null
        }
        formatBuilder = StringBuilder()
        formatter = Formatter(formatBuilder, Locale.getDefault())
        stopScrubbingRunnable = Runnable { stopScrubbing( /* canceled= */false) }
        scrubberPadding = if (scrubberDrawable != null) {
            (scrubberDrawable!!.minimumWidth + 1) / 2
        } else {
            ((scrubberDisabledSize.coerceAtLeast(
                scrubberEnabledSize.coerceAtLeast(
                    scrubberDraggedSize
                )
            ) + 1)
                    / 2)
        }
        duration = C.TIME_UNSET
        keyTimeIncrement = C.TIME_UNSET
        keyCountIncrement =
            DEFAULT_INCREMENT_COUNT
        isFocusable = true
        if (importantForAccessibility == IMPORTANT_FOR_ACCESSIBILITY_AUTO) {
            importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_YES
        }
    }
}