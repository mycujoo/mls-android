package tv.mycujoo.mls.api

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.nhaarman.mockitokotlin2.whenever
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
import tv.mycujoo.domain.entity.*
import tv.mycujoo.domain.repository.EventsRepository
import tv.mycujoo.mls.CoroutineTestRule
import tv.mycujoo.mls.network.MlsApi
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
class DataManagerTest {

    private lateinit var dataManager: DataManager

    @get:Rule
    var coroutineTestRule = CoroutineTestRule()

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Mock
    lateinit var eventsRepository: EventsRepository


    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)

        val mlsApi = object : MlsApi {
            override suspend fun getEvents(
                pageSize: Int?,
                pageToken: String?,
                status: List<String>?,
                orderBy: String?
            ): Events {

                val eventEntityList = ArrayList<EventEntity>()

                repeat(pageSize!!, action = { eventEntityList.add(getSampleEventEntity()) })

                return Events(eventEntityList)
            }

            override suspend fun getEventDetails(id: String, updateId: String?): EventEntity {
                return getSampleEventEntity()
            }
        }


        val scope = TestCoroutineScope()
        dataManager = DataManager(scope, eventsRepository, mlsApi)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }


    @Test
    fun `given event list on response of get event, should return in callback`() = runBlocking {
        val eventEntityArrayList = ArrayList<EventEntity>()

        dataManager.fetchEvents(
            2,
            null,
            listOf(EventStatus.EVENT_STATUS_SCHEDULED, EventStatus.EVENT_STATUS_CANCELLED),
            OrderByEventsParam.ORDER_TITLE_ASC
        ) { list -> eventEntityArrayList.addAll(list) }


        assertEquals(2, eventEntityArrayList.size)
    }

    @Test
    fun `given no event in event list on response of get event, should not add to callback`() = runBlocking {
        val eventEntityArrayList = ArrayList<EventEntity>()

        dataManager.fetchEvents(
            0,
            null,
            listOf(EventStatus.EVENT_STATUS_SCHEDULED, EventStatus.EVENT_STATUS_CANCELLED),
            OrderByEventsParam.ORDER_TITLE_ASC
        ) { list -> eventEntityArrayList.addAll(list) }


        assertEquals(0, eventEntityArrayList.size)
    }

    @Test
    fun `given new data, should call live data`() {
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
        whenever(eventsRepository.getEventDetails(getSampleEventEntity().id)).thenReturn(
            Result.Success(
                getSampleEventEntity()
            )
        )

        val event = getSampleEventEntity()

        val result = dataManager.getEventDetails(event.id)
        assert(result is Result.Success)
        assertEquals(event.id, (result as Result.Success).value.id)
        assertEquals(event.description, (result as Result.Success).value.description)

    }

    private fun getSampleEventEntity(): EventEntity {
        val location = Location(Physical("", "", Coordinates(0.toDouble(), 0.toDouble()), "", ""))
        return EventEntity(
            "42",
            "",
            "",
            "",
            location,
            "",
            "",
            EventStatus.EVENT_STATUS_UNSPECIFIED,
            emptyList(),
            "",
            emptyList(),
            Metadata(),
            false
        )
    }

}
