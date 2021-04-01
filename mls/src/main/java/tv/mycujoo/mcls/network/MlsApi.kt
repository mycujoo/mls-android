package tv.mycujoo.mcls.network

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import tv.mycujoo.data.entity.ActionResponse
import tv.mycujoo.domain.entity.EventEntity
import tv.mycujoo.domain.entity.Events

interface MlsApi {

    @GET("bff/events/v1beta1")
    suspend fun getEvents(
        @Query("page_size") pageSize: Int? = null,
        @Query("page_token") pageToken: String? = null,
        @Query("status") status: List<String>? = null,
        @Query("order_by") orderBy: String? = null
    ): Events

    @GET("bff/events/v1beta1/{id}")
    suspend fun getEventDetails(
        @Path("id") id: String,
        @Query("update_id") updateId: String? = null
    ): EventEntity

    @GET("bff/timeline/v1beta1/{timeline_id}")
    suspend fun getActions(
        @Path("timeline_id") timelineId: String,
        @Query("update_id") updateId: String? = null
    ): ActionResponse
}