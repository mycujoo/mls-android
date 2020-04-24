package tv.mycujoo.mls.widgets

import android.content.Context
import android.util.AttributeSet
import android.widget.SeekBar
import androidx.appcompat.widget.AppCompatSeekBar

class TimeLineSeekBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatSeekBar(context, attrs, defStyleAttr) {


    init {
        setListener()
    }

    var listener: OnTimeLineChangeListener? = null


    private fun setListener() {
        setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                listener?.onChange(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
    }


}