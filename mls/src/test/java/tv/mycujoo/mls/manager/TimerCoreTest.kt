package tv.mycujoo.mls.manager

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScope
import org.junit.Before
import org.junit.Test
import tv.mycujoo.mls.model.ScreenTimerDirection
import tv.mycujoo.mls.model.ScreenTimerFormat
import tv.mycujoo.mls.widgets.AdjustTimerEntity
import tv.mycujoo.mls.widgets.CreateTimerEntity
import tv.mycujoo.mls.widgets.StartTimerEntity
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
class TimerCoreTest {

    private lateinit var timeKeeper: TimeKeeper
    private lateinit var testCoroutineScope: TestCoroutineScope


    @Before
    fun setUp() {
        testCoroutineScope = TestCoroutineScope()
        timeKeeper = TimeKeeper(testCoroutineScope)
    }


    @Test
    fun `given startValue of 0ms & SECOND format, should return '0'`() {

        val createScreenTimerEntity = getSampleCreateScreenTimerEntity(
            ScreenTimerFormat.SECONDS,
            0L,
            1000L
        )
        timeKeeper.createTimer(createScreenTimerEntity)
        var actualValue = ""
        timeKeeper.observe(SAMPLE_NAME_0) { actualValue = it.second }


        timeKeeper.startTimer(StartTimerEntity(SAMPLE_NAME_0, ZERO_SECONDS), ZERO_SECONDS)
        timeKeeper.notify(SAMPLE_NAME_0)


        assertEquals("0", actualValue)
    }

    @Test
    fun `given startValue of 10000ms & SECOND format, should return '10'`() {

        val createScreenTimerEntity = getSampleCreateScreenTimerEntity(
            ScreenTimerFormat.SECONDS,
            10000L,
            1000L
        )
        timeKeeper.createTimer(createScreenTimerEntity)
        var actualValue = ""
        timeKeeper.observe(SAMPLE_NAME_0) { actualValue = it.second }


        timeKeeper.startTimer(StartTimerEntity(SAMPLE_NAME_0, ZERO_SECONDS), ZERO_SECONDS)
        timeKeeper.notify(SAMPLE_NAME_0)


        assertEquals("10", actualValue)
    }

    @Test
    fun `given startValue of 60000ms & SECOND format, should return '60'`() {

        val createScreenTimerEntity = getSampleCreateScreenTimerEntity(
            ScreenTimerFormat.SECONDS,
            60000L,
            1000L
        )
        timeKeeper.createTimer(createScreenTimerEntity)
        var actualValue = ""
        timeKeeper.observe(SAMPLE_NAME_0) { actualValue = it.second }


        timeKeeper.startTimer(StartTimerEntity(SAMPLE_NAME_0, ZERO_SECONDS), ZERO_SECONDS)
        timeKeeper.notify(SAMPLE_NAME_0)


        assertEquals("60", actualValue)
    }

    @Test
    fun `given startValue of 1200000ms & SECOND format, should return '120'`() {

        val createScreenTimerEntity = getSampleCreateScreenTimerEntity(
            ScreenTimerFormat.SECONDS,
            120000L,
            1000L
        )
        timeKeeper.createTimer(createScreenTimerEntity)
        var actualValue = ""
        timeKeeper.observe(SAMPLE_NAME_0) { actualValue = it.second }


        timeKeeper.startTimer(StartTimerEntity(SAMPLE_NAME_0, ZERO_SECONDS), ZERO_SECONDS)
        timeKeeper.notify(SAMPLE_NAME_0)


        assertEquals("120", actualValue)
    }


    @Test
    fun `given startValue of 0ms & MINTUES_SECOND format, should return '0_00'`() {

        val createScreenTimerEntity = getSampleCreateScreenTimerEntity(
            ScreenTimerFormat.MINUTES_SECONDS,
            0L,
            1000L
        )
        timeKeeper.createTimer(createScreenTimerEntity)
        var actualValue = ""
        timeKeeper.observe(SAMPLE_NAME_0) { actualValue = it.second }


        timeKeeper.startTimer(StartTimerEntity(SAMPLE_NAME_0, ZERO_SECONDS), ZERO_SECONDS)
        timeKeeper.notify(SAMPLE_NAME_0)


        assertEquals("0:00", actualValue)
    }

    @Test
    fun `given startValue of 10000ms & MINTUES_SECOND format, should return '0_10'`() {

        val createScreenTimerEntity = getSampleCreateScreenTimerEntity(
            ScreenTimerFormat.MINUTES_SECONDS,
            10000L,
            1000L
        )
        timeKeeper.createTimer(createScreenTimerEntity)
        var actualValue = ""
        timeKeeper.observe(SAMPLE_NAME_0) { actualValue = it.second }


        timeKeeper.startTimer(StartTimerEntity(SAMPLE_NAME_0, ZERO_SECONDS), ZERO_SECONDS)
        timeKeeper.notify(SAMPLE_NAME_0)


        assertEquals("0:10", actualValue)
    }

    @Test
    fun `given startValue of 60000ms & MINTUES_SECOND format, should return '1_00'`() {

        val createScreenTimerEntity = getSampleCreateScreenTimerEntity(
            ScreenTimerFormat.MINUTES_SECONDS,
            60000L,
            1000L
        )
        timeKeeper.createTimer(createScreenTimerEntity)
        var actualValue = ""
        timeKeeper.observe(SAMPLE_NAME_0) { actualValue = it.second }


        timeKeeper.startTimer(StartTimerEntity(SAMPLE_NAME_0, ZERO_SECONDS), ZERO_SECONDS)
        timeKeeper.notify(SAMPLE_NAME_0)


        assertEquals("1:00", actualValue)
    }

    @Test
    fun `given startValue of 120000ms & MINTUES_SECOND format, should return '2_00'`() {

        val createScreenTimerEntity = getSampleCreateScreenTimerEntity(
            ScreenTimerFormat.MINUTES_SECONDS,
            120000L,
            1000L
        )
        timeKeeper.createTimer(createScreenTimerEntity)
        var actualValue = ""
        timeKeeper.observe(SAMPLE_NAME_0) { actualValue = it.second }


        timeKeeper.startTimer(StartTimerEntity(SAMPLE_NAME_0, ZERO_SECONDS), ZERO_SECONDS)
        timeKeeper.notify(SAMPLE_NAME_0)


        assertEquals("2:00", actualValue)
    }


    @Test
    fun `given adjust command to 10000ms, should return '10'`() {
        val createScreenTimerEntity = getSampleCreateScreenTimerEntity(
            ScreenTimerFormat.SECONDS,
            0L,
            1000L
        )
        timeKeeper.createTimer(createScreenTimerEntity)
        var actualValue = ""

        timeKeeper.observe(SAMPLE_NAME_0) { actualValue = it.second }


        timeKeeper.adjustTimer(
            AdjustTimerEntity(SAMPLE_NAME_0, ONE_SECONDS, TEN_SECONDS),
            ONE_SECONDS
        )
        timeKeeper.notify(SAMPLE_NAME_0)


        assertEquals("10", actualValue)
    }

    @Test
    fun `given multiple adjust commands, should return last one`() {
        val createScreenTimerEntity = getSampleCreateScreenTimerEntity(
            ScreenTimerFormat.SECONDS,
            0L,
            1000L
        )
        timeKeeper.createTimer(createScreenTimerEntity)
        var actualValue = ""

        timeKeeper.observe(SAMPLE_NAME_0) { actualValue = it.second }


        //timeKeeper.adjustTime(SAMPLE_NAME_0, 10000L)
//        timeKeeper.adjustTime(SAMPLE_NAME_0, 20000L)
//        timeKeeper.adjustTime(SAMPLE_NAME_0, 30000L)
//        timeKeeper.adjustTime(SAMPLE_NAME_0, 20000L)

        timeKeeper.adjustTimer(
            AdjustTimerEntity(SAMPLE_NAME_0, ONE_SECONDS, ONE_SECONDS),
            ONE_SECONDS
        )
        timeKeeper.adjustTimer(
            AdjustTimerEntity(SAMPLE_NAME_0, ONE_SECONDS, TWO_SECONDS),
            ONE_SECONDS
        )
        timeKeeper.adjustTimer(
            AdjustTimerEntity(SAMPLE_NAME_0, ONE_SECONDS, FIVE_SECONDS),
            ONE_SECONDS
        )
        timeKeeper.adjustTimer(
            AdjustTimerEntity(SAMPLE_NAME_0, ONE_SECONDS, TWO_SECONDS),
            ONE_SECONDS
        )
        timeKeeper.notify(SAMPLE_NAME_0)



        assertEquals("2", actualValue)
    }

    @Test
    fun `given a start command, should tune time`() {
        val createScreenTimerEntity = getSampleCreateScreenTimerEntity(
            ScreenTimerFormat.SECONDS,
            0L,
            1000L
        )
        timeKeeper.createTimer(createScreenTimerEntity)
        var actualValue = ""
        timeKeeper.observe(SAMPLE_NAME_0) { actualValue = it.second }

        timeKeeper.startTimer(StartTimerEntity(SAMPLE_NAME_0, TWO_SECONDS), TWENTY_FIVE_SECONDS)
        timeKeeper.notify(SAMPLE_NAME_0)


        assertEquals("23", actualValue)
    }


    @Test
    fun `given direction=up, should increase time`() {
        val createScreenTimerEntity = getSampleCreateScreenTimerEntity(
            ScreenTimerFormat.SECONDS,
            ScreenTimerDirection.UP,
            30000L
        )
        timeKeeper.createTimer(createScreenTimerEntity)
        var actualValue = ""
        timeKeeper.observe(SAMPLE_NAME_0) { actualValue = it.second }


        timeKeeper.startTimer(StartTimerEntity(SAMPLE_NAME_0, FIFTEEN_SECONDS), SIXTY_SECONDS)
        timeKeeper.notify(SAMPLE_NAME_0)



        assertEquals("75", actualValue)
    }

    @Test
    fun `given direction=down, should decrease time`() {
        val createScreenTimerEntity = getSampleCreateScreenTimerEntity(
            ScreenTimerFormat.SECONDS,
            ScreenTimerDirection.DOWN,
            30000L,
            0L
        )
        timeKeeper.createTimer(createScreenTimerEntity)
        var actualValue = ""
        timeKeeper.observe(SAMPLE_NAME_0) { actualValue = it.second }


        timeKeeper.startTimer(StartTimerEntity(SAMPLE_NAME_0, FIFTEEN_SECONDS), TWENTY_FIVE_SECONDS)
        timeKeeper.notify(SAMPLE_NAME_0)


        assertEquals("20", actualValue)
    }

    @Test
    fun `given capValue with direction=up, should not exceed above capValue`() {
        val createScreenTimerEntity = getSampleCreateScreenTimerEntity(
            ScreenTimerFormat.SECONDS,
            ScreenTimerDirection.UP,
            0L,
            30000L

        )
        timeKeeper.createTimer(createScreenTimerEntity)
        var actualValue = ""
        timeKeeper.observe(SAMPLE_NAME_0) { actualValue = it.second }


        timeKeeper.startTimer(StartTimerEntity(SAMPLE_NAME_0, ONE_SECONDS), SIXTY_SECONDS)
        timeKeeper.notify(SAMPLE_NAME_0)



        assertEquals("30", actualValue)
    }

    @Test
    fun `given capValue with direction=down, should not exceed below capValue`() {
        val createScreenTimerEntity = getSampleCreateScreenTimerEntity(
            ScreenTimerFormat.SECONDS,
            ScreenTimerDirection.DOWN,
            50000L,
            30000L

        )
        timeKeeper.createTimer(createScreenTimerEntity)
        var actualValue = ""
        timeKeeper.observe(SAMPLE_NAME_0) { actualValue = it.second }


        timeKeeper.startTimer(StartTimerEntity(SAMPLE_NAME_0, ONE_SECONDS), SIXTY_SECONDS)
        timeKeeper.notify(SAMPLE_NAME_0)


        assertEquals("30", actualValue)
    }

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
}