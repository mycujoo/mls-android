package tv.mycujoo.domain.usecase

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
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
import retrofit2.converter.moshi.MoshiConverterFactory
import tv.mycujoo.data.jsonadapter.JodaJsonAdapter
import tv.mycujoo.data.repository.EventsRepository
import tv.mycujoo.domain.entity.EventEntity
import tv.mycujoo.domain.entity.Result
import tv.mycujoo.domain.params.EventListParams
import tv.mycujoo.mcls.CoroutineTestRule
import tv.mycujoo.mcls.network.EventsApi
import java.net.HttpURLConnection
import kotlin.test.assertEquals
import kotlin.test.fail


@ExperimentalCoroutinesApi
class GetEventsUseCaseTest {

    @get:Rule
    var coroutineTestRule = CoroutineTestRule()


    private lateinit var api: EventsApi
    private lateinit var server: MockWebServer

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()

        val okHttpClient = OkHttpClient.Builder()
            .build()


        api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(MoshiConverterFactory.create())
            .client(okHttpClient)
            .build()
            .create(EventsApi::class.java)

    }

    @After
    fun tearDown() {
        Dispatchers.resetMain() // reset main dispatcher to the original Main dispatcher
        server.shutdown()
    }

    @Test
    fun `given generic error, should return error`() = runBlocking<Unit> {
        val arrayList = ArrayList<EventEntity>(0)

        val moshi: Moshi = Moshi.Builder()
            .add(JodaJsonAdapter())
            .build()
        val listMyData = Types.newParameterizedType(List::class.java, EventEntity::class.java)
        val jsonAdapter = moshi.adapter<List<EventEntity>>(listMyData)

        val response = MockResponse()
            .setBody(jsonAdapter.toJson(arrayList))

        val errorCode = HttpURLConnection.HTTP_UNAUTHORIZED
        setResponseCodeAndStatus(response, errorCode)

        server.enqueue(response)


        when (val result = GetEventsUseCase(EventsRepository(api)).execute(EventListParams())) {

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