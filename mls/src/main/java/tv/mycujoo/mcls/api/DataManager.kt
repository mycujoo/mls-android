package tv.mycujoo.mcls.api

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
import tv.mycujoo.mcls.data.IDataManager
import tv.mycujoo.mcls.enum.C
import tv.mycujoo.mcls.enum.LogLevel
import tv.mycujoo.mcls.enum.MessageLevel
import tv.mycujoo.mcls.manager.Logger
import tv.mycujoo.mcls.model.SingleLiveEvent

/**
 * Serves client as 'Data Provider'
 * Serves internal usage as 'Internal Data Provider'
 * @param scope CoroutineScope which calls will made on it's context
 * @param eventsRepository actual implementation of EventsRepository, used to call Use-Cases
 * @param logger log info, error & warning based on required level of logging
 */
class DataManager(
    private val scope: CoroutineScope,
    private val eventsRepository: EventsRepository,
    private val logger: Logger
) : IDataManager {


    /**region Fields*/
    /**
     * observable holder for Events.
     */
    private val events = SingleLiveEvent<List<EventEntity>>()

    /**
     * holds current active EventEntity.
     * for easier access
     */
    override var currentEvent: EventEntity? = null

    /**
     * callback for paginating through received Events
     */
    private var fetchEventCallback: ((eventList: List<EventEntity>, previousPageToken: String, nextPageToken: String) -> Unit)? =
        null

    /**endregion */


    /**region InternalDataProvider*/

    /**
     * fetch Event with details
     */
    override suspend fun getEventDetails(
        eventId: String,
        updateId: String?
    ): Result<Exception, EventEntity> {
        return GetEventDetailUseCase(eventsRepository).execute(EventIdPairParam(eventId, updateId))
    }

    /**
     * set @param log level of Logger
     * @see LogLevel
     */
    override fun setLogLevel(logLevel: LogLevel) {
        logger.setLogLevel(logLevel)
    }

    /**
     * get Annotation Actions
     * @param timelineId timeLineId of Event
     * @param updateId nullable update of Event
     * @return list of Actions or Exception
     */
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

    /**
     * fetch Events with given specification
     * @param pageSize nullable size of page
     * @param pageToken nullable token of page
     * @param eventStatus nullable statuses of returned Events
     * @param orderBy nullable order of returned Events
     * @param fetchEventCallback nullable callback which will may be used to navigate through paginated data
     */
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
                        result.value.eventEntities
                    )
                    fetchEventCallback?.let {
                        it.invoke(
                            result.value.eventEntities,
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