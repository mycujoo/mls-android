package tv.mycujoo.data.repository

import tv.mycujoo.domain.entity.EventEntity
import tv.mycujoo.domain.entity.Events
import tv.mycujoo.domain.entity.Result
import tv.mycujoo.domain.repository.AbstractRepository
import tv.mycujoo.domain.repository.EventsRepository
import tv.mycujoo.mls.network.MlsApi

class EventsRepository(val api: MlsApi) : AbstractRepository(),
    EventsRepository {
    override suspend fun getEventsList(): Result<Exception, Events> {
        return safeApiCall { api.getEvents() }


    }

    override suspend fun getEventDetails(eventId: String, updatedId: String?): Result<Exception, EventEntity> {
        return safeApiCall { api.getEventDetails(eventId, updatedId) }
    }
}