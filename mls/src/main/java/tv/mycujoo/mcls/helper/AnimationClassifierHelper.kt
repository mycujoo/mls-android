package tv.mycujoo.mcls.helper

import tv.mycujoo.domain.entity.AnimationType
import tv.mycujoo.domain.entity.HideOverlayActionEntity
import tv.mycujoo.domain.entity.ShowOverlayActionEntity

/**
 * Helper class to distinguish between animation types
 * Each animation might be Static or Dynamic.
 * Dynamic animations change their position as part of animation. either x, y or both might change during the animating phase.
 * Static animations do NOT change their position at all. i.e. alpha might change.
 * Animations can be valid for specific type, i.e. Fade-in animation is only valid for 'Intro' animation, and
 * it is invalid for 'outro' animation.
 */
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

        fun hasDynamicIntroAnimation(animationType: AnimationType): Boolean {
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

        fun hasStaticIntroAnimation(animationType: AnimationType): Boolean {
            return when (animationType) {
                AnimationType.FADE_IN -> {
                    true
                }

                else -> {
                    false
                }
            }
        }

        fun hasDynamicOutroAnimation(animationType: AnimationType): Boolean {
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

        fun hasStaticOutroAnimation(animationType: AnimationType): Boolean {
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