package tv.mycujoo.data.repository

import tv.mycujoo.domain.entity.Result
import tv.mycujoo.domain.repository.AbstractRepository
import tv.mycujoo.domain.repository.EventsRepository
import tv.mycujoo.mls.model.Event
import tv.mycujoo.mls.network.MlsApi

class EventsRepository(val api: MlsApi) : AbstractRepository(),
    EventsRepository {
    override suspend fun getEventsList(): Result<Exception, List<Event>> {
        return safeApiCall { api.getEventList() }
    }
}