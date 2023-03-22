package tv.mycujoo.mcls.network

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import tv.mycujoo.data.entity.ActionResponse
import tv.mycujoo.data.model.EventSourceData
import tv.mycujoo.data.model.EventsSourceData
import tv.mycujoo.data.request.GetEventDetailsRequest
import tv.mycujoo.data.request.GetEventListRequest

interface MlsApi {

    @POST("mcls.cda.events.v1.EventsService/List")
    suspend fun getEvents(
        @Body eventListRequest: GetEventListRequest
    ): EventsSourceData

    @POST("mcls.cda.events.v1.EventsService/Get")
    suspend fun getEventDetails(
        @Body eventByIdRequest: GetEventDetailsRequest
    ): EventSourceData

    @POST("bff/timeline/v1beta1/{timeline_id}")
    suspend fun getActions(
        @Path("timeline_id") timelineId: String,
        @Query("update_id") updateId: String? = null
    ): ActionResponse
}
