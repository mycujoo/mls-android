package tv.mycujoo.data.repository

import tv.mycujoo.data.entity.ActionResponse
import tv.mycujoo.data.mapper.EventMapper.Companion.mapEventSourceDataToEventEntity
import tv.mycujoo.domain.entity.EventEntity
import tv.mycujoo.domain.entity.Events
import tv.mycujoo.domain.entity.Result
import tv.mycujoo.domain.params.EventListParams
import tv.mycujoo.domain.params.TimelineIdPairParam
import tv.mycujoo.domain.repository.AbstractRepository
import tv.mycujoo.domain.repository.EventsRepository
import tv.mycujoo.mcls.network.MlsApi

class EventsRepository(val api: MlsApi) : AbstractRepository(),
    EventsRepository {
    override suspend fun getEventsList(eventListParams: EventListParams): Result<Exception, Events> {
        return safeApiCall {
            val eventsSourceData = api.getEvents(
                pageSize = eventListParams.pageSize,
                pageToken = eventListParams.pageToken,
                status = eventListParams.status,
                orderBy = eventListParams.orderBy
            )
            val events = eventsSourceData.events.map { mapEventSourceDataToEventEntity(it) }
            Events(
                eventEntities = events,
                previousPageToken = eventsSourceData.previousPageToken,
                nextPageToken = eventsSourceData.nextPageToken
            )
        }
    }

    override suspend fun getEventDetails(
        eventId: String,
        updatedId: String?
    ): Result<Exception, EventEntity> {
        return safeApiCall {
            val eventDetails = api.getEventDetails(eventId, updatedId)
            mapEventSourceDataToEventEntity(eventDetails)
        }
    }

    override suspend fun getActions(timelineIdPairParam: TimelineIdPairParam): Result<Exception, ActionResponse> {
        return safeApiCall {
            api.getActions(
                timelineIdPairParam.timelineId,
                timelineIdPairParam.updateEventId
            )
        }
    }
}