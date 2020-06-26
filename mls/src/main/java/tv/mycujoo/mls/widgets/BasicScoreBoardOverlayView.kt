package tv.mycujoo.mls.widgets

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Context
import android.os.Handler
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.overlay_view_basic_score_board.view.*
import tv.mycujoo.mls.R
import tv.mycujoo.mls.entity.actions.OverLayAction

class BasicScoreBoardOverlayView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    init {
        LayoutInflater.from(context).inflate(R.layout.overlay_view_basic_score_board, this, true)
    }

    fun setData(overlayEntity: OverLayAction) {
        basicScoreBoardView_homeAbbrTextView.text = overlayEntity.firstText
        basicScoreBoardView_awayAbbrTextView.text = overlayEntity.secondText
        basicScoreBoardView_homeScoreTextView.text = overlayEntity.secondLineTexts[0]
        basicScoreBoardView_awayScoreTextView.text = overlayEntity.secondLineTexts[1]

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
                            (it as ViewGroup).removeView(this@BasicScoreBoardOverlayView)
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