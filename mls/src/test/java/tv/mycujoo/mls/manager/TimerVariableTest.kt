package tv.mycujoo.mls.manager

import org.junit.Before
import org.junit.Test
import tv.mycujoo.mls.entity.AdjustTimerEntity
import tv.mycujoo.mls.entity.PauseTimerEntity
import tv.mycujoo.mls.entity.SkipTimerEntity
import tv.mycujoo.mls.entity.StartTimerEntity
import tv.mycujoo.mls.model.ScreenTimerDirection
import tv.mycujoo.mls.model.ScreenTimerFormat
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


        timerVariable.start(StartTimerEntity("sample_name", 3000L), 4000L)


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


        timerVariable.start(StartTimerEntity("sample_name", 3000L), 4000L)


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


        timerVariable.start(StartTimerEntity("sample_name", 3000L), 4000L)


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


        timerVariable.start(StartTimerEntity("sample_name", 3000L), 4000L)
        timerVariable.start(StartTimerEntity("sample_name", 3000L), 8000L)


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


        timerVariable.start(StartTimerEntity("sample_name", 3000L), 4000L)
        timerVariable.start(StartTimerEntity("sample_name", 3000L), 8000L)


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
        timerVariable.start(StartTimerEntity("sample_name", 3000L), 6000L)
        timerVariable.pause(PauseTimerEntity("sample_name", 4000L), 6000L)

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
        timerVariable.start(StartTimerEntity("sample_name", 3000L), 6000L)
        timerVariable.pause(PauseTimerEntity("sample_name", 4000L), 6000L)

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
        timerVariable.start(StartTimerEntity("sample_name", 3000L), 6000L)
        timerVariable.pause(PauseTimerEntity("sample_name", 4000L), 6000L)

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

        timerVariable.start(StartTimerEntity("sample_name", 3000L), 7000L)
        timerVariable.pause(PauseTimerEntity("sample_name", 5000L), 7000L)
        timerVariable.pause(PauseTimerEntity("sample_name", 5000L), 7000L)

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
        timerVariable.start(StartTimerEntity("sample_name", 3000L), 6000L)
        timerVariable.adjust(AdjustTimerEntity("sample_name", 4000L, 20000L), 6000L)

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
        timerVariable.start(StartTimerEntity("sample_name", 3000L), 6000L)
        timerVariable.adjust(AdjustTimerEntity("sample_name", 4000L, 20000L), 6000L)

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
        timerVariable.start(StartTimerEntity("sample_name", 3000L), 6000L)
        timerVariable.skip(SkipTimerEntity("sample_name", 4000L, 20000L), 6000L)

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
        timerVariable.start(StartTimerEntity("sample_name", 3000L), 6000L)
        timerVariable.skip(SkipTimerEntity("sample_name", 4000L, 20000L), 6000L)

        assertEquals("0:17", timerVariable.getTime())
    }
}