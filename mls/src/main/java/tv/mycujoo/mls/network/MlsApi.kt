package tv.mycujoo.mls.network

import tv.mycujoo.mls.model.Event

interface MlsApi {
    suspend fun getEventList() : List<Event>
}