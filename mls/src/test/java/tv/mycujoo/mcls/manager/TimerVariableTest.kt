package tv.mycujoo.mcls.manager

import org.junit.Before
import org.junit.Test
import tv.mycujoo.mcls.model.ScreenTimerDirection
import tv.mycujoo.mcls.model.ScreenTimerFormat
import kotlin.test.assertEquals

class TimerVariableTest {

    @Before
    fun setUp() {
    }

    @Test
    fun `start timer`() {
        val timerVariable = TimerVariable(
            "sample_name",
            ScreenTimerFormat.MINUTES_SECONDS,
            ScreenTimerDirection.UP,
            0L,
            -1L
        )


        timerVariable.start(TimerEntity.StartTimer("sample_name", 3000L), 4000L)


        assertEquals("0:01", timerVariable.getTime())
    }

    @Test
    fun `start timer with start-value`() {
        val timerVariable = TimerVariable(
            "sample_name",
            ScreenTimerFormat.MINUTES_SECONDS,
            ScreenTimerDirection.UP,
            10000L,
            -1L
        )


        timerVariable.start(TimerEntity.StartTimer("sample_name", 3000L), 4000L)


        assertEquals("0:11", timerVariable.getTime())
    }


    @Test
    fun `start timer down with start-value`() {
        val timerVariable = TimerVariable(
            "sample_name",
            ScreenTimerFormat.MINUTES_SECONDS,
            ScreenTimerDirection.DOWN,
            10000L,
            -1L
        )


        timerVariable.start(TimerEntity.StartTimer("sample_name", 3000L), 4000L)


        assertEquals("0:09", timerVariable.getTime())
    }

    @Test
    fun `start timer multiple calls`() {
        val timerVariable = TimerVariable(
            "sample_name",
            ScreenTimerFormat.MINUTES_SECONDS,
            ScreenTimerDirection.UP,
            0L,
            -1L
        )


        timerVariable.start(TimerEntity.StartTimer("sample_name", 3000L), 4000L)
        timerVariable.start(TimerEntity.StartTimer("sample_name", 4000L), 8000L)


        assertEquals("0:05", timerVariable.getTime())
    }

    @Test
    fun `start timer multiple calls down`() {
        val timerVariable = TimerVariable(
            "sample_name",
            ScreenTimerFormat.MINUTES_SECONDS,
            ScreenTimerDirection.UP,
            10000L,
            -1L
        )


        timerVariable.start(TimerEntity.StartTimer("sample_name", 3000L), 4000L)
        timerVariable.start(TimerEntity.StartTimer("sample_name", 4000L), 8000L)


        assertEquals("0:15", timerVariable.getTime())
    }

    @Test
    fun `pause-timer`() {
        val timerVariable = TimerVariable(
            "sample_name",
            ScreenTimerFormat.MINUTES_SECONDS,
            ScreenTimerDirection.UP,
            0L,
            -1L
        )
        timerVariable.start(TimerEntity.StartTimer("sample_name", 3000L), 6000L)
        timerVariable.pause(TimerEntity.PauseTimer("sample_name", 4000L), 6000L)

        assertEquals("0:01", timerVariable.getTime())
    }

    @Test
    fun `pause-timer with start-value`() {
        val timerVariable = TimerVariable(
            "sample_name",
            ScreenTimerFormat.MINUTES_SECONDS,
            ScreenTimerDirection.UP,
            10000L,
            -1L
        )
        timerVariable.start(TimerEntity.StartTimer("sample_name", 3000L), 6000L)
        timerVariable.pause(TimerEntity.PauseTimer("sample_name", 4000L), 6000L)

        assertEquals("0:11", timerVariable.getTime())
    }

    @Test
    fun `pause-timer down`() {
        val timerVariable = TimerVariable(
            "sample_name",
            ScreenTimerFormat.MINUTES_SECONDS,
            ScreenTimerDirection.DOWN,
            10000L,
            -1L
        )
        timerVariable.start(TimerEntity.StartTimer("sample_name", 3000L), 6000L)
        timerVariable.pause(TimerEntity.PauseTimer("sample_name", 4000L), 6000L)

        assertEquals("0:09", timerVariable.getTime())
    }

    @Test
    fun `pause-timer multiple calls`() {
        val timerVariable = TimerVariable(
            "sample_name",
            ScreenTimerFormat.MINUTES_SECONDS,
            ScreenTimerDirection.UP,
            0L,
            -1L
        )

        timerVariable.start(TimerEntity.StartTimer("sample_name", 3000L), 7000L)
        timerVariable.pause(TimerEntity.PauseTimer("sample_name", 5000L), 7000L)
        timerVariable.pause(TimerEntity.PauseTimer("sample_name", 6000L), 7000L)

        assertEquals("0:02", timerVariable.getTime())
    }

    @Test
    fun `adjust-timer`() {
        val timerVariable = TimerVariable(
            "sample_name",
            ScreenTimerFormat.MINUTES_SECONDS,
            ScreenTimerDirection.UP,
            0L,
            -1L
        )
        timerVariable.start(TimerEntity.StartTimer("sample_name", 3000L), 6000L)
        timerVariable.adjust(TimerEntity.AdjustTimer("sample_name", 4000L, 20000L), 6000L)

        assertEquals("0:22", timerVariable.getTime())
    }

    @Test
    fun `adjust-timer down`() {
        val timerVariable = TimerVariable(
            "sample_name",
            ScreenTimerFormat.MINUTES_SECONDS,
            ScreenTimerDirection.DOWN,
            40000L,
            -1L
        )
        timerVariable.start(TimerEntity.StartTimer("sample_name", 3000L), 6000L)
        timerVariable.adjust(TimerEntity.AdjustTimer("sample_name", 4000L, 20000L), 6000L)

        assertEquals("0:18", timerVariable.getTime())
    }

    @Test
    fun `skip-timer`() {
        val timerVariable = TimerVariable(
            "sample_name",
            ScreenTimerFormat.MINUTES_SECONDS,
            ScreenTimerDirection.UP,
            0L,
            -1L
        )
        timerVariable.start(TimerEntity.StartTimer("sample_name", 3000L), 6000L)
        timerVariable.skip(TimerEntity.SkipTimer("sample_name", 4000L, 20000L), 6000L)

        assertEquals("0:23", timerVariable.getTime())
    }

    @Test
    fun `skip-timer down`() {
        val timerVariable = TimerVariable(
            "sample_name",
            ScreenTimerFormat.MINUTES_SECONDS,
            ScreenTimerDirection.DOWN,
            40000L,
            -1L
        )
        timerVariable.start(TimerEntity.StartTimer("sample_name", 3000L), 6000L)
        timerVariable.skip(TimerEntity.SkipTimer("sample_name", 4000L, 20000L), 6000L)

        assertEquals("0:17", timerVariable.getTime())
    }

    @Test
    fun `start-value test`() {
        val timerVariable = TimerVariable(
            "sample_name",
            ScreenTimerFormat.MINUTES_SECONDS,
            ScreenTimerDirection.UP,
            5000L,
            -1L
        )


        timerVariable.start(TimerEntity.StartTimer("sample_name", 3000L), 15000L)


        assertEquals("0:17", timerVariable.getTime())
    }

    @Test
    fun `start-value down test`() {
        val timerVariable = TimerVariable(
            "sample_name",
            ScreenTimerFormat.MINUTES_SECONDS,
            ScreenTimerDirection.DOWN,
            15000L,
            -1L
        )


        timerVariable.start(TimerEntity.StartTimer("sample_name", 3000L), 15000L)


        assertEquals("0:03", timerVariable.getTime())
    }

    @Test
    fun `cap-value test`() {
        val timerVariable = TimerVariable(
            "sample_name",
            ScreenTimerFormat.MINUTES_SECONDS,
            ScreenTimerDirection.UP,
            0L,
            10000L
        )


        timerVariable.start(TimerEntity.StartTimer("sample_name", 3000L), 15000L)


        assertEquals("0:10", timerVariable.getTime())
    }


    @Test
    fun `cap-value down test`() {
        val timerVariable = TimerVariable(
            "sample_name",
            ScreenTimerFormat.MINUTES_SECONDS,
            ScreenTimerDirection.DOWN,
            0L,
            -10000L
        )


        timerVariable.start(TimerEntity.StartTimer("sample_name", 3000L), 15000L)


        assertEquals("0:-10", timerVariable.getTime())
    }

    @Test
    fun `one pair of start-pause`() {
        val timerVariable = TimerVariable(
            "sample_name",
            ScreenTimerFormat.MINUTES_SECONDS,
            ScreenTimerDirection.UP,
            0L,
            59940000L
        )


        timerVariable.start(TimerEntity.StartTimer("sample_name", 3000L), 10000L)
        timerVariable.pause(TimerEntity.PauseTimer("sample_name", 4000L), 10000L)


        assertEquals("0:01", timerVariable.getTime())
    }

    @Test
    fun `two pairs of start-pause`() {
        val timerVariable = TimerVariable(
            "sample_name",
            ScreenTimerFormat.MINUTES_SECONDS,
            ScreenTimerDirection.UP,
            0L,
            59940000L
        )


        timerVariable.start(TimerEntity.StartTimer("sample_name", 2000L), 10000L)
        timerVariable.pause(TimerEntity.PauseTimer("sample_name", 4000L), 10000L)
        timerVariable.start(TimerEntity.StartTimer("sample_name", 5000L), 10000L)
        timerVariable.pause(TimerEntity.PauseTimer("sample_name", 8000L), 10000L)


        assertEquals("0:05", timerVariable.getTime())
    }
}