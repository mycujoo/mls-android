package tv.mycujoo.domain.usecase

import tv.mycujoo.domain.entity.Events
import tv.mycujoo.domain.entity.Result
import tv.mycujoo.domain.params.EventListParams
import tv.mycujoo.domain.repository.EventsRepository

/**
 * Usecase for getting Events.
 * Input parameter defines which types of Events should be returned
 * @see EventListParams
 */
class GetEventsUseCase(private val repository: EventsRepository) :
    AbstractParameterizedUseCase<EventListParams, Result<Exception, Events>>() {
    override suspend fun build(eventListParams: EventListParams): Result<Exception, Events> {
        return repository.getEventsList(eventListParams)
    }
}