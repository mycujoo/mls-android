package tv.mycujoo.mls.manager

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScope
import org.junit.Before
import org.junit.Test
import tv.mycujoo.mls.model.ScreenTimerDirection
import tv.mycujoo.mls.model.ScreenTimerFormat
import tv.mycujoo.mls.widgets.CreateTimerEntity
import tv.mycujoo.mls.widgets.StartTimerEntity
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
class TimerCoreTest {

    private lateinit var timeKeeper: TimeKeeper
    private lateinit var testCoroutineScope: TestCoroutineScope

    private val sampleName = "name_0"


    @Before
    fun setUp() {
        testCoroutineScope = TestCoroutineScope()
        timeKeeper = TimeKeeper(testCoroutineScope)
    }


    @Test
    fun `given startTime of 0ms & SECOND format, should return '0'`() {

        val createScreenTimerEntity = getSampleCreateScreenTimerEntity(
            ScreenTimerFormat.SECONDS,
            0L,
            1000L
        )
        timeKeeper.createTimer(createScreenTimerEntity)
        var actualValue = ""
        timeKeeper.observe(sampleName) { actualValue = it.second }
        timeKeeper.startTimer(sampleName)


        assertEquals("0", actualValue)
    }

    @Test
    fun `given startTime of 10000ms & SECOND format, should return '10'`() {

        val createScreenTimerEntity = getSampleCreateScreenTimerEntity(
            ScreenTimerFormat.SECONDS,
            10000L,
            1000L
        )
        timeKeeper.createTimer(createScreenTimerEntity)
        var actualValue = ""
        timeKeeper.observe(sampleName) { actualValue = it.second }
        timeKeeper.startTimer(sampleName)


        assertEquals("10", actualValue)
    }

    @Test
    fun `given startTime of 60000ms & SECOND format, should return '60'`() {

        val createScreenTimerEntity = getSampleCreateScreenTimerEntity(
            ScreenTimerFormat.SECONDS,
            60000L,
            1000L
        )
        timeKeeper.createTimer(createScreenTimerEntity)
        var actualValue = ""
        timeKeeper.observe(sampleName) { actualValue = it.second }
        timeKeeper.startTimer(sampleName)


        assertEquals("60", actualValue)
    }

    @Test
    fun `given startTime of 1200000ms & SECOND format, should return '120'`() {

        val createScreenTimerEntity = getSampleCreateScreenTimerEntity(
            ScreenTimerFormat.SECONDS,
            120000L,
            1000L
        )
        timeKeeper.createTimer(createScreenTimerEntity)
        var actualValue = ""
        timeKeeper.observe(sampleName) { actualValue = it.second }
        timeKeeper.startTimer(sampleName)


        assertEquals("120", actualValue)
    }


    @Test
    fun `given startTime of 0ms & MINTUES_SECOND format, should return '0_00'`() {

        val createScreenTimerEntity = getSampleCreateScreenTimerEntity(
            ScreenTimerFormat.MINUTES_SECONDS,
            0L,
            1000L
        )
        timeKeeper.createTimer(createScreenTimerEntity)
        var actualValue = ""
        timeKeeper.observe(sampleName) { actualValue = it.second }
        timeKeeper.startTimer(sampleName)


        assertEquals("0:00", actualValue)
    }

    @Test
    fun `given startTime of 10000ms & MINTUES_SECOND format, should return '0_10'`() {

        val createScreenTimerEntity = getSampleCreateScreenTimerEntity(
            ScreenTimerFormat.MINUTES_SECONDS,
            10000L,
            1000L
        )
        timeKeeper.createTimer(createScreenTimerEntity)
        var actualValue = ""
        timeKeeper.observe(sampleName) { actualValue = it.second }
        timeKeeper.startTimer(sampleName)


        assertEquals("0:10", actualValue)
    }

    @Test
    fun `given startTime of 60000ms & MINTUES_SECOND format, should return '1_00'`() {

        val createScreenTimerEntity = getSampleCreateScreenTimerEntity(
            ScreenTimerFormat.MINUTES_SECONDS,
            60000L,
            1000L
        )
        timeKeeper.createTimer(createScreenTimerEntity)
        var actualValue = ""
        timeKeeper.observe(sampleName) { actualValue = it.second }
        timeKeeper.startTimer(sampleName)


        assertEquals("1:00", actualValue)
    }

    @Test
    fun `given startTime of 120000ms & MINTUES_SECOND format, should return '2_00'`() {

        val createScreenTimerEntity = getSampleCreateScreenTimerEntity(
            ScreenTimerFormat.MINUTES_SECONDS,
            120000L,
            1000L
        )
        timeKeeper.createTimer(createScreenTimerEntity)
        var actualValue = ""
        timeKeeper.observe(sampleName) { actualValue = it.second }
        timeKeeper.startTimer(sampleName)


        assertEquals("2:00", actualValue)
    }

    @Test
    fun `given startTime of 10000ms, step of 1000, and 5 second, should display '15'`() {
        val createScreenTimerEntity = getSampleCreateScreenTimerEntity(
            ScreenTimerFormat.SECONDS,
            10000L,
            1000L
        )
        timeKeeper.createTimer(createScreenTimerEntity)
        var actualValue = ""
        timeKeeper.observe(sampleName) { actualValue = it.second }
        timeKeeper.startTimer(sampleName)

        testCoroutineScope.advanceTimeBy(3000)


        assertEquals("13", actualValue)
    }

    @Test
    fun `given startTime of 10000ms, step of 1000, and 60 second, should display '70'`() {
        val createScreenTimerEntity = getSampleCreateScreenTimerEntity(
            ScreenTimerFormat.SECONDS,
            10000L,
            1000L
        )
        timeKeeper.createTimer(createScreenTimerEntity)
        var actualValue = ""
        timeKeeper.observe(sampleName) { actualValue = it.second }
        timeKeeper.startTimer(sampleName)

        testCoroutineScope.advanceTimeBy(60000)


        assertEquals("70", actualValue)
    }

    @Test
    fun `given multiple start commands, should only take effect for the first one`() {
        val createScreenTimerEntity = getSampleCreateScreenTimerEntity(
            ScreenTimerFormat.SECONDS,
            0L,
            1000L
        )
        timeKeeper.createTimer(createScreenTimerEntity)
        var actualValue = ""

        timeKeeper.observe(sampleName) { actualValue = it.second }
        timeKeeper.startTimer(sampleName)

        timeKeeper.startTimer(sampleName)
        timeKeeper.startTimer(sampleName)

        testCoroutineScope.advanceTimeBy(60000)


        assertEquals("60", actualValue)
    }

    @Test
    fun `given pause command, should not change time afterward`() {
        val createScreenTimerEntity = getSampleCreateScreenTimerEntity(
            ScreenTimerFormat.SECONDS,
            0L,
            1000L
        )
        timeKeeper.createTimer(createScreenTimerEntity)
        var actualValue = ""

        timeKeeper.observe(sampleName) { actualValue = it.second }
        timeKeeper.startTimer(sampleName)

        testCoroutineScope.advanceTimeBy(60000)

        timeKeeper.pauseTimer(sampleName)
        val valueBeforePauseCommand = actualValue

        testCoroutineScope.advanceTimeBy(60000)


        assertEquals("60", valueBeforePauseCommand)
    }


    @Test
    fun `given resume command after pause command, should continue changing time`() {
        val createScreenTimerEntity = getSampleCreateScreenTimerEntity(
            ScreenTimerFormat.SECONDS,
            0L,
            1000L
        )
        timeKeeper.createTimer(createScreenTimerEntity)
        var actualValue = ""

        timeKeeper.observe(sampleName) { actualValue = it.second }
        timeKeeper.startTimer(sampleName)

        testCoroutineScope.advanceTimeBy(60000)


        timeKeeper.pauseTimer(sampleName)
        timeKeeper.resumeTimer(sampleName)

        testCoroutineScope.advanceTimeBy(60000)


        assertEquals("120", actualValue)
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

        timeKeeper.observe(sampleName) { actualValue = it.second }


        timeKeeper.adjustTime(sampleName, 10000L)


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

        timeKeeper.observe(sampleName) { actualValue = it.second }


        timeKeeper.adjustTime(sampleName, 10000L)
        timeKeeper.adjustTime(sampleName, 20000L)
        timeKeeper.adjustTime(sampleName, 30000L)
        timeKeeper.adjustTime(sampleName, 20000L)


        assertEquals("20", actualValue)
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
        timeKeeper.observe(sampleName) { actualValue = it.second }


        timeKeeper.tuneWithStartEntity(sampleName, StartTimerEntity(sampleName, 0L), 20000L)


        assertEquals("20", actualValue)
    }

    @Test
    fun `given multiple start commands, should return last one`() {
        val createScreenTimerEntity = getSampleCreateScreenTimerEntity(
            ScreenTimerFormat.SECONDS,
            0L,
            1000L
        )
        timeKeeper.createTimer(createScreenTimerEntity)
        var actualValue = ""
        timeKeeper.observe(sampleName) { actualValue = it.second }


        timeKeeper.tuneWithStartEntity(sampleName, StartTimerEntity(sampleName, 0L), 10000L)
        timeKeeper.tuneWithStartEntity(sampleName, StartTimerEntity(sampleName, 0L), 20000L)
        timeKeeper.tuneWithStartEntity(sampleName, StartTimerEntity(sampleName, 0L), 30000L)


        assertEquals("30", actualValue)
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
        timeKeeper.observe(sampleName) { actualValue = it.second }
        timeKeeper.startTimer(sampleName)


        testCoroutineScope.advanceTimeBy(30000L)


        assertEquals("60", actualValue)
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
        timeKeeper.observe(sampleName) { actualValue = it.second }
        timeKeeper.startTimer(sampleName)

        testCoroutineScope.advanceTimeBy(30000L)

        assertEquals("0", actualValue)
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
        timeKeeper.observe(sampleName) { actualValue = it.second }
        timeKeeper.startTimer(sampleName)


        testCoroutineScope.advanceTimeBy(50000L)


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
        timeKeeper.observe(sampleName) { actualValue = it.second }
        timeKeeper.startTimer(sampleName)


        testCoroutineScope.advanceTimeBy(50000L)


        assertEquals("30", actualValue)
    }

    @Test
    fun `given capValue with direction=up, when time passes beyond capValue, should return capValue on direct getTime() calls`() {
        val createScreenTimerEntity = getSampleCreateScreenTimerEntity(
            ScreenTimerFormat.SECONDS,
            ScreenTimerDirection.UP,
            0L,
            30000L

        )
        timeKeeper.createTimer(createScreenTimerEntity)
        timeKeeper.startTimer(sampleName)


        testCoroutineScope.advanceTimeBy(50000L)
        val actualValue = timeKeeper.getValue(sampleName)


        assertEquals("30", actualValue)
    }

    @Test
    fun `given capValue with direction=down, when time passes below capValue, should return capValue on direct getTime() calls`() {
        val createScreenTimerEntity = getSampleCreateScreenTimerEntity(
            ScreenTimerFormat.SECONDS,
            ScreenTimerDirection.DOWN,
            40000L,
            30000L

        )
        timeKeeper.createTimer(createScreenTimerEntity)
        timeKeeper.startTimer(sampleName)


        testCoroutineScope.advanceTimeBy(50000L)
        val actualValue = timeKeeper.getValue(sampleName)


        assertEquals("30", actualValue)
    }

    private fun getSampleCreateScreenTimerEntity(
        format: ScreenTimerFormat,
        startValue: Long,
        step: Long
    ): CreateTimerEntity {
        return CreateTimerEntity(
            sampleName,
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
            sampleName,
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
            sampleName,
            1000L,
            format,
            screenTimerDirection,
            startValue,
            1000L,
            capValue
        )
    }
}