package tv.mycujoo.mls.manager

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScope
import org.junit.Before
import org.junit.Test
import tv.mycujoo.mls.model.ScreenTimerDirection
import tv.mycujoo.mls.model.ScreenTimerFormat
import tv.mycujoo.mls.widgets.CreateTimerEntity
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
    fun `given startTime of 0 milli-sec & SECOND format, should return '0'`() {

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
    fun `given startTime of 10000 milli-sec & SECOND format, should return '10'`() {

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
    fun `given startTime of 60000 milli-sec & SECOND format, should return '60'`() {

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
    fun `given startTime of 1200000 milli-sec & SECOND format, should return '120'`() {

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
    fun `given startTime of 0 milli-sec & MINTUES_SECOND format, should return '0_00'`() {

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
    fun `given startTime of 10000 milli-sec & MINTUES_SECOND format, should return '0_10'`() {

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
    fun `given startTime of 60000 milli-sec & MINTUES_SECOND format, should return '1_00'`() {

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
    fun `given startTime of 120000 milli-sec & MINTUES_SECOND format, should return '2_00'`() {

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
    fun `given startTime of 10000, step of 1000, and 5 second, should display '15'`() {
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
    fun `given startTime of 10000, step of 1000, and 60 second, should display '70'`() {
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
}