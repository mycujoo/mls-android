package tv.mycujoo.mls.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import tv.mycujoo.mls.R
import tv.mycujoo.mls.widgets.mlstimebar.MLSTimeBar

class RemotePlayerControllerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val timeBar: MLSTimeBar

    /**region Initializing*/
    init {
        LayoutInflater.from(context).inflate(R.layout.view_remote_player_controller, this, true)
        timeBar = findViewById(R.id.timeBar)
    }
    /**endregion */

    /**region Controls*/
    fun setDuration(duration: Long) {
        timeBar.setDuration(duration)
    }

    fun setPosition(position: Long) {
        timeBar.setPosition(position)
    }


    /**endregion */
}