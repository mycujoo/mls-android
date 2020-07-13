package tv.mycujoo.mls.network

import retrofit2.http.GET
import retrofit2.http.Query
import tv.mycujoo.domain.entity.Events
import tv.mycujoo.mls.model.Event

interface MlsApi {
    @GET("api/events")
    suspend fun getEventList(): List<Event>

    @GET("bff/events/v1beta1")
    suspend fun getEvents(
        @Query("page_size") pageSize: Int?,
        @Query("page_token") pageToken: String?,
        @Query("status") status: List<String>?,
        @Query("order_by") orderBy: String?
    ): Events
}