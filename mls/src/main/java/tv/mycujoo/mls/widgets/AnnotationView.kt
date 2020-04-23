package tv.mycujoo.mls.widgets

import android.content.Context
import android.os.Handler
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.widget_annotation_simple.view.*
import tv.mycujoo.mls.R

class AnnotationView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {
    init {
        initView(context)
    }

    private fun initView(context: Context) {
        LayoutInflater.from(context).inflate(R.layout.widget_annotation_simple, this, true)
    }

    fun setText(text: String) {
        textView?.text = text
    }

    fun dismissIn(handler: Handler, delay: Long) {
        handler.postDelayed({
            parent?.let {
                (it as ViewGroup).removeView(this)
            }

        }, delay)
    }
}