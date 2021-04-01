package tv.mycujoo.mcls.helper

import org.junit.Test
import tv.mycujoo.domain.entity.AnimationType
import tv.mycujoo.domain.entity.HideOverlayActionEntity
import kotlin.test.assertFalse

class AnimationClassifierHelperTest {
    /**region hasIntroAnimation() tests*/
    @Test
    fun `given entity with FADE_IN animation, should return has intro animation`() {
        val introAnimationType = AnimationType.FADE_IN


        val hasIntroAnimation = AnimationClassifierHelper.hasIntroAnimation(introAnimationType)


        assert(hasIntroAnimation)
    }

    @Test
    fun `given entity with SLIDE_FROM_LEFT animation, should return has intro animation`() {
        val introAnimationType = AnimationType.SLIDE_FROM_LEFT


        val hasIntroAnimation = AnimationClassifierHelper.hasIntroAnimation(introAnimationType)


        assert(hasIntroAnimation)
    }

    @Test
    fun `given entity with SLIDE_FROM_TOP animation, should return has intro animation`() {
        val introAnimationType = AnimationType.SLIDE_FROM_TOP


        val hasIntroAnimation = AnimationClassifierHelper.hasIntroAnimation(introAnimationType)


        assert(hasIntroAnimation)
    }

    @Test
    fun `given entity with SLIDE_FROM_RIGHT animation, should return has intro animation`() {
        val introAnimationType = AnimationType.SLIDE_FROM_RIGHT


        val hasIntroAnimation = AnimationClassifierHelper.hasIntroAnimation(introAnimationType)


        assert(hasIntroAnimation)
    }

    @Test
    fun `given entity with SLIDE_FROM_BOTTOM animation, should return has intro animation`() {
        val introAnimationType = AnimationType.SLIDE_FROM_BOTTOM


        val hasIntroAnimation = AnimationClassifierHelper.hasIntroAnimation(introAnimationType)


        assert(hasIntroAnimation)
    }

    @Test
    fun `given entity without intro animation, should return has intro animation false`() {
        val introAnimationType = AnimationType.NONE


        val hasIntroAnimation = AnimationClassifierHelper.hasIntroAnimation(introAnimationType)


        assertFalse(hasIntroAnimation)
    }

    @Test
    fun `given entity with invalid intro animation (FADE_OUT), should return has intro animation false`() {
        val invalidIntroAnimation = AnimationType.FADE_OUT


        val hasIntroAnimation = AnimationClassifierHelper.hasIntroAnimation(invalidIntroAnimation)


        assertFalse(hasIntroAnimation)
    }

    @Test
    fun `given entity with invalid intro animation (SLIDE_TO_LEFT), should return has intro animation false`() {
        val invalidIntroAnimation = AnimationType.SLIDE_TO_LEFT


        val hasIntroAnimation = AnimationClassifierHelper.hasIntroAnimation(invalidIntroAnimation)


        assertFalse(hasIntroAnimation)
    }

    @Test
    fun `given entity with invalid intro animation (SLIDE_TO_TOP), should return has intro animation false`() {
        val invalidIntroAnimation = AnimationType.SLIDE_TO_TOP


        val hasIntroAnimation = AnimationClassifierHelper.hasIntroAnimation(invalidIntroAnimation)


        assertFalse(hasIntroAnimation)
    }

    @Test
    fun `given entity with invalid intro animation (SLIDE_TO_RIGHT), should return has intro animation false`() {
        val invalidIntroAnimation = AnimationType.SLIDE_TO_RIGHT


        val hasIntroAnimation = AnimationClassifierHelper.hasIntroAnimation(invalidIntroAnimation)


        assertFalse(hasIntroAnimation)
    }

    @Test
    fun `given entity with invalid intro animation (SLIDE_TO_BOTTOM), should return has intro animation false`() {
        val invalidIntroAnimation = AnimationType.SLIDE_TO_BOTTOM


        val hasIntroAnimation = AnimationClassifierHelper.hasIntroAnimation(invalidIntroAnimation)


        assertFalse(hasIntroAnimation)
    }

    /**endregion */

    /**region hasOutroAnimation() tests*/

    @Test
    fun `given entity with FADE_OUT animation, should return has outro animation`() {
        val outroAnimationType = AnimationType.FADE_OUT


        val hasOutroAnimation = AnimationClassifierHelper.hasOutroAnimation(outroAnimationType)


        assert(hasOutroAnimation)
    }

    @Test
    fun `given entity with SLIDE_TO_LEFT animation, should return has outro animation`() {
        val outroAnimationType = AnimationType.SLIDE_TO_LEFT


        val hasOutroAnimation = AnimationClassifierHelper.hasOutroAnimation(outroAnimationType)


        assert(hasOutroAnimation)
    }

    @Test
    fun `given entity with SLIDE_TO_TOP animation, should return has outro animation`() {
        val outroAnimationType = AnimationType.SLIDE_TO_TOP


        val hasOutroAnimation = AnimationClassifierHelper.hasOutroAnimation(outroAnimationType)


        assert(hasOutroAnimation)
    }

    @Test
    fun `given entity with SLIDE_TO_RIGHT animation, should return has outro animation`() {
        val outroAnimationType = AnimationType.SLIDE_TO_RIGHT


        val hasOutroAnimation = AnimationClassifierHelper.hasOutroAnimation(outroAnimationType)


        assert(hasOutroAnimation)
    }

    @Test
    fun `given entity with SLIDE_TO_BOTTOM animation, should return has outro animation`() {
        val outroAnimationType = AnimationType.SLIDE_TO_BOTTOM


        val hasOutroAnimation = AnimationClassifierHelper.hasOutroAnimation(outroAnimationType)


        assert(hasOutroAnimation)
    }

    @Test
    fun `given entity without outro animation, should return has outro animation false`() {
        val outroAnimationType = AnimationType.NONE


        val hasOutroAnimation = AnimationClassifierHelper.hasOutroAnimation(outroAnimationType)


        assertFalse(hasOutroAnimation)
    }

    @Test
    fun `given entity with invalid outro animation (FADE_IN), should return has outro animation false`() {
        val invalidOutroAnimationType = AnimationType.FADE_IN


        val hasOutroAnimation = AnimationClassifierHelper.hasOutroAnimation(invalidOutroAnimationType)


        assertFalse(hasOutroAnimation)
    }

    @Test
    fun `given entity with invalid outro animation (SLIDE_FROM_LEFT), should return has outro animation false`() {
        val invalidOutroAnimationType = AnimationType.SLIDE_FROM_LEFT


        val hasOutroAnimation = AnimationClassifierHelper.hasOutroAnimation(invalidOutroAnimationType)


        assertFalse(hasOutroAnimation)
    }

    @Test
    fun `given entity with invalid outro animation (SLIDE_FROM_TOP), should return has outro animation false`() {
        val invalidOutroAnimationType = AnimationType.SLIDE_FROM_TOP


        val hasOutroAnimation = AnimationClassifierHelper.hasOutroAnimation(invalidOutroAnimationType)


        assertFalse(hasOutroAnimation)
    }

    @Test
    fun `given entity with invalid outro animation (SLIDE_FROM_RIGHT), should return has outro animation false`() {
        val invalidOutroAnimationType = AnimationType.SLIDE_FROM_RIGHT


        val hasOutroAnimation = AnimationClassifierHelper.hasOutroAnimation(invalidOutroAnimationType)


        assertFalse(hasOutroAnimation)
    }

    @Test
    fun `given entity with invalid outro animation (SLIDE_FROM_BOTTOM), should return has outro animation false`() {
        val invalidOutroAnimationType = AnimationType.SLIDE_FROM_BOTTOM


        val hasOutroAnimation = AnimationClassifierHelper.hasOutroAnimation(invalidOutroAnimationType)


        assertFalse(hasOutroAnimation)
    }


    /**endregion */

    /**region hasDynamicIntroAnimation() tests*/
    @Test
    fun `given entity with SLIDE_FROM_LEFT intro animation, should return has dynamic intro animation`() {
        val introAnimation = AnimationType.SLIDE_FROM_LEFT


        val hasDynamicIntroAnimation = AnimationClassifierHelper.hasDynamicIntroAnimation(introAnimation)


        assert(hasDynamicIntroAnimation)
    }

    @Test
    fun `given entity with SLIDE_FROM_TOP intro animation, should return has dynamic intro animation`() {
        val introAnimation = AnimationType.SLIDE_FROM_TOP


        val hasDynamicIntroAnimation = AnimationClassifierHelper.hasDynamicIntroAnimation(introAnimation)


        assert(hasDynamicIntroAnimation)
    }

    @Test
    fun `given entity with SLIDE_FROM_BOTTOM intro animation, should return has dynamic intro animation`() {
        val introAnimation = AnimationType.SLIDE_FROM_BOTTOM


        val hasDynamicIntroAnimation = AnimationClassifierHelper.hasDynamicIntroAnimation(introAnimation)


        assert(hasDynamicIntroAnimation)
    }

    @Test
    fun `given entity with SLIDE_FROM_RIGHT intro animation, should return has dynamic intro animation`() {
        val introAnimation = AnimationType.SLIDE_FROM_RIGHT


        val hasDynamicIntroAnimation = AnimationClassifierHelper.hasDynamicIntroAnimation(introAnimation)


        assert(hasDynamicIntroAnimation)
    }


    /**endregion */

    /**region hasStaticIntroAnimation() tests*/
    @Test
    fun `given entity with FADE_IN intro animation, should return has dynamic intro animation`() {
        val introAnimation = AnimationType.FADE_IN


        val hasStaticIntroAnimation = AnimationClassifierHelper.hasStaticIntroAnimation(introAnimation)


        assert(hasStaticIntroAnimation)
    }

    /**endregion */

    /**region hasDynamicOutroAnimation() tests*/
    @Test
    fun `given entity with SLIDE_TO_LEFT outro animation, should return has dynamic outro animation`() {
        val outroAnimation = AnimationType.SLIDE_TO_LEFT


        val hasDynamicOutroAnimation = AnimationClassifierHelper.hasDynamicOutroAnimation(outroAnimation)


        assert(hasDynamicOutroAnimation)
    }

    @Test
    fun `given entity with SLIDE_TO_TOP outro animation, should return has dynamic outro animation`() {
        val outroAnimation = AnimationType.SLIDE_TO_TOP


        val hasDynamicOutroAnimation = AnimationClassifierHelper.hasDynamicOutroAnimation(outroAnimation)


        assert(hasDynamicOutroAnimation)
    }

    @Test
    fun `given entity with SLIDE_TO_RIGHT outro animation, should return has dynamic outro animation`() {
        val outroAnimation = AnimationType.SLIDE_TO_RIGHT


        val hasDynamicOutroAnimation = AnimationClassifierHelper.hasDynamicOutroAnimation(outroAnimation)


        assert(hasDynamicOutroAnimation)
    }

    @Test
    fun `given entity with SLIDE_TO_BOTTOM outro animation, should return has dynamic outro animation`() {
        val outroAnimation = AnimationType.SLIDE_TO_BOTTOM


        val hasDynamicOutroAnimation = AnimationClassifierHelper.hasDynamicOutroAnimation(outroAnimation)


        assert(hasDynamicOutroAnimation)
    }

    /**endregion */

    /**region hasStaticOutroAnimation() tests*/
    @Test
    fun `given entity with FADE_OUT outro animation, should return has static outro animation`() {
        val outroAnimation = AnimationType.FADE_OUT


        val hasStaticOutroAnimation = AnimationClassifierHelper.hasStaticOutroAnimation(outroAnimation)


        assert(hasStaticOutroAnimation)
    }

    /**endregion */

    private fun getHideOverlayActionEntity(animationType: AnimationType): HideOverlayActionEntity {
        return HideOverlayActionEntity(
            "random_id_1002", "custom_id_1001",
            animationType,
            -1L
        )
    }
}