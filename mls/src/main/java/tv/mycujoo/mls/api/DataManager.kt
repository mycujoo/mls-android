package tv.mycujoo.mls.api

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import tv.mycujoo.data.entity.ActionResponse
import tv.mycujoo.domain.entity.EventEntity
import tv.mycujoo.domain.entity.EventStatus
import tv.mycujoo.domain.entity.OrderByEventsParam
import tv.mycujoo.domain.entity.Result
import tv.mycujoo.domain.params.EventIdPairParam
import tv.mycujoo.domain.params.EventListParams
import tv.mycujoo.domain.params.TimelineIdPairParam
import tv.mycujoo.domain.repository.EventsRepository
import tv.mycujoo.domain.usecase.GetActionsUseCase
import tv.mycujoo.domain.usecase.GetEventDetailUseCase
import tv.mycujoo.domain.usecase.GetEventsUseCase
import tv.mycujoo.mls.data.IDataManager
import tv.mycujoo.mls.enum.C
import tv.mycujoo.mls.enum.LogLevel
import tv.mycujoo.mls.enum.MessageLevel
import tv.mycujoo.mls.manager.Logger
import tv.mycujoo.mls.model.SingleLiveEvent

/**
 * Serves client as Data Provider
 * Serves internal use as Internal Data Provider
 */
class DataManager(
    private val scope: CoroutineScope,
    private val eventsRepository: EventsRepository,
    private val logger: Logger
) : IDataManager {


    /**region Fields*/
    private val events = SingleLiveEvent<List<EventEntity>>()
    override var currentEvent: EventEntity? = null
    private var fetchEventCallback: ((eventList: List<EventEntity>, previousPageToken: String, nextPageToken: String) -> Unit)? =
        null

    /**endregion */


    /**region InternalDataProvider*/

    override suspend fun getEventDetails(
        eventId: String,
        updateId: String?
    ): Result<Exception, EventEntity> {
        return GetEventDetailUseCase(eventsRepository).execute(EventIdPairParam(eventId, updateId))
    }

    override fun setLogLevel(logLevel: LogLevel) {
        logger.setLogLevel(logLevel)
    }

    override suspend fun getActions(
        timelineId: String,
        updateId: String?
    ): Result<Exception, ActionResponse> {
        return GetActionsUseCase(eventsRepository).execute(
            TimelineIdPairParam(
                timelineId,
                updateId
            )
        )
    }

    /**endregion */

    /**region Data Provider*/
    override fun getEventsLiveData(): SingleLiveEvent<List<EventEntity>> {
        return events
    }

    override fun fetchEvents(
        pageSize: Int?,
        pageToken: String?,
        eventStatus: List<EventStatus>?,
        orderBy: OrderByEventsParam?,
        fetchEventCallback: ((eventList: List<EventEntity>, previousPageToken: String, nextPageToken: String) -> Unit)?
    ) {
        this.fetchEventCallback = fetchEventCallback
        scope.launch {

            val result = GetEventsUseCase(eventsRepository).execute(
                EventListParams(
                    pageSize,
                    pageToken,
                    eventStatus?.map { it.toString() },
                    orderBy?.toString()
                )
            )
            when (result) {
                is Result.Success -> {
                    events.postValue(
                        result.value.events
                    )
                    fetchEventCallback?.let {
                        it.invoke(
                            result.value.events,
                            result.value.previousPageToken ?: "",
                            result.value.nextPageToken ?: ""
                        )
                    }
                }
                is Result.NetworkError -> {
                    logger.log(MessageLevel.DEBUG, C.NETWORK_ERROR_MESSAGE.plus(" ${result.error}"))
                }
                is Result.GenericError -> {
                    logger.log(
                        MessageLevel.DEBUG,
                        C.INTERNAL_ERROR_MESSAGE.plus(" ${result.errorMessage} ${result.errorCode}")
                    )
                }

            }
        }
    }
    /**endregion */
}