package tv.mycujoo.mls.manager

import org.junit.Before
import org.junit.Test
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
}