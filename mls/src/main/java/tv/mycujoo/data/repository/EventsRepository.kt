package tv.mycujoo.data.repository

import tv.mycujoo.data.entity.ActionResponse
import tv.mycujoo.data.mapper.EventMapper.Companion.mapEventSourceDataToEventEntity
import tv.mycujoo.data.request.GetEventDetailsRequest
import tv.mycujoo.data.request.GetEventListRequest
import tv.mycujoo.domain.entity.EventEntity
import tv.mycujoo.domain.entity.Events
import tv.mycujoo.domain.entity.Result
import tv.mycujoo.domain.params.EventListParams
import tv.mycujoo.domain.params.TimelineIdPairParam
import tv.mycujoo.domain.repository.AbstractRepository
import tv.mycujoo.domain.repository.IEventsRepository
import tv.mycujoo.mcls.network.MlsApi
import javax.inject.Inject

class EventsRepository @Inject constructor(
    val api: MlsApi
) : AbstractRepository(), IEventsRepository {


    override suspend fun getEventsList(eventListParams: EventListParams): Result<Exception, Events> {
        return safeApiCall {
            val eventsSourceData = api.getEvents(
                GetEventListRequest(
                    pageSize = eventListParams.pageSize,
                    pageToken = eventListParams.pageToken,
                    filter = eventListParams.filter,
                    orderBy = eventListParams.orderBy.name,
                    search = eventListParams.search
                )
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
    ): Result<Exception, EventEntity> {
        return safeApiCall {
            val eventDetails = api.getEventDetails(
                GetEventDetailsRequest(eventId = eventId)
            )
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