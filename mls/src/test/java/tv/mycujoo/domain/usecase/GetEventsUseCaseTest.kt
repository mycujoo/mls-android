package tv.mycujoo.domain.usecase

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.resetMain
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import tv.mycujoo.data.repository.EventsRepository
import tv.mycujoo.domain.entity.Result
import tv.mycujoo.mls.CoroutineTestRule
import tv.mycujoo.mls.model.Event
import tv.mycujoo.mls.network.MlsApi
import java.net.HttpURLConnection
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail


@ExperimentalCoroutinesApi
class GetEventsUseCaseTest {

    @get:Rule
    var coroutineTestRule = CoroutineTestRule()


    private lateinit var api: MlsApi
    private lateinit var server: MockWebServer

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()

        val okHttpClient = OkHttpClient.Builder()
            .build()


        api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
            .create(MlsApi::class.java)

    }

    @After
    fun tearDown() {
        Dispatchers.resetMain() // reset main dispatcher to the original Main dispatcher
        server.shutdown();
    }

    @Test
    fun `given valid list of events, should return them`() = runBlocking<Unit> {


        val event_0 = Event("1000", "stream url", "name str", "location str", "started")
        val event_1 = Event("1001", "stream url", "name str", "location str", "started")
        val toJson = Gson().toJson(listOf(event_0, event_1))

        val response = MockResponse()
            .setBody(toJson)

        setResponseCodeAndStatus(response, HttpURLConnection.HTTP_OK)

        server.enqueue(response)


        when (val result = GetEventsUseCase(EventsRepository(api)).execute()) {

            is Result.Success -> {
                assertTrue { result.value.isEmpty() }
            }
            is Result.NetworkError -> {
                fail()
            }
            is Result.GenericError -> {
                fail()
            }
        }
    }

    @Test
    fun `given empty list of events, should return empty list`() = runBlocking<Unit> {
        val arrayList = ArrayList<Event>(0)
        val toJson = Gson().toJson(arrayList)
        val response = MockResponse()
            .setBody(toJson)

        setResponseCodeAndStatus(response, HttpURLConnection.HTTP_OK)

        server.enqueue(response)


        when (val result = GetEventsUseCase(EventsRepository(api)).execute()) {

            is Result.Success -> {
                assertTrue { result.value.isEmpty() }
            }
            is Result.NetworkError -> {
                fail()
            }
            is Result.GenericError -> {
                fail()
            }
        }
    }

    @Test
    fun `given generic error, should return error`() = runBlocking<Unit> {
        val arrayList = ArrayList<Event>(0)
        val toJson = Gson().toJson(arrayList)
        val response = MockResponse()
            .setBody(toJson)

        val errorCode = HttpURLConnection.HTTP_UNAUTHORIZED
        setResponseCodeAndStatus(response, errorCode)

        server.enqueue(response)


        when (val result = GetEventsUseCase(EventsRepository(api)).execute()) {

            is Result.Success -> {
                fail()
            }
            is Result.NetworkError -> {
                fail()
            }
            is Result.GenericError -> {
                assertEquals(errorCode, result.errorCode)
            }
        }
    }

    private fun setResponseCodeAndStatus(response: MockResponse, code: Int): MockResponse {

        response.setResponseCode(code)

        var reason = "Mock Response"
        when (code) {
            in 100..199 -> {
                reason = "Informational"
            }
            in 200..299 -> {
                reason = "OK"
            }
            in 300..399 -> {
                reason = "Redirection"
            }
            in 400..499 -> {
                reason = "Client Error"
            }
            in 500..599 -> {
                reason = "Server Error"
            }
        }
        return response.setStatus("HTTP/1.1 $code $reason")
    }
}