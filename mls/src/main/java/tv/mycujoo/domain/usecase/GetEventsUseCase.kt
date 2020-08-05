package tv.mycujoo.domain.usecase

import tv.mycujoo.domain.entity.Events
import tv.mycujoo.domain.entity.Result
import tv.mycujoo.domain.repository.EventsRepository

class GetEventsUseCase(private val repository: EventsRepository) :
    AbstractUseCase<Result<Exception, Events>>() {
    override suspend fun build(): Result<Exception, Events> {
        return repository.getEventsList()
    }
}