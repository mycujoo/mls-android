package tv.mycujoo.mls.core

import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import okhttp3.OkHttpClient
import okhttp3.WebSocket
import org.junit.*
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.invocation.InvocationOnMock
import tv.mycujoo.domain.entity.*
import tv.mycujoo.domain.repository.EventsRepository
import tv.mycujoo.domain.usecase.GetActionsFromJSONUseCase
import tv.mycujoo.mls.CoroutineTestRule
import tv.mycujoo.mls.api.MLSBuilder
import tv.mycujoo.mls.api.MLSConfiguration
import tv.mycujoo.mls.data.IDataHolder
import tv.mycujoo.mls.entity.msc.VideoPlayerConfig
import tv.mycujoo.mls.manager.ViewIdentifierManager
import tv.mycujoo.mls.network.socket.MainWebSocketListener
import tv.mycujoo.mls.network.socket.ReactorListener
import tv.mycujoo.mls.network.socket.ReactorSocket
import tv.mycujoo.mls.widgets.PlayerViewWrapper
import tv.mycujoo.mls.widgets.mlstimebar.MLSTimeBar


@ExperimentalCoroutinesApi
class VideoPlayerCoordinatorTest {


    @get:Rule
    var coroutineTestRule = CoroutineTestRule()

    private lateinit var videoPlayerCoordinator: VideoPlayerCoordinator

    @Mock
    private lateinit var playerViewWrapper: PlayerViewWrapper


    @Mock
    lateinit var MLSBuilder: MLSBuilder


    @Mock
    lateinit var videoPlayerConfig: VideoPlayerConfig


    lateinit var reactorSocket: ReactorSocket
    private lateinit var mainWebSocketListener: MainWebSocketListener


    @Mock
    lateinit var reactorListener: ReactorListener

    @Mock
    lateinit var webSocket: WebSocket

    @Mock
    lateinit var viewIdentifierManager: ViewIdentifierManager

    @Mock
    lateinit var dispatcher: CoroutineScope

    @Mock
    lateinit var eventsRepository: EventsRepository

    @Mock
    lateinit var dataHolder: IDataHolder

    @Mock
    lateinit var activity: AppCompatActivity

    @Mock
    lateinit var exoPlayer: SimpleExoPlayer
    private lateinit var exoPlayerMainEventListener: MainEventListener


    @Mock
    lateinit var mediaFactory: HlsMediaSource.Factory

    @Mock
    lateinit var hlsMediaSource: HlsMediaSource

    @Mock
    lateinit var timeBar: MLSTimeBar

    @Mock
    lateinit var okHttpClient: OkHttpClient


    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)

        whenever(okHttpClient.newWebSocket(any(), any())).thenReturn(webSocket)
        mainWebSocketListener = MainWebSocketListener()
        reactorSocket = ReactorSocket(okHttpClient, mainWebSocketListener)
        reactorSocket.setUUID("SAMPLE_UUID")

        whenever(MLSBuilder.activity).thenReturn(activity)
        whenever(MLSBuilder.createExoPlayer(any())).thenReturn(exoPlayer)
        whenever(MLSBuilder.createMediaFactory(any())).thenReturn(mediaFactory)
        whenever(mediaFactory.createMediaSource(any())).thenReturn(hlsMediaSource)

        whenever(MLSBuilder.createReactorListener(any())).thenReturn(reactorListener)
        whenever(MLSBuilder.mlsConfiguration).thenReturn(MLSConfiguration())
        whenever(MLSBuilder.hasAnalytic).thenReturn(false)

        whenever(playerViewWrapper.context).thenReturn(activity)
        whenever(playerViewWrapper.getTimeBar()).thenReturn(timeBar)

        whenever(dispatcher.coroutineContext).thenReturn(coroutineTestRule.testDispatcher)


        whenever(exoPlayer.addListener(any())).then { storeExoPlayerListener(it) }
        videoPlayerCoordinator = VideoPlayerCoordinator(
            videoPlayerConfig,
            viewIdentifierManager,
            reactorSocket,
            dispatcher,
            eventsRepository,
            dataHolder,
            GetActionsFromJSONUseCase.mappedActionCollections().timelineMarkerActionList
        )
        videoPlayerCoordinator.initialize(playerViewWrapper, MLSBuilder)
    }

    private fun storeExoPlayerListener(it: InvocationOnMock) {
        exoPlayerMainEventListener = it.arguments[0] as MainEventListener
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `given call to play event with id, should fetch event details`() = runBlockingTest {
        videoPlayerCoordinator.playVideo("1eUBgUbXhriLFCT6A8E5a6Lv0R7")


        verify(eventsRepository).getEventDetails("1eUBgUbXhriLFCT6A8E5a6Lv0R7")
    }

    @Test
    fun `given call to play event with eventEntity, should fetch event details`() = runBlockingTest {
        val event: EventEntity = getSampleEventEntity(emptyList())
        videoPlayerCoordinator.playVideo(event)


        verify(eventsRepository).getEventDetails(event.id)
    }

    @Test
    fun `given event with streamUrl, should play video`() = runBlockingTest {
        val event: EventEntity = getSampleEventEntity(emptyList())
        val eventEntityDetails = getSampleEventEntity(getSampleStreamList())
        whenever(eventsRepository.getEventDetails(event.id)).thenReturn(Result.Success(eventEntityDetails))
        whenever(mediaFactory.createMediaSource(any())).thenReturn(hlsMediaSource)


        videoPlayerCoordinator.playVideo(eventEntityDetails)


        verify(exoPlayer).prepare(any())
    }

    @Test
    fun `given event without streamUrl, should not play video`() = runBlockingTest {
        val event: EventEntity = getSampleEventEntity(emptyList())
        whenever(eventsRepository.getEventDetails(event.id)).thenReturn(Result.Success(event))
        whenever(mediaFactory.createMediaSource(any())).thenReturn(hlsMediaSource)


        videoPlayerCoordinator.playVideo(event)


        verify(exoPlayer, never()).prepare(any())
    }


    @Test
    fun `given event without streamUrl, should display event info`() = runBlockingTest {
        val event: EventEntity = getSampleEventEntity(emptyList())
        whenever(eventsRepository.getEventDetails(event.id)).thenReturn(Result.Success(event))
        whenever(mediaFactory.createMediaSource(any())).thenReturn(hlsMediaSource)

        videoPlayerCoordinator.playVideo(event)

        verify(playerViewWrapper).displayEventInformationPreEventDialog()
    }

    @Test
    fun `given event to play, should connect to reactor`() = runBlockingTest {
        val event: EventEntity = getSampleEventEntity(emptyList())
        whenever(eventsRepository.getEventDetails(event.id)).thenReturn(Result.Success(event))


        videoPlayerCoordinator.playVideo(event)


        verify(webSocket).send("joinEvent;${event.id}")
    }

    @Ignore("Event Status is not done on server yet")
    @Test
    fun `given event with anything but Started status to play, should not connect to reactor`() = runBlockingTest {
        val event = getSampleEventEntity(emptyList(), EventStatus.EVENT_STATUS_SCHEDULED)
        whenever(eventsRepository.getEventDetails(event.id)).thenReturn(Result.Success(event))


        videoPlayerCoordinator.playVideo(event)
        videoPlayerCoordinator.playVideo(getSampleEventEntity(emptyList(), EventStatus.EVENT_STATUS_RESCHEDULED))
        videoPlayerCoordinator.playVideo(getSampleEventEntity(emptyList(), EventStatus.EVENT_STATUS_CANCELLED))
        videoPlayerCoordinator.playVideo(getSampleEventEntity(emptyList(), EventStatus.EVENT_STATUS_POSTPONED))
        videoPlayerCoordinator.playVideo(getSampleEventEntity(emptyList(), EventStatus.EVENT_STATUS_DELAYED))
        videoPlayerCoordinator.playVideo(getSampleEventEntity(emptyList(), EventStatus.EVENT_STATUS_PAUSED))
        videoPlayerCoordinator.playVideo(getSampleEventEntity(emptyList(), EventStatus.EVENT_STATUS_DELAYED))
        videoPlayerCoordinator.playVideo(getSampleEventEntity(emptyList(), EventStatus.EVENT_STATUS_SUSPENDED))
        videoPlayerCoordinator.playVideo(getSampleEventEntity(emptyList(), EventStatus.EVENT_STATUS_FINISHED))
        videoPlayerCoordinator.playVideo(getSampleEventEntity(emptyList(), EventStatus.EVENT_STATUS_UNSPECIFIED))


        verify(reactorSocket, never()).join(any())
    }

    @Test
    fun `update viewers counter in VOD stream, should hide viewers counter in player wrapper`() = runBlockingTest {
        val event = getSampleEventEntity(getSampleStreamList(), EventStatus.EVENT_STATUS_SCHEDULED)
        whenever(eventsRepository.getEventDetails(event.id)).thenReturn(Result.Success(event))

        whenever(exoPlayer.isCurrentWindowDynamic).thenReturn(false)


        videoPlayerCoordinator.playVideo(event)
        exoPlayerMainEventListener.onPlayerStateChanged(true, 0)
        mainWebSocketListener.onMessage(webSocket, "eventTotal;ck2343whlc43k0g90i92grc0u;17")


        verify(playerViewWrapper).hideViewersCounter()
    }

    @Test
    fun `update viewers counter in LIVE stream, should update player wrapper`() = runBlockingTest {
        val event = getSampleEventEntity(getSampleStreamList(), EventStatus.EVENT_STATUS_SCHEDULED)
        whenever(eventsRepository.getEventDetails(event.id)).thenReturn(Result.Success(event))

        whenever(exoPlayer.isCurrentWindowDynamic).thenReturn(true)


        videoPlayerCoordinator.playVideo(event)
        exoPlayerMainEventListener.onPlayerStateChanged(true, 0)
        mainWebSocketListener.onMessage(webSocket, "eventTotal;ck2343whlc43k0g90i92grc0u;17")


        verify(playerViewWrapper).updateViewersCounter("17")
    }

    /**region Fake data*/
    companion object {
        private fun getSampleStreamList(): List<Stream> {
            return listOf(Stream("stream_url"))
        }

        fun getSampleEventEntity(
            streams: List<Stream>,
            status: EventStatus = EventStatus.EVENT_STATUS_UNSPECIFIED
        ): EventEntity {
//        EventEntity(id=1eUBgUbXhriLFCT6A8E5a6Lv0R7, title=Test Title 0, description=Desc txt, thumbnail_url=,
//        location=Location(physical=Physical(city=Amsterdam, continent_code=EU, coordinates=Coordinates(latitude=52.3666969, longitude=4.8945398), country_code=NL, venue=)),
//        organiser=Org text, start_time=2020-07-11T07:32:46Z, status=EVENT_STATUS_SCHEDULED, streams=[Stream(fullUrl=https://rendered-europe-west.mls.mycujoo.tv/shervin/ckcfwmo4g000j0131mvc1zchu/master.m3u8)],
//        timezone=America/Los_Angeles, timeline_ids=[], metadata=tv.mycujoo.domain.entity.Metadata@ea3de11, is_test=false)

            val location = Location(Physical("", "", Coordinates(0.toDouble(), 0.toDouble()), "", ""))
            return EventEntity(
                "42",
                "",
                "",
                "",
                location,
                "",
                "",
                status,
                streams,
                "",
                emptyList(),
                Metadata(),
                false
            )
        }

        const val SAMPLE_UUID = "aa-bb-cc-dd-ee"

    }

    /**endregion */


}