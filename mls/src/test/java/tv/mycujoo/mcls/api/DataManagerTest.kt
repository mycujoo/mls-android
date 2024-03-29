package tv.mycujoo.mcls.api

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.resetMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import tv.mycujoo.domain.entity.*
import tv.mycujoo.domain.repository.IEventsRepository
import tv.mycujoo.domain.usecase.GetActionsUseCase
import tv.mycujoo.domain.usecase.GetEventDetailUseCase
import tv.mycujoo.domain.usecase.GetEventsUseCase
import tv.mycujoo.mcls.CoroutineTestRule
import tv.mycujoo.mcls.enum.LogLevel
import tv.mycujoo.mcls.manager.Logger
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
class DataManagerTest {

    private lateinit var dataManager: DataManager

    @get:Rule
    var coroutineTestRule = CoroutineTestRule()

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Mock
    lateinit var getEventDetailUseCase: GetEventDetailUseCase

    @Mock
    lateinit var getActionsUseCase: GetActionsUseCase

    @Mock
    lateinit var getEventsUseCase: GetEventsUseCase


    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)

        val scope = TestCoroutineScope()
        dataManager = DataManager(
            scope,
            Logger(LogLevel.MINIMAL),
            getEventDetailUseCase,
            getActionsUseCase,
            getEventsUseCase
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }


    @Test
    fun `given event list on response of get event, should return in callback`() = runBlocking {
        whenever(getEventsUseCase.execute(any())).thenReturn(
            Result.Success(
                getSampleEvents(
                    null,
                    null
                )
            )
        )


        val eventEntityArrayList = ArrayList<EventEntity>()
        dataManager.fetchEvents(
            2,
            null,
            listOf(EventStatus.EVENT_STATUS_SCHEDULED, EventStatus.EVENT_STATUS_CANCELLED),
            OrderByEventsParam.ORDER_TITLE_ASC
        ) { eventList, _, _ -> eventEntityArrayList.addAll(eventList) }


        assertEquals(2, eventEntityArrayList.size)
    }

    @Test
    fun `given previousPageToken on response of get event, should return in callback`() =
        runBlocking {
            whenever(getEventsUseCase.execute(any())).thenReturn(
                Result.Success(
                    getSampleEvents(SAMPLE_PREVIOUS_PAGE_TOKEN, null)
                )
            )


            var result = ""
            dataManager.fetchEvents(
                2,
                null,
                listOf(EventStatus.EVENT_STATUS_SCHEDULED, EventStatus.EVENT_STATUS_CANCELLED),
                OrderByEventsParam.ORDER_TITLE_ASC
            ) { _, previousPageToken, _ -> result = previousPageToken }


            assertEquals(SAMPLE_PREVIOUS_PAGE_TOKEN, result)
        }

    @Test
    fun `given nextPageToken on response of get event, should return in callback`() = runBlocking {
        whenever(getEventsUseCase.execute(any())).thenReturn(
            Result.Success(
                getSampleEvents(null, SAMPLE_NEXT_PAGE_TOKEN)
            )
        )


        var result = ""
        dataManager.fetchEvents(
            2,
            null,
            listOf(EventStatus.EVENT_STATUS_SCHEDULED, EventStatus.EVENT_STATUS_CANCELLED),
            OrderByEventsParam.ORDER_TITLE_ASC
        ) { _, _, nextPageToken -> result = nextPageToken }


        assertEquals(SAMPLE_NEXT_PAGE_TOKEN, result)
    }

    @Test
    fun `given no event in event list on response of get event, should not add to callback`() =
        runBlocking {
            val eventEntityArrayList = ArrayList<EventEntity>()

            dataManager.fetchEvents(
                0,
                null,
                listOf(EventStatus.EVENT_STATUS_SCHEDULED, EventStatus.EVENT_STATUS_CANCELLED),
                OrderByEventsParam.ORDER_TITLE_ASC
            ) { eventList, _, _ -> eventEntityArrayList.addAll(eventList) }


            assertEquals(0, eventEntityArrayList.size)
        }

    @Test
    fun `given new data, should call live data`() = runBlocking {
        whenever(getEventsUseCase.execute(any())).then {
            dataManager.getEventsLiveData().value =
                listOf(getSampleEventEntity(), getSampleEventEntity())
            true
        }


        dataManager.fetchEvents(
            2,
            null,
            listOf(EventStatus.EVENT_STATUS_SCHEDULED, EventStatus.EVENT_STATUS_CANCELLED),
            OrderByEventsParam.ORDER_TITLE_ASC
        )

        assertEquals(2, dataManager.getEventsLiveData().value?.size)
    }

    @Test
    fun `calling fetch event details, should call on repository`() = runBlocking {
        whenever(getEventDetailUseCase.execute(any())).thenReturn(
            Result.Success(
                getSampleEventEntity()
            )
        )

        val event = getSampleEventEntity()

        val result = dataManager.getEventDetails(event.id)
        assert(result is Result.Success)
        assertEquals(event.id, (result as Result.Success).value.id)
        assertEquals(event.description, result.value.description)

    }

    private fun getSampleEventEntity(): EventEntity {
        val location = Location(Physical("", "", Coordinates(0.toDouble(), 0.toDouble()), "", ""))
        return EventEntity(
            "42",
            "",
            "",
            "",
            null,
            location,
            "",
            null,
            EventStatus.EVENT_STATUS_UNSPECIFIED,
            emptyList(),
            "",
            emptyList(),
            Metadata(),
            false
        )
    }

    private fun getSampleEvents(previousPageToken: String?, nextPageToken: String?): Events {
        return Events(
            listOf(getSampleEventEntity(), getSampleEventEntity()),
            previousPageToken,
            nextPageToken
        )
    }


    companion object {
        const val SAMPLE_PREVIOUS_PAGE_TOKEN = "sample_previous_page_token"
        const val SAMPLE_NEXT_PAGE_TOKEN = "sample_next_page_token"
    }

}
