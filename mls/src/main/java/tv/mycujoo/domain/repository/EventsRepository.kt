package tv.mycujoo.domain.repository

import tv.mycujoo.domain.entity.Result
import tv.mycujoo.mls.model.Event

interface EventsRepository {
    suspend fun getEventsList(): Result<Exception, List<Event>>
}