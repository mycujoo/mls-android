package tv.mycujoo.mls.api

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import tv.mycujoo.domain.entity.EventEntity
import tv.mycujoo.domain.entity.EventStatus
import tv.mycujoo.domain.entity.OrderByEventsParam
import tv.mycujoo.mls.model.SingleLiveEvent
import tv.mycujoo.mls.network.MlsApi
import javax.inject.Inject

class DataProviderImpl @Inject constructor(val scope: CoroutineScope) : DataProvider {

    @Inject
    lateinit var mlsApi: MlsApi

    /**region Events*/
    val events = SingleLiveEvent<List<EventEntity>>()

    /**endregion */

    override fun getEventsLiveData(): SingleLiveEvent<List<EventEntity>> {
        return events
    }


    override fun fetchEvents(
        pageSize: Int?,
        pageToken: String?,
        eventStatus: List<EventStatus>?,
        orderBy: OrderByEventsParam?
    ) {
        scope.launch {
            val eventsResponse =
                mlsApi.getEvents(pageSize, pageToken, eventStatus?.map { it.name }, orderBy?.name)
            events.postValue(
                eventsResponse.events
            )
        }
    }
}