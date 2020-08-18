package tv.mycujoo.mls.data

import tv.mycujoo.domain.entity.EventEntity
import tv.mycujoo.domain.entity.Result

interface IInternalDataProvider {
    var currentEvent: EventEntity?

    suspend fun getEventDetails(eventId: String): Result<Exception, EventEntity>
}