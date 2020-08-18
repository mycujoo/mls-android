package tv.mycujoo.domain.entity

import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class OverlayEntityTest {

    @Before
    fun setUp() {
    }


    /**region Regular play mode*/
    @Test
    fun `given update() in 1 second range of intro offset, should return INTRO act`() {
        val sampleOverlayEntity = getSampleOverlayEntity(ONE_SECONDS, FIVE_SECONDS)


        val overlayAct1ms = sampleOverlayEntity.update(1L)
        val overlayAct1000ms = sampleOverlayEntity.update(1000L)


        assertEquals(OverlayAct.INTRO, overlayAct1ms)
        assertEquals(OverlayAct.INTRO, overlayAct1000ms)
    }

    @Test
    fun `given update() in 1 second range of intro offset, should not return INTRO act if its onscreen`() {
        val sampleOverlayEntity = getSampleOverlayEntity(ONE_SECONDS, FIVE_SECONDS)
        sampleOverlayEntity.isOnScreen = true


        val overlayAct1ms = sampleOverlayEntity.update(1L)
        val overlayAct1000ms = sampleOverlayEntity.update(1000L)


        assertNotEquals(OverlayAct.INTRO, overlayAct1ms)
        assertNotEquals(OverlayAct.INTRO, overlayAct1000ms)
    }

    @Test
    fun `given update() out of 1 second range of intro offset, should not return INTRO act`() {
        val sampleOverlayEntity = getSampleOverlayEntity(ONE_SECONDS, FIVE_SECONDS)


        val overlayAct0ms = sampleOverlayEntity.update(0L)
        val overlayAct1001ms = sampleOverlayEntity.update(1001L)


        assertNotEquals(OverlayAct.INTRO, overlayAct0ms)
        assertNotEquals(OverlayAct.INTRO, overlayAct1001ms)
    }

    @Test
    fun `given update() in 1 second range of outro offset, should return OUTRO act`() {
        val sampleOverlayEntity = getSampleOverlayEntity(ONE_SECONDS, FIVE_SECONDS)
        sampleOverlayEntity.isOnScreen = true


        val overlayAct4001ms = sampleOverlayEntity.update(4001L)
        val overlayAct5000ms = sampleOverlayEntity.update(5000L)


        assertEquals(OverlayAct.OUTRO, overlayAct4001ms)
        assertEquals(OverlayAct.OUTRO, overlayAct5000ms)
    }

    @Test
    fun `given update() out of 1 second range of outro offset, should return DO_NOTHING`() {
        val sampleOverlayEntity = getSampleOverlayEntity(ONE_SECONDS, FIVE_SECONDS)


        val overlayAct6000ms = sampleOverlayEntity.update(6000L)
        val overlayAct7000ms = sampleOverlayEntity.update(7000L)


        assertEquals(OverlayAct.DO_NOTHING, overlayAct6000ms)
        assertEquals(OverlayAct.DO_NOTHING, overlayAct7000ms)
    }

    @Test
    fun `given update() before 1 second range of intro offset, should return DO_NOTHING act`() {
        val sampleOverlayEntity = getSampleOverlayEntity(FIFTEEN_SECONDS, TWENTY_FIVE_SECONDS)


        val overlayAct0ms = sampleOverlayEntity.update(0L)
        val overlayAct1400ms = sampleOverlayEntity.update(1400L)


        assertEquals(OverlayAct.DO_NOTHING, overlayAct0ms)
        assertEquals(OverlayAct.DO_NOTHING, overlayAct1400ms)
    }
    /**endregion */

    /**region Seek or Jumped play mode*/
    @Test
    fun `given forceUpdate before 1 second range of intro offset, should return LINGERING_REMOVE`() {
        val sampleOverlayEntity = getSampleOverlayEntity(FIFTEEN_SECONDS, TWENTY_FIVE_SECONDS)


        val overlayAct0ms = sampleOverlayEntity.forceUpdate(0L)
        val overlayAct1400ms = sampleOverlayEntity.forceUpdate(14000L)


        assertEquals(OverlayAct.LINGERING_REMOVE, overlayAct0ms)
        assertEquals(OverlayAct.LINGERING_REMOVE, overlayAct1400ms)
    }

    @Test
    fun `given forceUpdate after outro offset without animation, should return LINGERING_REMOVE`() {
        val introTransitionSpec = TransitionSpec(FIFTEEN_SECONDS, AnimationType.NONE, ONE_SECONDS)
        val outroTransitionSpec =
            TransitionSpec(TWENTY_FIVE_SECONDS, AnimationType.NONE, 0L)
        val sampleOverlayEntity = getSampleOverlayEntity(introTransitionSpec, outroTransitionSpec)


        val overlayAct20001ms = sampleOverlayEntity.forceUpdate(20001L)
        val overlayAct26000ms = sampleOverlayEntity.forceUpdate(26000L)


        assertEquals(OverlayAct.LINGERING_REMOVE, overlayAct20001ms)
        assertEquals(OverlayAct.LINGERING_REMOVE, overlayAct26000ms)
    }

    @Test
    fun `given forceUpdate after outro animation range, should return LINGERING_REMOVE`() {
        val introTransitionSpec = TransitionSpec(FIFTEEN_SECONDS, AnimationType.NONE, ONE_SECONDS)
        val outroTransitionSpec =
            TransitionSpec(TWENTY_FIVE_SECONDS, AnimationType.FADE_OUT, ONE_SECONDS)
        val sampleOverlayEntity = getSampleOverlayEntity(introTransitionSpec, outroTransitionSpec)


        val overlayAct26000ms = sampleOverlayEntity.forceUpdate(26000L)
        val overlayAct27000ms = sampleOverlayEntity.forceUpdate(27000L)


        assertEquals(OverlayAct.LINGERING_REMOVE, overlayAct26000ms)
        assertEquals(OverlayAct.LINGERING_REMOVE, overlayAct27000ms)
    }

    @Test
    fun `given forceUpdate in intro animation range, should return LINGERING_INTRO`() {
        val introTransitionSpec = TransitionSpec(FIFTEEN_SECONDS, AnimationType.NONE, ONE_SECONDS)
        val sampleOverlayEntity = getSampleOverlayEntity(introTransitionSpec, TWENTY_FIVE_SECONDS)


        val overlayAct15000ms = sampleOverlayEntity.forceUpdate(15000L)
        val overlayAct15999ms = sampleOverlayEntity.forceUpdate(15999L)


        assertEquals(OverlayAct.LINGERING_INTRO, overlayAct15000ms)
        assertEquals(OverlayAct.LINGERING_INTRO, overlayAct15999ms)
    }

    @Test
    fun `given forceUpdate after into offset and before outro offset, should return LINGERING_MIDWAY`() {
        val introTransitionSpec = TransitionSpec(FIFTEEN_SECONDS, AnimationType.NONE, ONE_SECONDS)
        val outroTransitionSpec =
            TransitionSpec(TWENTY_FIVE_SECONDS, AnimationType.FADE_OUT, ONE_SECONDS)
        val sampleOverlayEntity = getSampleOverlayEntity(introTransitionSpec, outroTransitionSpec)


        val overlayAct16000ms = sampleOverlayEntity.forceUpdate(16000L)
        val overlayAct24999ms = sampleOverlayEntity.forceUpdate(24999L)


        assertEquals(OverlayAct.LINGERING_MIDWAY, overlayAct16000ms)
        assertEquals(OverlayAct.LINGERING_MIDWAY, overlayAct24999ms)
    }

    @Test
    fun `given forceUpdate after into offset when no outro offset is available, should return LINGERING_MIDWAY`() {
        val introTransitionSpec = TransitionSpec(FIFTEEN_SECONDS, AnimationType.NONE, ONE_SECONDS)
        val outroTransitionSpec = TransitionSpec(INVALID, AnimationType.UNSPECIFIED, 0L)
        val sampleOverlayEntity = getSampleOverlayEntity(introTransitionSpec, outroTransitionSpec)


        val overlayAct16000ms = sampleOverlayEntity.forceUpdate(16000L)
        val overlayAct1000000ms = sampleOverlayEntity.forceUpdate(1000000L)


        assertEquals(OverlayAct.LINGERING_MIDWAY, overlayAct16000ms)
        assertEquals(OverlayAct.LINGERING_MIDWAY, overlayAct1000000ms)
    }

    @Test
    fun `given forceUpdate in outro animation range, should return LINGERING_OUTRO if outro has valid animation`() {
        val introTransitionSpec = TransitionSpec(FIFTEEN_SECONDS, AnimationType.NONE, ONE_SECONDS)
        val outroTransitionSpec =
            TransitionSpec(TWENTY_FIVE_SECONDS, AnimationType.FADE_OUT, ONE_SECONDS)
        val sampleOverlayEntity = getSampleOverlayEntity(introTransitionSpec, outroTransitionSpec)


        val overlayAct15000ms = sampleOverlayEntity.forceUpdate(25000L)
        val overlayAct15999ms = sampleOverlayEntity.forceUpdate(25999L)


        assertEquals(OverlayAct.LINGERING_OUTRO, overlayAct15000ms)
        assertEquals(OverlayAct.LINGERING_OUTRO, overlayAct15999ms)
    }

    @Test
    fun `given forceUpdate in outro animation range, should not return LINGERING_OUTRO if outro does not have valid animation`() {
        val introTransitionSpec = TransitionSpec(FIFTEEN_SECONDS, AnimationType.NONE, ONE_SECONDS)
        val outroTransitionSpec =
            TransitionSpec(TWENTY_FIVE_SECONDS, AnimationType.NONE, ONE_SECONDS)
        val sampleOverlayEntity = getSampleOverlayEntity(introTransitionSpec, outroTransitionSpec)


        val overlayAct15000ms = sampleOverlayEntity.forceUpdate(25000L)
        val overlayAct15999ms = sampleOverlayEntity.forceUpdate(25999L)


        assertNotEquals(OverlayAct.LINGERING_OUTRO, overlayAct15000ms)
        assertNotEquals(OverlayAct.LINGERING_OUTRO, overlayAct15999ms)
    }

    @Test
    fun `given forceUpdate out of outro animation range, should not return LINGERING_OUTRO`() {
        val introTransitionSpec = TransitionSpec(FIFTEEN_SECONDS, AnimationType.NONE, ONE_SECONDS)
        val outroTransitionSpec =
            TransitionSpec(TWENTY_FIVE_SECONDS, AnimationType.NONE, ONE_SECONDS)
        val sampleOverlayEntity = getSampleOverlayEntity(introTransitionSpec, outroTransitionSpec)


        val overlayAct0ms = sampleOverlayEntity.forceUpdate(0L)
        val overlayAct14000ms = sampleOverlayEntity.forceUpdate(14000L)
        val overlayAct14001ms = sampleOverlayEntity.forceUpdate(14001L)
        val overlayAct24000ms = sampleOverlayEntity.forceUpdate(24000L)
        val overlayAct26000ms = sampleOverlayEntity.forceUpdate(26000L)


        assertNotEquals(OverlayAct.LINGERING_OUTRO, overlayAct0ms)
        assertNotEquals(OverlayAct.LINGERING_OUTRO, overlayAct14000ms)
        assertNotEquals(OverlayAct.LINGERING_OUTRO, overlayAct14001ms)
        assertNotEquals(OverlayAct.LINGERING_OUTRO, overlayAct24000ms)
        assertNotEquals(OverlayAct.LINGERING_OUTRO, overlayAct26000ms)
    }

    /**endregion */

    /**region Inner class*/
    companion object {
        private const val INVALID = -1L
        private const val ONE_SECONDS = 1000L
        private const val FIVE_SECONDS = 5000L
        private const val FIFTEEN_SECONDS = 15000L
        private const val TWENTY_FIVE_SECONDS = 25000L


        fun getSampleOverlayEntity(introOffset: Long, outroOffset: Long): OverlayEntity {
            val viewSpec = ViewSpec(null, null)

            val introTransitionSpec = TransitionSpec(introOffset, AnimationType.NONE, 0L)
            val outroTransitionSpec = TransitionSpec(outroOffset, AnimationType.NONE, 0L)

            return OverlayEntity(
                "id_1001",
                null,
                viewSpec,
                introTransitionSpec,
                outroTransitionSpec,
                emptyList()
            )
        }

        fun getSampleOverlayEntity(
            introTransitionSpec: TransitionSpec,
            outroOffset: Long
        ): OverlayEntity {
            val viewSpec = ViewSpec(null, null)

            val outroTransitionSpec = TransitionSpec(outroOffset, AnimationType.NONE, 0L)

            return OverlayEntity(
                "id_1001",
                null,
                viewSpec,
                introTransitionSpec,
                outroTransitionSpec,
                emptyList()
            )
        }

        fun getSampleOverlayEntity(
            introTransitionSpec: TransitionSpec,
            outroTransitionSpec: TransitionSpec

        ): OverlayEntity {
            val viewSpec = ViewSpec(null, null)

            return OverlayEntity(
                "id_1001",
                null,
                viewSpec,
                introTransitionSpec,
                outroTransitionSpec,
                emptyList()
            )
        }

    }


    /**endregion */
}