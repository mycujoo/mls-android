package tv.mycujoo.mcls.api

import android.util.Log
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
import tv.mycujoo.domain.usecase.GetActionsUseCase
import tv.mycujoo.domain.usecase.GetEventDetailUseCase
import tv.mycujoo.domain.usecase.GetEventsUseCase
import tv.mycujoo.mcls.data.IDataManager
import tv.mycujoo.mcls.enum.C
import tv.mycujoo.mcls.enum.LogLevel
import tv.mycujoo.mcls.enum.MessageLevel
import tv.mycujoo.mcls.helper.EventFilterFactory
import tv.mycujoo.mcls.manager.Logger
import tv.mycujoo.mcls.model.SingleLiveEvent
import javax.inject.Inject

/**
 * Serves client as 'Data Provider'
 * Serves internal usage as 'Internal Data Provider'
 * @param scope CoroutineScope which calls will made on it's context
 * @param logger log info, error & warning based on required level of logging
 */
class DataManager @Inject constructor(
    private val scope: CoroutineScope,
    private val logger: Logger,
    private val getEventDetailUseCase: GetEventDetailUseCase,
    private val getActionsUseCase: GetActionsUseCase,
    private val getEventsUseCase: GetEventsUseCase
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
        return getEventDetailUseCase.execute(EventIdPairParam(eventId))
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
        return getActionsUseCase.execute(
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

            val filterBuilder = EventFilterFactory()
                .withEventStatus(eventStatus.orEmpty())
                .build()

            val result = getEventsUseCase.execute(
                EventListParams(
                    pageSize = pageSize ?: DEFAULT_EVENTS_PER_PAGE,
                    pageToken = pageToken,
                    filter = filterBuilder,
                    orderBy = orderBy ?: OrderByEventsParam.ORDER_UNSPECIFIED,
                    search = null
                )
            )
            when (result) {
                is Result.Success -> {
                    events.postValue(
                        result.value.eventEntities
                    )
                    fetchEventCallback?.invoke(
                        result.value.eventEntities,
                        result.value.previousPageToken ?: "",
                        result.value.nextPageToken ?: ""
                    )
                }
                is Result.NetworkError -> {
                    logger.log(MessageLevel.DEBUG, C.NETWORK_ERROR_MESSAGE.plus(" ${result.error}"))
                }
                is Result.GenericError -> {
                    Log.d(TAG, "fetchEvents: Error ${result.errorCode}")
                    logger.log(
                        MessageLevel.DEBUG,
                        C.INTERNAL_ERROR_MESSAGE.plus(" ${result.errorMessage} ${result.errorCode}")
                    )
                }

            }
        }
    }

    override fun getActions(
        timelineId: String,
        onSuccess: (ActionResponse) -> Unit,
        onError: ((String) -> Unit)?
    ) {
        scope.launch {
            when (val response = getActionsUseCase.execute(TimelineIdPairParam(timelineId))) {
                is Result.Success -> {
                    onSuccess(response.value)
                }
                is Result.GenericError -> {
                    onError?.invoke(response.errorMessage)
                }
                is Result.NetworkError -> {
                    onError?.invoke(response.error.message.orEmpty())
                }
            }
        }
    }

    /**endregion */

    companion object {
        private const val TAG = "DataManager"

        private const val DEFAULT_EVENTS_PER_PAGE = 20
    }
}