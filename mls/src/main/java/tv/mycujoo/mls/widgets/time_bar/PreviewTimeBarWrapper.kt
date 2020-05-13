package tv.mycujoo.mls.widgets.time_bar

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.ViewGroup
import com.google.android.exoplayer2.ui.DefaultTimeBar
import com.google.android.exoplayer2.ui.TimeBar
import kotlinx.android.synthetic.main.custom_controls_layout.view.*

class PreviewTimeBarWrapper @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : DefaultTimeBar(context, attrs, defStyleAttr), PreviewView, TimeBar.OnScrubListener {

    //    lateinit var defaultTimeBar : DefaultTimeBar
    lateinit var delegate: PreviewDelegate

    var onPreviewChangeListener = ArrayList<OnPreviewChangeListener>(0)

    private var scrubProgress = 0
    private var duration = 0
    private var scrubberDiameter = 48


    init {
        delegate = PreviewDelegate(this, 555555)
        delegate.setEnabled(isEnabled)

//        defaultTimeBar = DefaultTimeBar(context, attrs, defStyleAttr)
        addListener(this)
//        defaultTimeBar.addLis
    }

    /**region View Over-ridden*/
    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (!delegate.isSetup() && width != 0 && height != 0 && !isInEditMode) {
            if ((parent as ViewGroup).previewContainerLayout != null) {
                delegate.onLayout(
                    (parent as ViewGroup),
                    (parent as ViewGroup).previewContainerLayout.id
                )
            }
        }
    }
    /**endregion */


    /**region PreviewView Over-ridden*/

    override fun getProgress(): Int {
        return scrubProgress
    }

    override fun getMax(): Int {
        return duration
    }

    override fun getThumbOffset(): Int {
        return scrubberDiameter / 2
    }

    override fun addOnPreviewChangeListener(listener: OnPreviewChangeListener) {
        onPreviewChangeListener.add(listener)
    }
    /**endregion */

    /**region Exo Over-ridden*/

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        delegate.setEnabled(enabled)
    }

    override fun setPosition(position: Long) {
        super.setPosition(position)
        scrubProgress = position.toInt()
    }

    override fun setDuration(duration: Long) {
        super.setDuration(duration)
        this.duration = duration.toInt()
    }

    /**endregion */


    /**region OnScrubListener Over-ridden*/
    override fun onScrubStart(timeBar: TimeBar, position: Long) {
        Log.d("PreviewTimeBar", "onScrubStart p:$position")
        scrubProgress = position.toInt()
        onPreviewChangeListener.forEach { it.onStartPreview(this, position.toInt()) }
    }

    override fun onScrubMove(timeBar: TimeBar, position: Long) {
        Log.d("PreviewTimeBar", "onScrubMove p:$position")
        scrubProgress = position.toInt()
        onPreviewChangeListener.forEach { it.onPreview(this, position.toInt(), true) }

    }


    override fun onScrubStop(timeBar: TimeBar, position: Long, canceled: Boolean) {
        Log.d("PreviewTimeBar", "onScrubStop p:$position")
        onPreviewChangeListener.forEach { it.onStopPreview(this, position.toInt()) }

    }
    /**endregion */


}