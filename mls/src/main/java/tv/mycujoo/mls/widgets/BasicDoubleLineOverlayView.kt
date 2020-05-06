package tv.mycujoo.mls.widgets

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Context
import android.os.Handler
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.overlay_view_basic_double_line.view.*
import tv.mycujoo.mls.R
import tv.mycujoo.mls.entity.OverLayAction

class BasicDoubleLineOverlayView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    init {
        LayoutInflater.from(context).inflate(R.layout.overlay_view_basic_double_line, this, true)
    }

    fun setData(overlayEntity: OverLayAction) {
        Glide.with(basicDoubleLineView_logoImageView).load(overlayEntity.logoUrl)
            .into(basicDoubleLineView_logoImageView)
        basicDoubleLineView_firstLineFirstTextView.text = overlayEntity.firstText
        basicDoubleLineView_firstLineSecondTextView.text = overlayEntity.secondText
        basicDoubleLineView_secondLineFirstTextView.text = overlayEntity.secondLineTexts[0]
        basicDoubleLineView_secondLineSecondTextView.text = overlayEntity.secondLineTexts[1]

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
                            (it as ViewGroup).removeView(this@BasicDoubleLineOverlayView)
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

    fun dismissToRightIn(handler: Handler, delay: Long) {
        handler.postDelayed({

            parent?.let {
                val animation =
                    ObjectAnimator.ofFloat(this, "translationX", this.width.toFloat())
                animation.duration = 1000L
                animation.start()
                animation.addListener(object : Animator.AnimatorListener {
                    override fun onAnimationRepeat(animation: Animator?) {
                    }

                    override fun onAnimationEnd(animation: Animator?, isReverse: Boolean) {
                    }

                    override fun onAnimationEnd(animation: Animator?) {
                        parent?.let {
                            (it as ViewGroup).removeView(this@BasicDoubleLineOverlayView)
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