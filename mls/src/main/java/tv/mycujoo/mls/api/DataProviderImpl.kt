package tv.mycujoo.mls.api

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import tv.mycujoo.domain.entity.EventEntity
import tv.mycujoo.mls.model.SingleLiveEvent
import tv.mycujoo.mls.network.MlsApi
import javax.inject.Inject

class DataProviderImpl @Inject constructor(val scope: CoroutineScope) : DataProvider {

    @Inject
    lateinit var mlsApi: MlsApi

    /**region Events*/
    val events = SingleLiveEvent<List<EventEntity>>()

    /**endregion */

    override fun getEventsLiveData(): SingleLiveEvent<List<EventEntity>> {
        return events
    }


    override fun fetchEvents() {
        scope.launch {
            events.postValue(mlsApi.getEvents().events)
        }
    }
}