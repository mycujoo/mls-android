package tv.mycujoo.mcls.network

import retrofit2.http.Body
import retrofit2.http.POST
import tv.mycujoo.data.model.EventResponse
import tv.mycujoo.data.model.EventsSourceData
import tv.mycujoo.data.request.GetEventDetailsRequest
import tv.mycujoo.data.request.GetEventListRequest

interface EventsApi {

    @POST("mcls.cda.events.v1.EventsService/List")
    suspend fun getEvents(
        @Body eventListRequest: GetEventListRequest
    ): EventsSourceData

    @POST("mcls.cda.events.v1.EventsService/Get")
    suspend fun getEventDetails(
        @Body eventByIdRequest: GetEventDetailsRequest
    ): EventResponse
}