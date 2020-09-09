package tv.mycujoo.domain.usecase

import tv.mycujoo.data.entity.ActionResponse
import tv.mycujoo.domain.entity.Result
import tv.mycujoo.domain.repository.EventsRepository

class GetActionsUseCase(private val repository: EventsRepository) :
    AbstractParameterizedUseCase<String, Result<Exception, ActionResponse>>() {
    override suspend fun build(timelineId: String): Result<Exception, ActionResponse> {
        return repository.getActions(timelineId)
    }
}