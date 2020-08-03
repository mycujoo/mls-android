package tv.mycujoo.mls.helper

import tv.mycujoo.domain.entity.AnimationType
import tv.mycujoo.domain.entity.HideOverlayActionEntity
import tv.mycujoo.domain.entity.ShowOverlayActionEntity

class AnimationClassifierHelper {
    companion object {
        private fun hasNoAnimation(overlayEntity: HideOverlayActionEntity): Boolean {
            return overlayEntity.outroAnimationType == AnimationType.NONE
        }

        private fun hasNoAnimation(overlayEntity: ShowOverlayActionEntity): Boolean {
            return overlayEntity.introAnimationType == AnimationType.NONE
        }

        fun hasIntroAnimation(animationType: AnimationType): Boolean {
            return when (animationType) {
                AnimationType.FADE_IN,
                AnimationType.SLIDE_FROM_LEFT,
                AnimationType.SLIDE_FROM_TOP,
                AnimationType.SLIDE_FROM_RIGHT,
                AnimationType.SLIDE_FROM_BOTTOM -> {
                    true
                }
                else -> false
            }
        }


        fun hasOutroAnimation(animationType: AnimationType): Boolean {
            return when (animationType) {
                AnimationType.FADE_OUT,
                AnimationType.SLIDE_TO_LEFT,
                AnimationType.SLIDE_TO_TOP,
                AnimationType.SLIDE_TO_RIGHT,
                AnimationType.SLIDE_TO_BOTTOM -> {
                    true
                }

                else -> {
                    false
                }
            }
        }

        private fun hasDynamicIntroAnimation(animationType: AnimationType): Boolean {
            return when (animationType) {
                AnimationType.SLIDE_FROM_LEFT,
                AnimationType.SLIDE_FROM_TOP,
                AnimationType.SLIDE_FROM_BOTTOM,
                AnimationType.SLIDE_FROM_RIGHT -> {
                    true
                }
                else -> {
                    false
                }
            }
        }

        private fun hasStaticIntroAnimation(animationType: AnimationType): Boolean {
            return when (animationType) {
                AnimationType.FADE_IN -> {
                    true
                }

                else -> {
                    false
                }
            }
        }

        private fun hasDynamicOutroAnimation(animationType: AnimationType): Boolean {
            return when (animationType) {
                AnimationType.SLIDE_TO_LEFT,
                AnimationType.SLIDE_TO_TOP,
                AnimationType.SLIDE_TO_RIGHT,
                AnimationType.SLIDE_TO_BOTTOM -> {
                    true
                }

                else -> {
                    false
                }
            }
        }

        private fun hasStaticOutroAnimation(animationType: AnimationType): Boolean {
            return when (animationType) {
                AnimationType.FADE_OUT -> {
                    true
                }

                else -> {
                    false
                }
            }
        }
    }
}