package tv.mycujoo.mls.tv.widgets

import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import android.util.SparseArray
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import tv.mycujoo.mls.R

class MLSThumbsBar @JvmOverloads constructor(context: Context, attrs: AttributeSet?, defStyle: Int = 0) :
    LinearLayout(context, attrs, defStyle) {
    // initial value for Thumb's number before measuring the screen size
    var mNumOfThumbs = -1
    var mThumbWidthInPixel: Int
    var mThumbHeightInPixel: Int
    var mHeroThumbWidthInPixel: Int
    var mHeroThumbHeightInPixel: Int
    var mMeasuredMarginInPixel: Int
    val mBitmaps = SparseArray<Bitmap?>()

    // flag to determine if the number of thumbs in thumbs bar is set by user through
    // setNumberofThumbs API or auto-calculated according to android tv design spec.
    private var mIsUserSets = false

    /**
     * Get hero index which is the middle child.
     */
    val heroIndex: Int
        get() = childCount / 2

    /**
     * Set size of thumb view in pixels
     */
    fun setThumbSize(width: Int, height: Int) {
        mThumbHeightInPixel = height
        mThumbWidthInPixel = width
        val heroIndex = heroIndex
        for (i in 0 until childCount) {
            if (heroIndex != i) {
                val child = getChildAt(i)
                val lp = child.layoutParams as LayoutParams
                var changed = false
                if (lp.height != height) {
                    lp.height = height
                    changed = true
                }
                if (lp.width != width) {
                    lp.width = width
                    changed = true
                }
                if (changed) {
                    child.layoutParams = lp
                }
            }
        }
    }

    /**
     * Set size of hero thumb view in pixels, it is usually larger than other thumbs.
     */
    fun setHeroThumbSize(width: Int, height: Int) {
        mHeroThumbHeightInPixel = height
        mHeroThumbWidthInPixel = width
        val heroIndex = heroIndex
        for (i in 0 until childCount) {
            if (heroIndex == i) {
                val child = getChildAt(i)
                val lp = child.layoutParams as LayoutParams
                var changed = false
                if (lp.height != height) {
                    lp.height = height
                    changed = true
                }
                if (lp.width != width) {
                    lp.width = width
                    changed = true
                }
                if (changed) {
                    child.layoutParams = lp
                }
            }
        }
    }

    /**
     * Set the space between thumbs in pixels
     */
    fun setThumbSpace(spaceInPixel: Int) {
        mMeasuredMarginInPixel = spaceInPixel
        requestLayout()
    }

    /**
     * Set number of thumb views.
     */
    fun setNumberOfThumbs(numOfThumbs: Int) {
        mIsUserSets = true
        mNumOfThumbs = numOfThumbs
        setNumberOfThumbsInternal()
    }

    /**
     * Helper function for setNumberOfThumbs.
     * Will Update the layout settings in ThumbsBar based on mNumOfThumbs
     */
    private fun setNumberOfThumbsInternal() {
        while (childCount > mNumOfThumbs) {
            removeView(getChildAt(childCount - 1))
        }
        while (childCount < mNumOfThumbs) {
            val view = createThumbView(this)
            val lp = LayoutParams(
                mThumbWidthInPixel,
                mThumbHeightInPixel
            )
            addView(view, lp)
        }
        val heroIndex = heroIndex
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            val lp = child.layoutParams as LayoutParams
            if (heroIndex == i) {
                lp.width = mHeroThumbWidthInPixel
                lp.height = mHeroThumbHeightInPixel
            } else {
                lp.width = mThumbWidthInPixel
                lp.height = mThumbHeightInPixel
            }
            child.layoutParams = lp
        }
    }

    /**
     * Helper function to compute how many thumbs should be put in the screen
     * Assume we should put x's non-hero thumbs in the screen, the equation should be
     * 192dp (width of hero thumbs) +
     * 154dp (width of common thumbs) * x +
     * 4dp (width of the margin between thumbs) * x
     * = width
     * So the calculated number of non-hero thumbs should be (width - 192dp) / 158dp.
     * If the calculated number of non-hero thumbs is less than 2, it will be updated to 2
     * or if the calculated number or non-hero thumbs is not an even number, it will be
     * decremented by one.
     * This processing is used to make sure the arrangement of non-hero thumbs
     * in ThumbsBar is symmetrical.
     * Also there should be a hero thumb in the middle of the ThumbsBar,
     * the final result should be non-hero thumbs (after processing) + 1.
     *
     * @param widthInPixel measured width in pixel
     * @return The number of thumbs
     */
    private fun calculateNumOfThumbs(widthInPixel: Int): Int {
        var nonHeroThumbNum = roundUp(
            widthInPixel - mHeroThumbWidthInPixel,
            mThumbWidthInPixel + mMeasuredMarginInPixel
        )
        if (nonHeroThumbNum < 2) {
            // If the calculated number of non-hero thumbs is less than 2,
            // it will be updated to 2
            nonHeroThumbNum = 2
        } else if (nonHeroThumbNum and 1 != 0) {
            // If the calculated number or non-hero thumbs is not an even number,
            // it will be increased by one.
            nonHeroThumbNum++
        }
        // Count Hero Thumb to the final result
        return nonHeroThumbNum + 1
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = measuredWidth
        // If the number of thumbs in ThumbsBar is not set by user explicitly, it will be
        // recalculated based on Android TV Design Spec
        if (!mIsUserSets) {
            val numOfThumbs = calculateNumOfThumbs(width)
            // Set new number of thumbs when calculation result is different with current number
            if (mNumOfThumbs != numOfThumbs) {
                mNumOfThumbs = numOfThumbs
                setNumberOfThumbsInternal()
            }
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        val heroIndex = heroIndex
        val heroView = getChildAt(heroIndex)
        var heroLeft = width / 2 - heroView.measuredWidth / 2
        var heroRight = width / 2 + heroView.measuredWidth / 2
        heroView.layout(
            heroLeft, paddingTop, heroRight,
            paddingTop + heroView.measuredHeight
        )
        val heroCenter = paddingTop + heroView.measuredHeight / 2
        for (i in heroIndex - 1 downTo 0) {
            heroLeft -= mMeasuredMarginInPixel
            val child = getChildAt(i)
            child.layout(
                heroLeft - child.measuredWidth,
                heroCenter - child.measuredHeight / 2,
                heroLeft,
                heroCenter + child.measuredHeight / 2
            )
            heroLeft -= child.measuredWidth
        }
        for (i in heroIndex + 1 until mNumOfThumbs) {
            heroRight += mMeasuredMarginInPixel
            val child = getChildAt(i)
            child.layout(
                heroRight,
                heroCenter - child.measuredHeight / 2,
                heroRight + child.measuredWidth,
                heroCenter + child.measuredHeight / 2
            )
            heroRight += child.measuredWidth
        }
    }

    /**
     * Create a thumb view, it's by default a ImageView.
     */
    protected fun createThumbView(parent: ViewGroup): View {
        return ImageView(parent.context)
    }

    /**
     * Clear all thumb bitmaps set on thumb views.
     */
    fun clearThumbBitmaps() {
        for (i in 0 until childCount) {
            setThumbBitmap(i, null)
        }
        mBitmaps.clear()
    }

    /**
     * Get bitmap of given child index.
     */
    fun getThumbBitmap(index: Int): Bitmap? {
        return mBitmaps[index]
    }

    /**
     * Set thumb bitmap for a given index of child.
     */
    fun setThumbBitmap(index: Int, bitmap: Bitmap?) {
        mBitmaps.put(index, bitmap)
        (getChildAt(index) as ImageView).setImageBitmap(bitmap)
    }

    companion object {
        private fun roundUp(num: Int, divisor: Int): Int {
            return (num + divisor - 1) / divisor
        }
    }

    init {
        // According to the spec,
        // the width of non-hero thumb should be 80% of HeroThumb's Width, i.e. 0.8 * 192dp = 154dp
        mThumbWidthInPixel = context.resources.getDimensionPixelSize(
            R.dimen.lb_playback_transport_thumbs_width
        )
        mThumbHeightInPixel = context.resources.getDimensionPixelSize(
            R.dimen.lb_playback_transport_thumbs_height
        )
        // According to the spec, the width of HeroThumb should be 192dp
        mHeroThumbHeightInPixel = context.resources.getDimensionPixelSize(
            R.dimen.lb_playback_transport_hero_thumbs_width
        )
        mHeroThumbWidthInPixel = context.resources.getDimensionPixelSize(
            R.dimen.lb_playback_transport_hero_thumbs_height
        )
        // According to the spec, the margin between thumbs to be 4dp
        mMeasuredMarginInPixel = context.resources.getDimensionPixelSize(
            R.dimen.lb_playback_transport_thumbs_margin
        )
    }
}