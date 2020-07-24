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
    fun `given update in 1 second range of intro offset, should return INTRO act`() {
        val sampleOverlayEntity = getSampleOverlayEntity(ONE_SECONDS, FIVE_SECONDS)


        val overlayAct1ms = sampleOverlayEntity.update(1L)
        val overlayAct1000ms = sampleOverlayEntity.update(1000L)


        assertEquals(OverlayAct.INTRO, overlayAct1ms)
        assertEquals(OverlayAct.INTRO, overlayAct1000ms)
    }

    @Test
    fun `given update in 1 second range of intro offset, should not return INTRO act if its onscreen`() {
        val sampleOverlayEntity = getSampleOverlayEntity(ONE_SECONDS, FIVE_SECONDS)
        sampleOverlayEntity.isOnScreen = true


        val overlayAct1ms = sampleOverlayEntity.update(1L)
        val overlayAct1000ms = sampleOverlayEntity.update(1000L)


        assertNotEquals(OverlayAct.INTRO, overlayAct1ms)
        assertNotEquals(OverlayAct.INTRO, overlayAct1000ms)
    }

    @Test
    fun `given update out of 1 second range of intro offset, should not return INTRO act`() {
        val sampleOverlayEntity = getSampleOverlayEntity(ONE_SECONDS, FIVE_SECONDS)


        val overlayAct0ms = sampleOverlayEntity.update(0L)
        val overlayAct1001ms = sampleOverlayEntity.update(1001L)


        assertNotEquals(OverlayAct.INTRO, overlayAct0ms)
        assertNotEquals(OverlayAct.INTRO, overlayAct1001ms)
    }

    @Test
    fun `given update in 1 second range of outro offset, should return OUTRO act`() {
        val sampleOverlayEntity = getSampleOverlayEntity(ONE_SECONDS, FIVE_SECONDS)
        sampleOverlayEntity.isOnScreen = true


        val overlayAct4001ms = sampleOverlayEntity.update(4001L)
        val overlayAct5000ms = sampleOverlayEntity.update(5000L)


        assertEquals(OverlayAct.OUTRO, overlayAct4001ms)
        assertEquals(OverlayAct.OUTRO, overlayAct5000ms)
    }

    @Test
    fun `given update out of 1 second range of outro offset, should not return OUTRO act if its offscreen `() {
        val sampleOverlayEntity = getSampleOverlayEntity(ONE_SECONDS, FIVE_SECONDS)


        val overlayAct4001ms = sampleOverlayEntity.update(4001L)
        val overlayAct5000ms = sampleOverlayEntity.update(5000L)


        assertNotEquals(OverlayAct.OUTRO, overlayAct4001ms)
        assertNotEquals(OverlayAct.OUTRO, overlayAct5000ms)
    }

    @Test
    fun `given update before 1 second range of intro offset, should return DO_NOTHING act`() {
        val sampleOverlayEntity = getSampleOverlayEntity(FIFTEEN_SECONDS, TWENTY_FIVE_SECONDS)


        val overlayAct0ms = sampleOverlayEntity.update(0L)
        val overlayAct1400ms = sampleOverlayEntity.update(1400L)


        assertEquals(OverlayAct.DO_NOTHING, overlayAct0ms)
        assertEquals(OverlayAct.DO_NOTHING, overlayAct1400ms)
    }
    /**endregion */

    /**region Seek or Jumped play mode*/
    @Test
    fun `given forceUpdate before 1 second range of intro offset, should return DO_NOTHING`() {
        val sampleOverlayEntity = getSampleOverlayEntity(FIFTEEN_SECONDS, TWENTY_FIVE_SECONDS)


        val overlayAct0ms = sampleOverlayEntity.forceUpdate(0L)
        val overlayAct1400ms = sampleOverlayEntity.forceUpdate(1400L)


        assertEquals(OverlayAct.DO_NOTHING, overlayAct0ms)
        assertEquals(OverlayAct.DO_NOTHING, overlayAct1400ms)
    }

    @Test
    fun `given forceUpdate in intro animation range, should return LINGERING_INTRO`() {
        val introTransitionSpec = TransitionSpec(FIFTEEN_SECONDS, AnimationType.NONE, ONE_SECONDS)
        val sampleOverlayEntity = getSampleOverlayEntity(introTransitionSpec, TWENTY_FIVE_SECONDS)
        sampleOverlayEntity.isOnScreen = true


        val overlayAct15000ms = sampleOverlayEntity.forceUpdate(15000L)
        val overlayAct15999ms = sampleOverlayEntity.forceUpdate(15999L)


        assertEquals(OverlayAct.LINGERING_INTRO, overlayAct15000ms)
        assertEquals(OverlayAct.LINGERING_INTRO, overlayAct15999ms)
    }

    @Test
    fun `given forceUpdate in outro animation range, should return LINGERING_OUTRO if outro has valid animation`() {
        val introTransitionSpec = TransitionSpec(FIFTEEN_SECONDS, AnimationType.NONE, ONE_SECONDS)
        val outroTransitionSpec =
            TransitionSpec(TWENTY_FIVE_SECONDS, AnimationType.FADE_OUT, ONE_SECONDS)
        val sampleOverlayEntity = getSampleOverlayEntity(introTransitionSpec, outroTransitionSpec)
        sampleOverlayEntity.isOnScreen = true


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
        sampleOverlayEntity.isOnScreen = true


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
        sampleOverlayEntity.isOnScreen = true


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


    companion object {
        private const val ONE_SECONDS = 1000L
        private const val FIVE_SECONDS = 5000L
        private const val FIFTEEN_SECONDS = 15000L
        private const val TWENTY_FIVE_SECONDS = 25000L

    }


    private fun getSampleOverlayEntity(introOffset: Long, outroOffset: Long): OverlayEntity {
        val viewSpec = ViewSpec(null, null)

        val introTransitionSpec = TransitionSpec(introOffset, AnimationType.NONE, 0L)
        val outroTransitionSpec = TransitionSpec(outroOffset, AnimationType.NONE, 0L)

        return OverlayEntity(
            "id_1001",
            null,
            viewSpec,
            introTransitionSpec,
            outroTransitionSpec,
            emptyMap()
        )
    }

    private fun getSampleOverlayEntity(
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
            emptyMap()
        )
    }

    private fun getSampleOverlayEntity(
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
            emptyMap()
        )
    }
}