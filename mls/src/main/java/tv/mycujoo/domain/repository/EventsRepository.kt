package tv.mycujoo.domain.repository

import tv.mycujoo.domain.entity.EventEntity
import tv.mycujoo.domain.entity.Events
import tv.mycujoo.domain.entity.Result

interface EventsRepository {
    suspend fun getEventsList(): Result<Exception, Events>
    suspend fun getEventDetails(eventId: String, updatedId: String? = null): Result<Exception, EventEntity>
}