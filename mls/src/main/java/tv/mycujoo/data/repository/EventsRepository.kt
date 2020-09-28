package tv.mycujoo.data.repository

import tv.mycujoo.data.entity.ActionResponse
import tv.mycujoo.domain.entity.EventEntity
import tv.mycujoo.domain.entity.Events
import tv.mycujoo.domain.entity.Result
import tv.mycujoo.domain.params.EventListParams
import tv.mycujoo.domain.params.TimelineIdPairParam
import tv.mycujoo.domain.repository.AbstractRepository
import tv.mycujoo.domain.repository.EventsRepository
import tv.mycujoo.mls.network.MlsApi

class EventsRepository(val api: MlsApi) : AbstractRepository(),
    EventsRepository {
    override suspend fun getEventsList(eventListParams: EventListParams): Result<Exception, Events> {
        return safeApiCall {
            api.getEvents(
                eventListParams.pageSize,
                eventListParams.pageToken,
                eventListParams.status,
                eventListParams.orderBy
            )
        }
    }

    override suspend fun getEventDetails(eventId: String, updatedId: String?): Result<Exception, EventEntity> {
        return safeApiCall { api.getEventDetails(eventId, updatedId) }
    }

    override suspend fun getActions(timelineIdPairParam: TimelineIdPairParam): Result<Exception, ActionResponse> {
        return safeApiCall { api.getActions(timelineIdPairParam.timelineId, timelineIdPairParam.updateEventId) }
    }
}