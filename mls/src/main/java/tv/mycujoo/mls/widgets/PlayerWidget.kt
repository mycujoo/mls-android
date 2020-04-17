package tv.mycujoo.mls.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerControlView
import com.google.android.exoplayer2.ui.PlayerView

class PlayerWidget : ConstraintLayout {

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context)
    }

    private lateinit var playerView: PlayerView
    private lateinit var playerControlView: PlayerControlView


    private fun init(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) {

        playerView = PlayerView(context, attrs, defStyleAttr)
        playerView.id = View.generateViewId()
        playerView.useController = false

        addView(playerView)

        val constraintSet = ConstraintSet()
        constraintSet.clone(this)
        constraintSet.connect(playerView.id, ConstraintSet.TOP, id, ConstraintSet.TOP)
        constraintSet.connect(playerView.id, ConstraintSet.START, id, ConstraintSet.START)
        constraintSet.connect(playerView.id, ConstraintSet.END, id, ConstraintSet.END)
        constraintSet.connect(playerView.id, ConstraintSet.BOTTOM, id, ConstraintSet.BOTTOM)
        constraintSet.constrainHeight(playerView.id, 0)

        playerControlView = PlayerControlView(context, attrs, defStyleAttr)
        playerControlView.id = View.generateViewId()

        addView(playerControlView)

        constraintSet.clone(this)
        constraintSet.connect(
            playerControlView.id,
            ConstraintSet.TOP,
            playerView.id,
            ConstraintSet.BOTTOM
        )
        constraintSet.connect(playerControlView.id, ConstraintSet.START, id, ConstraintSet.START)
        constraintSet.connect(playerControlView.id, ConstraintSet.END, id, ConstraintSet.END)

        constraintSet.applyTo(this)

    }

    fun setPlayer(player: Player?) {
        playerView.player = player
        playerControlView.player = player
        playerControlView.showTimeoutMs = 0

    }
}