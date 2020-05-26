package tv.mycujoo.mls.helper

import android.view.ViewGroup
import android.widget.RelativeLayout
import tv.mycujoo.mls.entity.LayoutPosition
import tv.mycujoo.mls.widgets.OverlayHost

class OverlayHelper {
    companion object {

        fun addView(
            host: OverlayHost,
            overlay: ViewGroup,
            position: LayoutPosition
        ) {

            when (position) {
                LayoutPosition.TOP_LEFT -> {
                    val layoutParams = RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP)
                    host.addView(overlay, layoutParams)
                }
                LayoutPosition.TOP_RIGHT -> {
                    val layoutParams = RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP)
                    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
                    host.addView(overlay, layoutParams)
                }
                LayoutPosition.BOTTOM_RIGHT -> {
                    val layoutParams = RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
                    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
                    host.addView(overlay, layoutParams)
                }
                LayoutPosition.BOTTOM_LEFT -> {
                    val layoutParams = RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
                    host.addView(overlay, layoutParams)
                }
            }


        }

        fun removeInFuture(
            host: OverlayHost,
            overlayView: ViewGroup,
            dismissIn: Long
        ) {
            host.postDelayed({ host.removeView(overlayView) }, dismissIn)
        }

    }

}
