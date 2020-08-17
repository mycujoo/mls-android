package tv.mycujoo.mls.api

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import tv.mycujoo.domain.entity.EventEntity
import tv.mycujoo.domain.entity.EventStatus
import tv.mycujoo.domain.entity.OrderByEventsParam
import tv.mycujoo.mls.data.IDataHolder
import tv.mycujoo.mls.model.SingleLiveEvent
import tv.mycujoo.mls.network.MlsApi
import javax.inject.Inject

class DataProviderImpl @Inject constructor(val scope: CoroutineScope) : DataProvider, IDataHolder {


    /**region Fields*/
    @Inject
    lateinit var mlsApi: MlsApi

    private val events = SingleLiveEvent<List<EventEntity>>()
    override var currentEvent: EventEntity? = null
    private var fetchEventCallback: ((List<EventEntity>) -> Unit)? = null
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
        fetchEventCallback: ((List<EventEntity>) -> Unit)?
    ) {
        this.fetchEventCallback = fetchEventCallback
        scope.launch {
            val eventsResponse =
                mlsApi.getEvents(pageSize, pageToken, eventStatus?.map { it.name }, orderBy?.name)
            events.postValue(
                eventsResponse.events
            )
            fetchEventCallback?.let {
                it.invoke(eventsResponse.events)
            }
        }
    }
    /**endregion */
}