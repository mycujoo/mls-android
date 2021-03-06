package tv.mycujoo.domain.usecase

import tv.mycujoo.domain.entity.EventEntity
import tv.mycujoo.domain.entity.Result
import tv.mycujoo.domain.params.EventIdPairParam
import tv.mycujoo.domain.repository.EventsRepository

/**
 * Usecase for getting Event details.
 * Input parameter is pair of eventId and eventUpdateId
 * @see EventIdPairParam
 */
class GetEventDetailUseCase(private val repository: EventsRepository) :
    AbstractParameterizedUseCase<EventIdPairParam, Result<Exception, EventEntity>>() {
    override suspend fun build(param: EventIdPairParam): Result<Exception, EventEntity> {
        return repository.getEventDetails(param.eventId, param.updateEventId)
    }
}