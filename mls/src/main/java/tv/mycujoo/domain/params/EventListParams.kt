package tv.mycujoo.domain.params

import tv.mycujoo.domain.entity.OrderByEventsParam

data class EventListParams(
    val pageSize: Int = 10,
    val pageToken: String? = null,
    val filter: String? = null,
    val search: String? = null,
    val orderBy: OrderByEventsParam = OrderByEventsParam.ORDER_UNSPECIFIED
)