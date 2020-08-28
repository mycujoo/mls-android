package tv.mycujoo.mls.api

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import tv.mycujoo.domain.entity.EventEntity
import tv.mycujoo.domain.entity.EventStatus
import tv.mycujoo.domain.entity.OrderByEventsParam
import tv.mycujoo.domain.entity.Result
import tv.mycujoo.domain.params.EventIdPairParam
import tv.mycujoo.domain.repository.EventsRepository
import tv.mycujoo.domain.usecase.GetEventDetailUseCase
import tv.mycujoo.domain.usecase.GetEventsUseCase
import tv.mycujoo.mls.data.IDataManager
import tv.mycujoo.mls.model.SingleLiveEvent
import tv.mycujoo.mls.network.MlsApi

/**
 * Serves client as Data Provider
 * Serves internal use as Internal Data Provider
 */
class DataManager(
    private val scope: CoroutineScope,
    private val eventsRepository: EventsRepository,
    private var mlsApi: MlsApi
) : IDataManager {


    /**region Fields*/
    private val events = SingleLiveEvent<List<EventEntity>>()
    override var currentEvent: EventEntity? = null
    private var fetchEventCallback: ((eventList: List<EventEntity>, previousPageToken: String, nextPageToken: String) -> Unit)? =
        null

    /**endregion */


    /**region InternalDataProvider*/
    override suspend fun getEventDetails(eventId: String): Result<Exception, EventEntity> {
        return GetEventDetailUseCase(eventsRepository).execute(EventIdPairParam(eventId))
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

            val result = GetEventsUseCase(eventsRepository).execute()
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
                    Log.w("DataManager", result.error.toString())
                }
                is Result.GenericError -> {
                    Log.w("DataManager", "Error: ${result.errorCode}:${result.errorMessage}")
                }

            }
        }
    }
    /**endregion */
}