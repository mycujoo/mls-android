package tv.mycujoo.mls.manager

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScope
import org.junit.Before
import org.junit.Test
import tv.mycujoo.mls.model.ScreenTimerDirection
import tv.mycujoo.mls.model.ScreenTimerFormat
import tv.mycujoo.mls.widgets.AdjustTimerEntity
import tv.mycujoo.mls.widgets.CreateTimerEntity
import tv.mycujoo.mls.widgets.SkipTimerEntity
import tv.mycujoo.mls.widgets.StartTimerEntity
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
class TimerKeeperTest {

    /**region Subject under test*/
    private lateinit var timerKeeper: TimerKeeper
    /**endregion */


    /**region Fields*/
    private lateinit var testCoroutineScope: TestCoroutineScope

    /**endregion */

    @Before
    fun setUp() {
        testCoroutineScope = TestCoroutineScope()
        timerKeeper = TimerKeeper(testCoroutineScope)
    }


    /**region GetValue tests*/
    @Test
    fun `given not initialized timer, should return empty string`() {
        // not creating (and starting) the timer


        val value = timerKeeper.getValue("name_0")


        assertEquals("", value)
    }
    /**region */


    /**region Start & Formatting*/
    @Test
    fun `given startValue of 0ms & SECOND format, should return '0'`() {
        val createScreenTimerEntity = getSampleCreateScreenTimerEntity(
            ScreenTimerFormat.SECONDS,
            0L,
            1000L
        )
        timerKeeper.createTimer(createScreenTimerEntity)
        var actualValue = ""
        timerKeeper.observe(SAMPLE_NAME_0) { actualValue = it.second }


        timerKeeper.startTimer(StartTimerEntity(SAMPLE_NAME_0, ZERO_SECONDS), ZERO_SECONDS)
        timerKeeper.notify(SAMPLE_NAME_0)


        assertEquals("0", actualValue)
        assertEquals("0", timerKeeper.getValue(SAMPLE_NAME_0))
    }

    @Test
    fun `given startValue of 10000ms & SECOND format, should return '10'`() {
        val createScreenTimerEntity = getSampleCreateScreenTimerEntity(
            ScreenTimerFormat.SECONDS,
            10000L,
            1000L
        )
        timerKeeper.createTimer(createScreenTimerEntity)
        var actualValue = ""
        timerKeeper.observe(SAMPLE_NAME_0) { actualValue = it.second }


        timerKeeper.startTimer(StartTimerEntity(SAMPLE_NAME_0, ZERO_SECONDS), ZERO_SECONDS)
        timerKeeper.notify(SAMPLE_NAME_0)


        assertEquals("10", actualValue)
        assertEquals("10", timerKeeper.getValue(SAMPLE_NAME_0))

    }

    @Test
    fun `given startValue of 60000ms & SECOND format, should return '60'`() {
        val createScreenTimerEntity = getSampleCreateScreenTimerEntity(
            ScreenTimerFormat.SECONDS,
            60000L,
            1000L
        )
        timerKeeper.createTimer(createScreenTimerEntity)
        var actualValue = ""
        timerKeeper.observe(SAMPLE_NAME_0) { actualValue = it.second }


        timerKeeper.startTimer(StartTimerEntity(SAMPLE_NAME_0, ZERO_SECONDS), ZERO_SECONDS)
        timerKeeper.notify(SAMPLE_NAME_0)


        assertEquals("60", actualValue)
        assertEquals("60", timerKeeper.getValue(SAMPLE_NAME_0))

    }

    @Test
    fun `given startValue of 1200000ms & SECOND format, should return '120'`() {
        val createScreenTimerEntity = getSampleCreateScreenTimerEntity(
            ScreenTimerFormat.SECONDS,
            120000L,
            1000L
        )
        timerKeeper.createTimer(createScreenTimerEntity)
        var actualValue = ""
        timerKeeper.observe(SAMPLE_NAME_0) { actualValue = it.second }


        timerKeeper.startTimer(StartTimerEntity(SAMPLE_NAME_0, ZERO_SECONDS), ZERO_SECONDS)
        timerKeeper.notify(SAMPLE_NAME_0)


        assertEquals("120", actualValue)
        assertEquals("120", timerKeeper.getValue(SAMPLE_NAME_0))
    }


    @Test
    fun `given startValue of 0ms & MINTUES_SECOND format, should return '0_00'`() {
        val createScreenTimerEntity = getSampleCreateScreenTimerEntity(
            ScreenTimerFormat.MINUTES_SECONDS,
            0L,
            1000L
        )
        timerKeeper.createTimer(createScreenTimerEntity)
        var actualValue = ""
        timerKeeper.observe(SAMPLE_NAME_0) { actualValue = it.second }


        timerKeeper.startTimer(StartTimerEntity(SAMPLE_NAME_0, ZERO_SECONDS), ZERO_SECONDS)
        timerKeeper.notify(SAMPLE_NAME_0)


        assertEquals("0:00", actualValue)
        assertEquals("0:00", timerKeeper.getValue(SAMPLE_NAME_0))

    }

    @Test
    fun `given startValue of 10000ms & MINTUES_SECOND format, should return '0_10'`() {
        val createScreenTimerEntity = getSampleCreateScreenTimerEntity(
            ScreenTimerFormat.MINUTES_SECONDS,
            10000L,
            1000L
        )
        timerKeeper.createTimer(createScreenTimerEntity)
        var actualValue = ""
        timerKeeper.observe(SAMPLE_NAME_0) { actualValue = it.second }


        timerKeeper.startTimer(StartTimerEntity(SAMPLE_NAME_0, ZERO_SECONDS), ZERO_SECONDS)
        timerKeeper.notify(SAMPLE_NAME_0)


        assertEquals("0:10", actualValue)
        assertEquals("0:10", timerKeeper.getValue(SAMPLE_NAME_0))

    }

    @Test
    fun `given startValue of 60000ms & MINTUES_SECOND format, should return '1_00'`() {
        val createScreenTimerEntity = getSampleCreateScreenTimerEntity(
            ScreenTimerFormat.MINUTES_SECONDS,
            60000L,
            1000L
        )
        timerKeeper.createTimer(createScreenTimerEntity)
        var actualValue = ""
        timerKeeper.observe(SAMPLE_NAME_0) { actualValue = it.second }


        timerKeeper.startTimer(StartTimerEntity(SAMPLE_NAME_0, ZERO_SECONDS), ZERO_SECONDS)
        timerKeeper.notify(SAMPLE_NAME_0)


        assertEquals("1:00", actualValue)
        assertEquals("1:00", timerKeeper.getValue(SAMPLE_NAME_0))
    }

    @Test
    fun `given startValue of 120000ms & MINTUES_SECOND format, should return '2_00'`() {
        val createScreenTimerEntity = getSampleCreateScreenTimerEntity(
            ScreenTimerFormat.MINUTES_SECONDS,
            120000L,
            1000L
        )
        timerKeeper.createTimer(createScreenTimerEntity)
        var actualValue = ""
        timerKeeper.observe(SAMPLE_NAME_0) { actualValue = it.second }


        timerKeeper.startTimer(StartTimerEntity(SAMPLE_NAME_0, ZERO_SECONDS), ZERO_SECONDS)
        timerKeeper.notify(SAMPLE_NAME_0)


        assertEquals("2:00", actualValue)
        assertEquals("2:00", timerKeeper.getValue(SAMPLE_NAME_0))
    }

    @Test
    fun `given a start command, should tune time`() {
        val createScreenTimerEntity = getSampleCreateScreenTimerEntity(
            ScreenTimerFormat.SECONDS,
            0L,
            1000L
        )
        timerKeeper.createTimer(createScreenTimerEntity)
        var actualValue = ""
        timerKeeper.observe(SAMPLE_NAME_0) { actualValue = it.second }


        timerKeeper.startTimer(StartTimerEntity(SAMPLE_NAME_0, TWO_SECONDS), TWENTY_FIVE_SECONDS)
        timerKeeper.notify(SAMPLE_NAME_0)


        assertEquals("23", actualValue)
        assertEquals("23", timerKeeper.getValue(SAMPLE_NAME_0))
    }
    /**endregion */

    /**region Adjust tests*/

    @Test
    fun `given adjust command to 10000ms, should return '10'`() {
        val createScreenTimerEntity = getSampleCreateScreenTimerEntity(
            ScreenTimerFormat.SECONDS,
            0L,
            1000L
        )
        timerKeeper.createTimer(createScreenTimerEntity)
        var actualValue = ""

        timerKeeper.observe(SAMPLE_NAME_0) { actualValue = it.second }


        timerKeeper.adjustTimer(
            AdjustTimerEntity(SAMPLE_NAME_0, ONE_SECONDS, TEN_SECONDS),
            ONE_SECONDS
        )
        timerKeeper.notify(SAMPLE_NAME_0)


        assertEquals("10", actualValue)
        assertEquals("10", timerKeeper.getValue(SAMPLE_NAME_0))

    }

    @Test
    fun `given multiple adjust commands, should return last one`() {
        val createScreenTimerEntity = getSampleCreateScreenTimerEntity(
            ScreenTimerFormat.SECONDS,
            0L,
            1000L
        )
        timerKeeper.createTimer(createScreenTimerEntity)
        var actualValue = ""

        timerKeeper.observe(SAMPLE_NAME_0) { actualValue = it.second }


        timerKeeper.adjustTimer(
            AdjustTimerEntity(SAMPLE_NAME_0, ONE_SECONDS, ONE_SECONDS),
            ONE_SECONDS
        )
        timerKeeper.adjustTimer(
            AdjustTimerEntity(SAMPLE_NAME_0, ONE_SECONDS, TWO_SECONDS),
            ONE_SECONDS
        )
        timerKeeper.adjustTimer(
            AdjustTimerEntity(SAMPLE_NAME_0, ONE_SECONDS, FIVE_SECONDS),
            ONE_SECONDS
        )
        timerKeeper.adjustTimer(
            AdjustTimerEntity(SAMPLE_NAME_0, ONE_SECONDS, TWO_SECONDS),
            ONE_SECONDS
        )
        timerKeeper.notify(SAMPLE_NAME_0)



        assertEquals("2", actualValue)
        assertEquals("2", timerKeeper.getValue(SAMPLE_NAME_0))
    }
    /**endregion */

    /**region Skip tests*/

    @Test
    fun `given skip command by 0ms, should return '0'`() {
        val createScreenTimerEntity = getSampleCreateScreenTimerEntity(
            ScreenTimerFormat.SECONDS,
            10000L,
            1000L
        )
        timerKeeper.createTimer(createScreenTimerEntity)
        var actualValue = ""
        timerKeeper.observe(SAMPLE_NAME_0) { actualValue = it.second }


        timerKeeper.skipTimer(SkipTimerEntity(SAMPLE_NAME_0, ONE_SECONDS, 0L))
        timerKeeper.notify(SAMPLE_NAME_0)


        assertEquals("10", actualValue)
        assertEquals("10", timerKeeper.getValue(SAMPLE_NAME_0))
    }
    @Test
    fun `given skip command by 10000ms, should return '20'`() {
        val createScreenTimerEntity = getSampleCreateScreenTimerEntity(
            ScreenTimerFormat.SECONDS,
            10000L,
            1000L
        )
        timerKeeper.createTimer(createScreenTimerEntity)
        var actualValue = ""
        timerKeeper.observe(SAMPLE_NAME_0) { actualValue = it.second }


        timerKeeper.skipTimer(SkipTimerEntity(SAMPLE_NAME_0, ONE_SECONDS, TEN_SECONDS))
        timerKeeper.notify(SAMPLE_NAME_0)


        assertEquals("20", actualValue)
        assertEquals("20", timerKeeper.getValue(SAMPLE_NAME_0))
    }
    @Test
    fun `given skip command by -10000ms, should return '0'`() {
        val createScreenTimerEntity = getSampleCreateScreenTimerEntity(
            ScreenTimerFormat.SECONDS,
            10000L,
            1000L
        )
        timerKeeper.createTimer(createScreenTimerEntity)
        var actualValue = ""
        timerKeeper.observe(SAMPLE_NAME_0) { actualValue = it.second }


        timerKeeper.skipTimer(SkipTimerEntity(SAMPLE_NAME_0, ONE_SECONDS, -10000L))
        timerKeeper.notify(SAMPLE_NAME_0)


        assertEquals("0", actualValue)
        assertEquals("0", timerKeeper.getValue(SAMPLE_NAME_0))
    }

    @Test
    fun `given multiple skip commands, should apply all`() {
        val createScreenTimerEntity = getSampleCreateScreenTimerEntity(
            ScreenTimerFormat.SECONDS,
            10000L,
            1000L
        )
        timerKeeper.createTimer(createScreenTimerEntity)
        var actualValue = ""
        timerKeeper.observe(SAMPLE_NAME_0) { actualValue = it.second }


        timerKeeper.skipTimer(SkipTimerEntity(SAMPLE_NAME_0, ONE_SECONDS, TEN_SECONDS))
        timerKeeper.skipTimer(SkipTimerEntity(SAMPLE_NAME_0, ONE_SECONDS, TEN_SECONDS))
        timerKeeper.notify(SAMPLE_NAME_0)


        assertEquals("30", actualValue)
        assertEquals("30", timerKeeper.getValue(SAMPLE_NAME_0))

    }
    /**endregion */

    /**region Direction tests*/


    @Test
    fun `given direction=up, should increase time`() {
        val createScreenTimerEntity = getSampleCreateScreenTimerEntity(
            ScreenTimerFormat.SECONDS,
            ScreenTimerDirection.UP,
            30000L
        )
        timerKeeper.createTimer(createScreenTimerEntity)
        var actualValue = ""
        timerKeeper.observe(SAMPLE_NAME_0) { actualValue = it.second }


        timerKeeper.startTimer(StartTimerEntity(SAMPLE_NAME_0, FIFTEEN_SECONDS), SIXTY_SECONDS)
        timerKeeper.notify(SAMPLE_NAME_0)



        assertEquals("75", actualValue)
        assertEquals("75", timerKeeper.getValue(SAMPLE_NAME_0))

    }

    @Test
    fun `given direction=down, should decrease time`() {
        val createScreenTimerEntity = getSampleCreateScreenTimerEntity(
            ScreenTimerFormat.SECONDS,
            ScreenTimerDirection.DOWN,
            30000L,
            0L
        )
        timerKeeper.createTimer(createScreenTimerEntity)
        var actualValue = ""
        timerKeeper.observe(SAMPLE_NAME_0) { actualValue = it.second }


        timerKeeper.startTimer(StartTimerEntity(SAMPLE_NAME_0, FIFTEEN_SECONDS), TWENTY_FIVE_SECONDS)
        timerKeeper.notify(SAMPLE_NAME_0)


        assertEquals("20", actualValue)
        assertEquals("20", timerKeeper.getValue(SAMPLE_NAME_0))
    }

    /**endregion */

    /**region Cap-Value tests*/

    @Test
    fun `given capValue with direction=up, should not exceed above capValue`() {
        val createScreenTimerEntity = getSampleCreateScreenTimerEntity(
            ScreenTimerFormat.SECONDS,
            ScreenTimerDirection.UP,
            0L,
            30000L

        )
        timerKeeper.createTimer(createScreenTimerEntity)
        var actualValue = ""
        timerKeeper.observe(SAMPLE_NAME_0) { actualValue = it.second }


        timerKeeper.startTimer(StartTimerEntity(SAMPLE_NAME_0, ONE_SECONDS), SIXTY_SECONDS)
        timerKeeper.notify(SAMPLE_NAME_0)



        assertEquals("30", actualValue)
        assertEquals("30", timerKeeper.getValue(SAMPLE_NAME_0))
    }

    @Test
    fun `given capValue with direction=down, should not exceed below capValue`() {
        val createScreenTimerEntity = getSampleCreateScreenTimerEntity(
            ScreenTimerFormat.SECONDS,
            ScreenTimerDirection.DOWN,
            50000L,
            30000L

        )
        timerKeeper.createTimer(createScreenTimerEntity)
        var actualValue = ""
        timerKeeper.observe(SAMPLE_NAME_0) { actualValue = it.second }


        timerKeeper.startTimer(StartTimerEntity(SAMPLE_NAME_0, ONE_SECONDS), SIXTY_SECONDS)
        timerKeeper.notify(SAMPLE_NAME_0)


        assertEquals("30", actualValue)
        assertEquals("30", timerKeeper.getValue(SAMPLE_NAME_0))
    }

    /**endregion */

    /**region Test sample data*/

    private fun getSampleCreateScreenTimerEntity(
        format: ScreenTimerFormat,
        startValue: Long,
        step: Long
    ): CreateTimerEntity {
        return CreateTimerEntity(
            SAMPLE_NAME_0,
            1000L,
            format,
            ScreenTimerDirection.UP,
            startValue,
            step,
            500000L
        )
    }

    private fun getSampleCreateScreenTimerEntity(
        format: ScreenTimerFormat,
        screenTimerDirection: ScreenTimerDirection,
        startValue: Long
    ): CreateTimerEntity {
        return CreateTimerEntity(
            SAMPLE_NAME_0,
            1000L,
            format,
            screenTimerDirection,
            startValue,
            1000L,
            500000L
        )
    }

    private fun getSampleCreateScreenTimerEntity(
        format: ScreenTimerFormat,
        screenTimerDirection: ScreenTimerDirection,
        startValue: Long,
        capValue: Long
    ): CreateTimerEntity {
        return CreateTimerEntity(
            SAMPLE_NAME_0,
            1000L,
            format,
            screenTimerDirection,
            startValue,
            1000L,
            capValue
        )
    }

    companion object {
        const val SAMPLE_NAME_0 = "sample_name_0"

        private const val INVALID = -1L
        private const val ZERO_SECONDS = 0L
        private const val ONE_SECONDS = 1000L
        private const val TWO_SECONDS = 2000L
        private const val FIVE_SECONDS = 5000L
        private const val TEN_SECONDS = 10000L
        private const val FIFTEEN_SECONDS = 15000L
        private const val TWENTY_FIVE_SECONDS = 25000L
        private const val SIXTY_SECONDS = 60000L
    }

    /**endregion */

}