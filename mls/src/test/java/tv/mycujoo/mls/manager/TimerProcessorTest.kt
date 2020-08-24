package tv.mycujoo.mls.manager

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.never
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import tv.mycujoo.mls.model.ScreenTimerDirection
import tv.mycujoo.mls.model.ScreenTimerFormat
import tv.mycujoo.mls.entity.AdjustTimerEntity
import tv.mycujoo.mls.entity.CreateTimerEntity
import tv.mycujoo.mls.entity.StartTimerEntity
import tv.mycujoo.mls.widgets.TimerCollection

@ExperimentalCoroutinesApi
class TimerProcessorTest {


    @Mock
    lateinit var timerKeeper: TimerKeeper

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
    }


    @Test
    fun `given Create command before current time, should call create timer`() {
        val createTimerEntity = getSampleCreateTimerEntity(ZERO_SECONDS)
        val timerProcessor = TimerProcessor(
            getSampleTimerCollection(createTimerEntity),
            timerKeeper,
            arrayListOf()
        )


        timerProcessor.process(ONE_SECONDS)


        Mockito.verify(timerKeeper).createTimer(createTimerEntity)
    }

    @Test
    fun `given Create command on current time, should call create timer`() {
        val createTimerEntity = getSampleCreateTimerEntity(ONE_SECONDS)
        val timerProcessor = TimerProcessor(
            getSampleTimerCollection(createTimerEntity),
            timerKeeper,
            arrayListOf()
        )


        timerProcessor.process(ONE_SECONDS)


        Mockito.verify(timerKeeper).createTimer(createTimerEntity)
    }

    @Test
    fun `given Create command after current time, should not call create timer`() {
        val createTimerEntity = getSampleCreateTimerEntity(ONE_SECONDS)
        val timerProcessor = TimerProcessor(
            getSampleTimerCollection(createTimerEntity),
            timerKeeper,
            arrayListOf()
        )


        timerProcessor.process(999)


        Mockito.verify(timerKeeper, never()).createTimer(any())
    }

    @Test
    fun `given Start command before current time, should call start timer`() {
        val startTimerEntity = getSampleStartTimerEntity(ONE_SECONDS)

        val timerProcessor = TimerProcessor(
            getSampleTimerCollection(startTimerEntity),
            timerKeeper,
            arrayListOf()
        )


        timerProcessor.process(TWO_SECONDS)


        Mockito.verify(timerKeeper).startTimer(startTimerEntity, TWO_SECONDS)
    }

    @Test
    fun `given Start command on current time, should call start timer`() {
        val startTimerEntity = getSampleStartTimerEntity(ONE_SECONDS)

        val timerProcessor = TimerProcessor(
            getSampleTimerCollection(startTimerEntity),
            timerKeeper,
            arrayListOf()
        )


        timerProcessor.process(ONE_SECONDS)


        Mockito.verify(timerKeeper).startTimer(startTimerEntity, ONE_SECONDS)
    }

    @Test
    fun `given Start command after current time, should call start timer`() {
        val startTimerEntity = getSampleStartTimerEntity(ONE_SECONDS)

        val timerProcessor = TimerProcessor(
            getSampleTimerCollection(startTimerEntity),
            timerKeeper,
            arrayListOf()
        )


        timerProcessor.process(999L)


        Mockito.verify(timerKeeper, never()).startTimer(any(), any())
    }


    @Test
    fun `given Adjust command before current time, should call adjust timer`() {
        val adjustTimerEntity = getSampleAdjustTimerEntity(ONE_SECONDS, FIVE_SECONDS)

        val timerProcessor = TimerProcessor(
            getSampleTimerCollection(adjustTimerEntity),
            timerKeeper,
            arrayListOf()
        )


        timerProcessor.process(TWO_SECONDS)


        Mockito.verify(timerKeeper).adjustTimer(adjustTimerEntity, TWO_SECONDS)
    }

    @Test
    fun `given Adjust command on current time, should call adjust timer`() {
        val adjustTimerEntity = getSampleAdjustTimerEntity(ONE_SECONDS, FIVE_SECONDS)

        val timerProcessor = TimerProcessor(
            getSampleTimerCollection(adjustTimerEntity),
            timerKeeper,
            arrayListOf()
        )


        timerProcessor.process(ONE_SECONDS)


        Mockito.verify(timerKeeper).adjustTimer(adjustTimerEntity, ONE_SECONDS)
    }

    @Test
    fun `given Adjust command after current time, should not call adjust timer`() {
        val adjustTimerEntity = getSampleAdjustTimerEntity(ONE_SECONDS, FIVE_SECONDS)

        val timerProcessor = TimerProcessor(
            getSampleTimerCollection(adjustTimerEntity),
            timerKeeper,
            arrayListOf()
        )


        timerProcessor.process(999L)


        Mockito.verify(timerKeeper, never()).adjustTimer(any(), any())
    }


    @Test
    fun `given out of range timer, should kill it`() {


        Mockito.verify(timerKeeper, never()).killTimer(SAMPLE_NAME_0)
    }

    /**region */

    private fun getSampleCreateTimerEntity(offset: Long): CreateTimerEntity {

        return CreateTimerEntity(
            SAMPLE_NAME_0,
            offset,
            ScreenTimerFormat.SECONDS,
            ScreenTimerDirection.UP,
            offset,
            ONE_SECONDS,
            1000000L
        )
    }

    private fun getSampleStartTimerEntity(offset: Long): StartTimerEntity {

        return StartTimerEntity(
            SAMPLE_NAME_0,
            offset
        )
    }

    private fun getSampleAdjustTimerEntity(offset: Long, value: Long): AdjustTimerEntity {
        return AdjustTimerEntity(
            SAMPLE_NAME_0,
            offset,
            value
        )
    }

    private fun getSampleTimerCollection(createTimerEntity: CreateTimerEntity): List<TimerCollection> {

        val timerCollection = TimerCollection(
            SAMPLE_NAME_0,
            createTimerEntity,
            arrayListOf(),
            arrayListOf(),
            arrayListOf(),
            arrayListOf()
        )

        return listOf(timerCollection)
    }

    private fun getSampleTimerCollection(startTimerEntity: StartTimerEntity): List<TimerCollection> {

        val fakeCreateTimerEntity = CreateTimerEntity(
            "fakeCreateTimerEntity",
            -1L,
            ScreenTimerFormat.SECONDS,
            ScreenTimerDirection.UP,
            -1L,
            ONE_SECONDS,
            -1L
        )

        val timerCollection = TimerCollection(
            SAMPLE_NAME_0,
            fakeCreateTimerEntity,
            arrayListOf(startTimerEntity),
            arrayListOf(),
            arrayListOf(),
            arrayListOf()
        )

        return listOf(timerCollection)
    }

    private fun getSampleTimerCollection(adjustTimerEntity: AdjustTimerEntity): List<TimerCollection> {

        val fakeCreateTimerEntity = CreateTimerEntity(
            "fakeCreateTimerEntity",
            -1L,
            ScreenTimerFormat.SECONDS,
            ScreenTimerDirection.UP,
            -1L,
            ONE_SECONDS,
            -1L
        )

        val timerCollection = TimerCollection(
            SAMPLE_NAME_0,
            fakeCreateTimerEntity,
            arrayListOf(),
            arrayListOf(),
            arrayListOf(adjustTimerEntity),
            arrayListOf()
        )

        return listOf(timerCollection)
    }

    /**endregion */


    companion object {
        const val SAMPLE_NAME_0 = "sample_name_0"

        private const val INVALID = -1L
        private const val ZERO_SECONDS = 0L
        private const val ONE_SECONDS = 1000L
        private const val TWO_SECONDS = 1000L
        private const val FIVE_SECONDS = 5000L
        private const val FIFTEEN_SECONDS = 15000L
        private const val TWENTY_FIVE_SECONDS = 25000L
    }
}