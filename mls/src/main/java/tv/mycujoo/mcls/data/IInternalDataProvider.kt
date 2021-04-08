package tv.mycujoo.mcls.data

import tv.mycujoo.data.entity.ActionResponse
import tv.mycujoo.domain.entity.EventEntity
import tv.mycujoo.domain.entity.Result
import tv.mycujoo.mcls.enum.LogLevel

interface IInternalDataProvider {
    var currentEvent: EventEntity?

    suspend fun getEventDetails(
        eventId: String,
        updateId: String? = null
    ): Result<Exception, EventEntity>

    suspend fun getActions(
        timelineId: String,
        updateId: String? = null
    ): Result<Exception, ActionResponse>

    fun setLogLevel(logLevel: LogLevel)
}