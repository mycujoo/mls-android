package tv.mycujoo.domain.usecase

import tv.mycujoo.domain.repository.EventsRepository
import tv.mycujoo.mls.model.Event

class GetEventsUseCase(private val repository: EventsRepository) :
    AbstractUseCase<tv.mycujoo.domain.entity.Result<Exception, List<Event>>>() {
    override suspend fun build(): tv.mycujoo.domain.entity.Result<Exception, List<Event>> {
        return repository.getEventsList()
    }
}