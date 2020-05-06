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
import kotlinx.android.synthetic.main.overlay_view_basic_single_line.view.*
import kotlinx.android.synthetic.main.overlay_view_substitute.view.*
import tv.mycujoo.mls.R
import tv.mycujoo.mls.entity.OverLayAction

class BasicSingleLineOverlayView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    init {
        LayoutInflater.from(context).inflate(R.layout.overlay_view_basic_single_line, this, true)
    }

    fun setData(overlayEntity: OverLayAction) {
        basicSingleLineView_firstTextView.text = overlayEntity.firstText
        basicSingleLineView_secondTextView.text = overlayEntity.secondText
        Glide.with(basicSingleLineView_logoImageView).load(overlayEntity.logoUrl)
            .into(basicSingleLineView_logoImageView)
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
                            (it as ViewGroup).removeView(this@BasicSingleLineOverlayView)
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
                            (it as ViewGroup).removeView(this@BasicSingleLineOverlayView)
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