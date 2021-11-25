package tv.mycujoo.ui

import android.content.Context
import androidx.constraintlayout.widget.ConstraintLayout

interface PlayerViewContract {
    fun context(): Context
    fun overlayHost(): ConstraintLayout
}