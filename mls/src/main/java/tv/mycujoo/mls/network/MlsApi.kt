package tv.mycujoo.mls.network

import retrofit2.http.GET
import tv.mycujoo.mls.model.Event

interface MlsApi {
    @GET("api/events")
    suspend fun getEventList() : List<Event>
}