package tv.mycujoo.mls.widgets.overlay

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.view_overlay_announcement.view.*
import tv.mycujoo.mls.R
import tv.mycujoo.mls.entity.actions.ShowAnnouncementOverlayAction

class AnnouncementOverlayView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    init {
        LayoutInflater.from(context).inflate(R.layout.view_overlay_announcement, this, true)
    }

    fun viewAction(action: ShowAnnouncementOverlayAction) {
        announcementOverlayView_rootLayout.setBackgroundColor(Color.parseColor(action.color))

        announcementOverlayView_line1TextView.text = action.line1
        announcementOverlayView_line2TextView.text = action.line2
        Glide.with(announcementOverlayView_imageView).load(action.imageUrl)
            .into(announcementOverlayView_imageView)
    }

    fun dismissIn(handler: Handler, delay: Long) {
        handler.postDelayed({


            parent?.let {
                val animation =
                    ObjectAnimator.ofFloat(this, "translationX", this.width.unaryMinus().toFloat())
                animation.duration = 1000L
                animation.start()
                animation.addListener(object : Animator.AnimatorListener {
                    override fun onAnimationRepeat(animation: Animator?) {
                    }

                    override fun onAnimationEnd(animation: Animator?, isReverse: Boolean) {
                    }

                    override fun onAnimationEnd(animation: Animator?) {
                        parent?.let {
                            (it as ViewGroup).removeView(this@AnnouncementOverlayView)
                        }
                    }

                    override fun onAnimationCancel(animation: Animator?) {
                    }

                    override fun onAnimationStart(animation: Animator?, isReverse: Boolean) {
                    }

                    override fun onAnimationStart(animation: Animator?) {
                    }
                })
            }

        }, delay)
    }
}