package tv.mycujoo.mls.widgets.time_bar

import android.graphics.drawable.Drawable
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.annotation.Nullable
import androidx.core.graphics.drawable.DrawableCompat
import tv.mycujoo.mls.R
import tv.mycujoo.mls.widgets.time_bar.animation.PreviewAnimatorImpl
import tv.mycujoo.mls.widgets.time_bar.animation.PreviewAnimatorLollipopImplKotlin

class PreviewDelegate(private val previewView: PreviewView, var scrubberColor: Int) :
    OnPreviewChangeListener {

    private lateinit var previewFrameLayout: FrameLayout
    private lateinit var morphView: View
    private lateinit var previewFrameView: View
    private lateinit var previewParent: ViewGroup
    private lateinit var animator: PreviewAnimator
    private lateinit var previewLoader: PreviewLoader


    private var showing = false
    private var startTouch = false
    private var setup = false
    private var enabled = false


    init {
        previewView.addOnPreviewChangeListener(this)
    }

    fun setPreviewLoader(previewLoader: PreviewLoader) {
        this.previewLoader = previewLoader
    }


    fun onLayout(previewParent: ViewGroup, frameLayoutId: Int) {
        if (!setup) {
            this.previewParent = previewParent
            findFrameLayout(previewParent, frameLayoutId)?.let { attachPreviewFrameLayout(it) }
        }
    }

    fun attachPreviewFrameLayout(frameLayout: FrameLayout) {
        if (setup) {
            return
        }
        previewParent = (frameLayout.parent as ViewGroup)
        previewFrameLayout = frameLayout
        inflateViews(frameLayout)
        morphView.visibility = View.INVISIBLE
        previewFrameLayout.visibility = View.INVISIBLE
        previewFrameView.visibility = View.INVISIBLE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            animator =
                PreviewAnimatorLollipopImplKotlin(
                    previewParent,
                    previewView,
                    morphView,
                    previewFrameLayout,
                    previewFrameView
                )
        } else {
            animator = PreviewAnimatorImpl(
                previewParent, previewView, morphView,
                previewFrameLayout, previewFrameView
            )
        }
        setup = true
    }

    fun show() {
        if (!showing && setup) {
            animator.show()
            showing = true
        }
    }

    fun hide() {
        if (showing) {
            animator.hide()
            showing = false
        }
    }

    fun setEnabled(enabled: Boolean) {
        this.enabled = enabled
    }

    fun setPreviewColorTint(@ColorInt color: Int) {
        val drawable: Drawable =
            DrawableCompat.wrap(morphView.background)
        DrawableCompat.setTint(drawable, color)
        morphView.background = drawable
        previewFrameView.setBackgroundColor(color)
    }


    override fun onStartPreview(previewView: PreviewView, progress: Int) {
        startTouch = true
    }

    override fun onPreview(previewView: PreviewView, progress: Int, fromUser: Boolean) {
        if (setup && enabled) {

            animator.move()

            if (!showing && !startTouch && fromUser) {
                show()
            }

            previewLoader.loadPreview(progress.toLong(), previewView.getMax().toLong())
        }

        startTouch = false
    }


    override fun onStopPreview(previewView: PreviewView, progress: Int) {
        if (showing) {
            animator.hide()
        }
        showing = false
        startTouch = false
    }

    fun isSetup(): Boolean {
        return setup
    }


    private fun inflateViews(frameLayout: FrameLayout) {

        // Create morph view
        morphView = View(frameLayout.context)
        morphView.setBackgroundResource(R.drawable.shape_previewseekbar_morph)

        // Setup morph view
        val layoutParams = ViewGroup.LayoutParams(0, 0)
        layoutParams.width = frameLayout.resources
            .getDimensionPixelSize(R.dimen.previewseekbar_indicator_width)
        layoutParams.height = layoutParams.width
        previewParent.addView(morphView, layoutParams)

        // Create frame view for the circular reveal
        previewFrameView = View(frameLayout.context)
        val frameLayoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        frameLayout.addView(previewFrameView, frameLayoutParams)

        // Apply same color for the morph and frame views
//        setPreviewColorTint(scrubberColor)
        frameLayout.requestLayout()
    }


    @Nullable
    private fun findFrameLayout(parent: ViewGroup?, id: Int): FrameLayout? {
        if (id == View.NO_ID || parent == null) {
            return null
        }
        for (i in 0 until parent.childCount) {
            val child = parent.getChildAt(i)
            if (child.id == id && child is FrameLayout) {
                return child
            }
        }
        return null
    }
}