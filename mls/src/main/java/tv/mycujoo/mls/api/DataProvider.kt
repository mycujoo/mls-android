package tv.mycujoo.mls.api

import tv.mycujoo.domain.entity.EventEntity
import tv.mycujoo.domain.entity.EventStatus
import tv.mycujoo.domain.entity.OrderByEventsParam
import tv.mycujoo.mls.model.SingleLiveEvent

interface DataProvider {
    /**
     * subscribe to an observable which will emit list of EventEntities
     */
    fun getEventsLiveData(): SingleLiveEvent<List<EventEntity>>

    /**
     * calls Event-list endpoint and emits result on eventLiveData
     */
    fun fetchEvents(
        pageSize: Int? = null,
        pageToken: String? = null,
        eventStatus: List<EventStatus>? = null,
        orderBy: OrderByEventsParam? = null
    )
}