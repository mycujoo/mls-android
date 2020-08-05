package tv.mycujoo.domain.usecase

import tv.mycujoo.domain.entity.EventEntity
import tv.mycujoo.domain.entity.Result
import tv.mycujoo.domain.repository.EventsRepository

class GetEventDetailUseCase(private val repository: EventsRepository) :
    AbstractParameterizedUseCase<String, Result<Exception, EventEntity>>() {
    override suspend fun build(eventId: String): Result<Exception, EventEntity> {
        return repository.getEventDetails(eventId)
    }
}