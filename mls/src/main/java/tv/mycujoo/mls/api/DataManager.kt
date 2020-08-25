package tv.mycujoo.mls.api

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import tv.mycujoo.domain.entity.EventEntity
import tv.mycujoo.domain.entity.EventStatus
import tv.mycujoo.domain.entity.OrderByEventsParam
import tv.mycujoo.domain.entity.Result
import tv.mycujoo.domain.params.EventIdPairParam
import tv.mycujoo.domain.repository.EventsRepository
import tv.mycujoo.domain.usecase.GetEventDetailUseCase
import tv.mycujoo.mls.data.IDataManager
import tv.mycujoo.mls.model.SingleLiveEvent
import tv.mycujoo.mls.network.MlsApi

/**
 * Serves client as Data Provider
 * Serves internal use as Internal Data Provider
 */
class DataManager(
    private val scope: CoroutineScope,
    private val eventsRepository: EventsRepository,
    private var mlsApi: MlsApi
) : IDataManager {


    /**region Fields*/
    private val events = SingleLiveEvent<List<EventEntity>>()
    override var currentEvent: EventEntity? = null
    private var fetchEventCallback: ((eventList: List<EventEntity>, previousPageToken: String, nextPageToken: String) -> Unit)? =
        null

    /**endregion */


    /**region InternalDataProvider*/
    override suspend fun getEventDetails(eventId: String): Result<Exception, EventEntity> {
        return GetEventDetailUseCase(eventsRepository).execute(EventIdPairParam(eventId))
    }
    /**endregion */

    /**region Data Provider*/
    override fun getEventsLiveData(): SingleLiveEvent<List<EventEntity>> {
        return events
    }

    override fun fetchEvents(
        pageSize: Int?,
        pageToken: String?,
        eventStatus: List<EventStatus>?,
        orderBy: OrderByEventsParam?,
        fetchEventCallback: ((eventList: List<EventEntity>, previousPageToken: String, nextPageToken: String) -> Unit)?
    ) {
        this.fetchEventCallback = fetchEventCallback
        scope.launch {
            val eventsResponse =
                mlsApi.getEvents(pageSize, pageToken, eventStatus?.map { it.name }, orderBy?.name)
            eventsResponse?.let { response ->
                events.postValue(
                    response.events
                )
                fetchEventCallback?.let {
                    it.invoke(response.events, response.previousPageToken ?: "", response.nextPageToken ?: "")
                }
            }

        }
    }
    /**endregion */
}