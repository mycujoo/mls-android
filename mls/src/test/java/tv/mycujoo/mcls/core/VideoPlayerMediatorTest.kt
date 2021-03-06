package tv.mycujoo.mcls.core

import android.content.res.Resources
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player.*
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.nhaarman.mockitokotlin2.*
import com.npaw.youbora.lib6.exoplayer2.Exoplayer2Adapter
import com.npaw.youbora.lib6.plugin.Plugin
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
import tv.mycujoo.data.entity.ActionResponse
import tv.mycujoo.data.entity.ServerConstants.Companion.ERROR_CODE_GEOBLOCKED
import tv.mycujoo.data.entity.ServerConstants.Companion.ERROR_CODE_NO_ENTITLEMENT
import tv.mycujoo.data.entity.ServerConstants.Companion.ERROR_CODE_UNSPECIFIED
import tv.mycujoo.domain.entity.*
import tv.mycujoo.mcls.CoroutineTestRule
import tv.mycujoo.mcls.R
import tv.mycujoo.mcls.analytic.YouboraClient
import tv.mycujoo.mcls.api.MLSBuilder
import tv.mycujoo.mcls.api.MLSConfiguration
import tv.mycujoo.mcls.api.defaultVideoPlayerConfig
import tv.mycujoo.mcls.cast.ICast
import tv.mycujoo.mcls.cast.ICasterSession
import tv.mycujoo.mcls.cast.ISessionManagerListener
import tv.mycujoo.mcls.data.IDataManager
import tv.mycujoo.mcls.entity.msc.VideoPlayerConfig
import tv.mycujoo.mcls.manager.Logger
import tv.mycujoo.mcls.manager.ViewHandler
import tv.mycujoo.mcls.matcher.SeekParameterArgumentMatcher
import tv.mycujoo.mcls.mediator.AnnotationMediator
import tv.mycujoo.mcls.network.socket.MainWebSocketListener
import tv.mycujoo.mcls.network.socket.ReactorListener
import tv.mycujoo.mcls.network.socket.ReactorSocket
import tv.mycujoo.mcls.player.IPlayer
import tv.mycujoo.mcls.player.MediaFactory
import tv.mycujoo.mcls.player.MediaDatum
import tv.mycujoo.mcls.widgets.MLSPlayerView
import tv.mycujoo.mcls.widgets.PlayerControllerMode
import tv.mycujoo.mcls.widgets.RemotePlayerControllerView
import tv.mycujoo.mcls.widgets.mlstimebar.MLSTimeBar


@ExperimentalCoroutinesApi
class VideoPlayerMediatorTest {


    @get:Rule
    var coroutineTestRule = CoroutineTestRule()

    private lateinit var videoPlayerMediator: VideoPlayerMediator

    @Mock
    private lateinit var playerView: MLSPlayerView

    @Mock
    private lateinit var remotePlayerControllerView: RemotePlayerControllerView

    @Mock
    lateinit var MLSBuilder: MLSBuilder

    @Mock
    lateinit var internalBuilder: InternalBuilder

    @Mock
    lateinit var videoPlayerConfig: VideoPlayerConfig

    lateinit var reactorSocket: ReactorSocket
    private lateinit var mainWebSocketListener: MainWebSocketListener

    @Mock
    lateinit var reactorListener: ReactorListener

    @Mock
    lateinit var webSocket: WebSocket

    @Mock
    lateinit var viewHandler: ViewHandler

    @Mock
    lateinit var dispatcher: CoroutineScope

    @Mock
    lateinit var dataManager: IDataManager

    @Mock
    lateinit var activity: AppCompatActivity

    @Mock
    lateinit var resources: Resources

    @Mock
    lateinit var player: IPlayer

    @Mock
    lateinit var exoPlayer: SimpleExoPlayer
    private lateinit var exoPlayerMainEventListener: MainEventListener

    @Mock
    lateinit var mediaFactory: MediaFactory

    @Mock
    lateinit var hlsMediaSource: HlsMediaSource

    @Mock
    lateinit var mediaItem: MediaItem

    @Mock
    lateinit var timeBar: MLSTimeBar

    @Mock
    lateinit var okHttpClient: OkHttpClient

    @Mock
    lateinit var youboraClient: YouboraClient

    @Mock
    lateinit var youboraPlugin: Plugin

    @Mock
    lateinit var exoplayer2Adapter: Exoplayer2Adapter

    @Mock
    lateinit var annotationMediator: AnnotationMediator

    @Mock
    lateinit var logger: Logger

    @Mock
    lateinit var casterSession: ICasterSession

    @Mock
    lateinit var cast: ICast

    @Mock
    lateinit var sessionManagerListener: ISessionManagerListener
    lateinit var castListener: tv.mycujoo.mcls.cast.ICastListener


    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)

        internalBuilder.initialize()

        whenever(okHttpClient.newWebSocket(any(), any())).thenReturn(webSocket)
        mainWebSocketListener = MainWebSocketListener()
        reactorSocket = ReactorSocket(okHttpClient, mainWebSocketListener)
        reactorSocket.setUUID("SAMPLE_UUID")

        whenever(MLSBuilder.activity).thenReturn(activity)
        whenever(MLSBuilder.createExoPlayer(any())).thenReturn(exoPlayer)
        whenever(mediaFactory.createHlsMediaSource(any())).thenReturn(hlsMediaSource)

        whenever(MLSBuilder.createReactorListener(any())).thenReturn(reactorListener)
        whenever(MLSBuilder.mlsConfiguration).thenReturn(MLSConfiguration())
        whenever(MLSBuilder.hasAnalytic).thenReturn(true)

        whenever(MLSBuilder.publicKey).thenReturn("SAMPLE_PUBLIC_KEY")
        whenever(MLSBuilder.internalBuilder).thenReturn(internalBuilder)
        whenever(internalBuilder.createYouboraPlugin(any(), any())).thenReturn(youboraPlugin)
        whenever(internalBuilder.createExoPlayerAdapter(any())).thenReturn(exoplayer2Adapter)
        whenever(internalBuilder.createYouboraClient(any())).thenReturn(youboraClient)
        whenever(internalBuilder.logger).thenReturn(logger)

        whenever(playerView.context).thenReturn(activity)
        whenever(playerView.resources).thenReturn(resources)
        whenever(playerView.getTimeBar()).thenReturn(timeBar)
        whenever(playerView.getRemotePlayerControllerView()).thenReturn(remotePlayerControllerView)

        whenever(dispatcher.coroutineContext).thenReturn(coroutineTestRule.testDispatcher)

        whenever(player.getDirectInstance()).thenReturn(exoPlayer)

        whenever(resources.getString(R.string.message_geoblocked_stream)).thenReturn("This stream cannot be watched in your area.")
        whenever(resources.getString(R.string.message_no_entitlement_stream)).thenReturn("Access to this stream is restricted.")


        whenever(player.addListener(any())).then { storeExoPlayerListener(it) }
        whenever(
            cast.initialize(
                any(),
                any()
            )
        )
            .thenAnswer {
                storeCastListener(it)
                return@thenAnswer sessionManagerListener
            }
        videoPlayerMediator = VideoPlayerMediator(
            videoPlayerConfig,
            viewHandler,
            reactorSocket,
            dispatcher,
            dataManager,
            emptyList(),
            cast,
            internalBuilder.logger
        )
        videoPlayerMediator.initialize(playerView, player, MLSBuilder)
        videoPlayerMediator.setAnnotationMediator(annotationMediator)
    }

    private fun storeExoPlayerListener(invocationOnMock: InvocationOnMock) {
        if (invocationOnMock.arguments[0] is MainEventListener) {
            exoPlayerMainEventListener = invocationOnMock.arguments[0] as MainEventListener
        }
    }

    private fun storeCastListener(invocationOnMock: InvocationOnMock) {
        if (invocationOnMock.arguments[1] is tv.mycujoo.mcls.cast.ICastListener) {
            castListener = invocationOnMock.arguments[1] as tv.mycujoo.mcls.cast.ICastListener
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `ensure seek_tolerance is applied`() {
        verify(exoPlayer).setSeekParameters(argThat(SeekParameterArgumentMatcher(MLSBuilder.mlsConfiguration.seekTolerance)))
    }

    @Test
    fun `given event with id to play, should fetch event details`() = runBlockingTest {
        videoPlayerMediator.playVideo("1eUBgUbXhriLFCT6A8E5a6Lv0R7")


        verify(dataManager).getEventDetails("1eUBgUbXhriLFCT6A8E5a6Lv0R7")
    }

    @Test
    fun `given eventEntity to play, should fetch event details`() =
        runBlockingTest {
            val event: EventEntity = getSampleEventEntity(emptyList())
            videoPlayerMediator.playVideo(event)


            verify(dataManager).getEventDetails(event.id)
        }

    @Test
    fun `given event with streamUrl, should play video`() = runBlockingTest {
        val eventEntityDetails = getSampleEventEntity(getSampleStreamList())
        whenever(dataManager.getEventDetails(eventEntityDetails.id)).thenReturn(
            Result.Success(
                eventEntityDetails
            )
        )
        whenever(mediaFactory.createMediaItem(any())).thenReturn(mediaItem)
        whenever(mediaFactory.createHlsMediaSource(any())).thenReturn(hlsMediaSource)


        videoPlayerMediator.playVideo(eventEntityDetails)


        verify(player).play(any<MediaDatum.MediaData>())
    }

    @Test
    fun `given event with streamUrl, should never pull streamUrl`() = runBlockingTest {
        val eventEntityDetails = getSampleEventEntity(getSampleStreamList())
        whenever(dataManager.getEventDetails(eventEntityDetails.id)).thenReturn(
            Result.Success(
                eventEntityDetails
            )
        )

        videoPlayerMediator.playVideo(eventEntityDetails)
        coroutineTestRule.testDispatcher.advanceTimeBy(61000L)
        videoPlayerMediator.cancelPulling()

        verify(dataManager, times(1)).getEventDetails(eventEntityDetails.id, null)
    }

    @Test
    fun `given event without streamUrl, should not play video`() = runBlockingTest {
        val event: EventEntity = getSampleEventEntity(emptyList())
        whenever(dataManager.getEventDetails(event.id)).thenReturn(Result.Success(event))
        whenever(mediaFactory.createHlsMediaSource(any<MediaItem>())).thenReturn(hlsMediaSource)


        videoPlayerMediator.playVideo(event)
        videoPlayerMediator.cancelPulling()


        verify(exoPlayer, never()).setMediaItem(any(), any<Boolean>())
    }

    @Test
    fun `given event to play which has timelineId, should fetchActions`() =
        runBlockingTest {
            val event: EventEntity = getSampleEventEntity(
                id = "42",
                streams = emptyList(),
                timelineIds = "timeline_id_01"
            )
            whenever(dataManager.getEventDetails(event.id)).thenReturn(Result.Success(event))


            videoPlayerMediator.playVideo(event.id)
            videoPlayerMediator.cancelPulling()

            val timelineIdCaptor = argumentCaptor<String>()
            val updateIdCaptor = argumentCaptor<String>()
            val callbackArgumentCaptor =
                argumentCaptor<(result: Result<Exception, ActionResponse>) -> Unit>()
            verify(annotationMediator).fetchActions(
                timelineIdCaptor.capture(),
                updateIdCaptor.capture(),
                callbackArgumentCaptor.capture()
            )
        }

    @Test
    fun `given event to play which does not have timelineId, should not call fetchActions`() =
        runBlockingTest {
            val event: EventEntity =
                getSampleEventEntity(id = "42", streams = emptyList(), timelineIds = null)
            whenever(dataManager.getEventDetails(event.id)).thenReturn(Result.Success(event))


            videoPlayerMediator.playVideo(event.id)
            videoPlayerMediator.cancelPulling()


            verify(dataManager, never()).getActions(any(), any())
        }

    @Test
    fun `given event without streamUrl, should display event info`() = runBlockingTest {
        val event: EventEntity = getSampleEventEntity(emptyList())
        whenever(dataManager.getEventDetails(event.id)).thenReturn(Result.Success(event))

        videoPlayerMediator.playVideo(event)
        videoPlayerMediator.cancelPulling()

        verify(playerView).showPreEventInformationDialog()
    }

    @Test
    fun `event with geoBlocked-stream, displays custom information dialog`() = runBlockingTest {
        val geoBlockedStream = getSampleStream(
            null,
            ErrorCodeAndMessage(ERROR_CODE_GEOBLOCKED, null)
        )
        val event: EventEntity = getSampleEventEntity(
            listOf(geoBlockedStream), EventStatus.EVENT_STATUS_SCHEDULED
        )
        whenever(dataManager.getEventDetails(event.id)).thenReturn(Result.Success(event))


        videoPlayerMediator.playVideo(event)
        videoPlayerMediator.cancelPulling()


        verify(playerView).showCustomInformationDialog("This stream cannot be watched in your area.")
        verify(player).pause()

    }

    @Test
    fun `event with noEntitlement-stream, displays custom information dialog`() = runBlockingTest {
        val noEntitlementStream = getSampleStream(
            null,
            ErrorCodeAndMessage(ERROR_CODE_NO_ENTITLEMENT, null)
        )
        val event: EventEntity = getSampleEventEntity(
            listOf(noEntitlementStream), EventStatus.EVENT_STATUS_SCHEDULED
        )
        whenever(dataManager.getEventDetails(event.id)).thenReturn(Result.Success(event))


        videoPlayerMediator.playVideo(event)
        videoPlayerMediator.cancelPulling()


        verify(playerView).showCustomInformationDialog("Access to this stream is restricted.")
        verify(player).pause()
    }


    @Test
    fun `event with unknownError-stream, displays pre-event information dialog`() =
        runBlockingTest {
            val unknownErrorStream = getSampleStream(
                null,
                ErrorCodeAndMessage(ERROR_CODE_UNSPECIFIED, null)
            )
            val event: EventEntity = getSampleEventEntity(
                listOf(unknownErrorStream), EventStatus.EVENT_STATUS_SCHEDULED
            )
            whenever(dataManager.getEventDetails(event.id)).thenReturn(Result.Success(event))


            videoPlayerMediator.playVideo(event)
            videoPlayerMediator.cancelPulling()


            verify(playerView).showPreEventInformationDialog()
            verify(playerView, never()).showCustomInformationDialog(any())
            verify(player).pause()
        }

    @Test
    fun `given event without streamUrl, should pull streamUrl periodically`() = runBlockingTest {
        val event: EventEntity = getSampleEventEntity(emptyList())
        whenever(dataManager.getEventDetails(event.id)).thenReturn(Result.Success(event))

        videoPlayerMediator.playVideo(event)
        coroutineTestRule.testDispatcher.advanceTimeBy(61000L)
        videoPlayerMediator.cancelPulling()

        verify(dataManager, times(3)).getEventDetails(event.id, null)
    }

    @Test
    fun `given event to play, should connect to reactor service`() = runBlockingTest {
        val event: EventEntity = getSampleEventEntity(emptyList())
        whenever(dataManager.getEventDetails(event.id)).thenReturn(Result.Success(event))


        videoPlayerMediator.playVideo(event)
        videoPlayerMediator.cancelPulling()


        verify(webSocket).send("joinEvent;${event.id}")
    }

    @Test
    fun `given 2nd event to play, should re-connect to reactor service`() = runBlockingTest {
        val firstEvent: EventEntity = getSampleEventEntity("42")
        whenever(dataManager.getEventDetails(firstEvent.id)).thenReturn(Result.Success(firstEvent))
        val secondEvent: EventEntity = getSampleEventEntity("57")
        whenever(dataManager.getEventDetails(secondEvent.id)).thenReturn(Result.Success(secondEvent))



        videoPlayerMediator.playVideo(firstEvent)
        videoPlayerMediator.cancelPulling()


        videoPlayerMediator.playVideo(secondEvent)
        videoPlayerMediator.cancelPulling()



        verify(webSocket).send("joinEvent;${firstEvent.id}")
        verify(webSocket).send("leaveEvent;${firstEvent.id}")
        verify(webSocket).send("joinEvent;${secondEvent.id}")
    }

    @Ignore("Event Status is not done on server yet")
    @Test
    fun `given event with anything but Started status to play, should not connect to reactor`() =
        runBlockingTest {
            val event = getSampleEventEntity(emptyList(), EventStatus.EVENT_STATUS_SCHEDULED)
            whenever(dataManager.getEventDetails(event.id)).thenReturn(Result.Success(event))



            videoPlayerMediator.playVideo(event)
            videoPlayerMediator.playVideo(
                getSampleEventEntity(
                    emptyList(),
                    EventStatus.EVENT_STATUS_RESCHEDULED
                )
            )
            videoPlayerMediator.playVideo(
                getSampleEventEntity(
                    emptyList(),
                    EventStatus.EVENT_STATUS_CANCELLED
                )
            )
            videoPlayerMediator.playVideo(
                getSampleEventEntity(
                    emptyList(),
                    EventStatus.EVENT_STATUS_POSTPONED
                )
            )
            videoPlayerMediator.playVideo(
                getSampleEventEntity(
                    emptyList(),
                    EventStatus.EVENT_STATUS_DELAYED
                )
            )
            videoPlayerMediator.playVideo(
                getSampleEventEntity(
                    emptyList(),
                    EventStatus.EVENT_STATUS_PAUSED
                )
            )
            videoPlayerMediator.playVideo(
                getSampleEventEntity(
                    emptyList(),
                    EventStatus.EVENT_STATUS_DELAYED
                )
            )
            videoPlayerMediator.playVideo(
                getSampleEventEntity(
                    emptyList(),
                    EventStatus.EVENT_STATUS_SUSPENDED
                )
            )
            videoPlayerMediator.playVideo(
                getSampleEventEntity(
                    emptyList(),
                    EventStatus.EVENT_STATUS_FINISHED
                )
            )
            videoPlayerMediator.playVideo(
                getSampleEventEntity(
                    emptyList(),
                    EventStatus.EVENT_STATUS_UNSPECIFIED
                )
            )


            verify(reactorSocket, never()).joinEvent(any())
        }

    @Test
    fun `update viewers counter in VOD stream, should hide viewers counter in player wrapper`() =
        runBlockingTest {
            val event =
                getSampleEventEntity(getSampleStreamList(), EventStatus.EVENT_STATUS_SCHEDULED)
            whenever(dataManager.getEventDetails(event.id)).thenReturn(Result.Success(event))


            whenever(exoPlayer.isCurrentWindowDynamic).thenReturn(false)


            videoPlayerMediator.playVideo(event)
            exoPlayerMainEventListener.onPlayerStateChanged(true, 0)
            mainWebSocketListener.onMessage(webSocket, "eventTotal;ck2343whlc43k0g90i92grc0u;17")


            verify(playerView).hideViewersCounter()
        }

    @Test
    fun `update viewers counter in LIVE stream, should update player view`() = runBlockingTest {
        val event = getSampleEventEntity(getSampleStreamList(), EventStatus.EVENT_STATUS_SCHEDULED)
        whenever(dataManager.getEventDetails(event.id)).thenReturn(Result.Success(event))
        videoPlayerMediator.config(defaultVideoPlayerConfig())
        whenever(player.isLive()).thenReturn(true)


        videoPlayerMediator.playVideo(event)
        exoPlayerMainEventListener.onPlayerStateChanged(true, 0)
        mainWebSocketListener.onMessage(webSocket, "eventTotal;ck2343whlc43k0g90i92grc0u;17")


        verify(playerView).updateViewersCounter("17")
    }

    @Test
    fun `given ready player state, should log event if needed`() {
        val event = getSampleEventEntity(getSampleStreamList(), EventStatus.EVENT_STATUS_STARTED)
        whenever(dataManager.currentEvent).thenReturn(event)


        exoPlayerMainEventListener.onPlayerStateChanged(true, STATE_READY)


        verify(youboraClient).logEvent(event, false)
    }

    @Test
    fun `given ready player state, should log event only once`() {
        val event = getSampleEventEntity(getSampleStreamList(), EventStatus.EVENT_STATUS_STARTED)
        whenever(dataManager.currentEvent).thenReturn(event)


        exoPlayerMainEventListener.onPlayerStateChanged(true, STATE_READY)
        exoPlayerMainEventListener.onPlayerStateChanged(true, STATE_READY)
        exoPlayerMainEventListener.onPlayerStateChanged(true, STATE_READY)


        verify(youboraClient, times(1)).logEvent(event, false)
    }

    @Test
    fun `given player with any state other than ready, should not log event`() {
        val event = getSampleEventEntity(getSampleStreamList(), EventStatus.EVENT_STATUS_STARTED)
        whenever(dataManager.currentEvent).thenReturn(event)


        exoPlayerMainEventListener.onPlayerStateChanged(true, STATE_IDLE)
        exoPlayerMainEventListener.onPlayerStateChanged(true, STATE_BUFFERING)
        exoPlayerMainEventListener.onPlayerStateChanged(true, STATE_ENDED)


        verify(youboraClient, never()).logEvent(any(), any())
    }

    /**region Cast*/

    @Test
    fun `should load remote media, when connected to remote player`() = runBlockingTest {
        val event =
            getSampleEventEntity(
                listOf(
                    Stream(
                        "id_0",
                        60000000L.toString(),
                        "http://www.google.com",
                        null
                    )
                )
            )
        whenever(dataManager.currentEvent).thenReturn(event)

        castListener.onSessionStarted(casterSession)

        verify(cast).loadRemoteMedia(any())
    }

    @Test
    fun `should do nothing, when connected to remote player with null cast-session`() {
        castListener.onSessionStarted(null)


        verify(cast, never()).loadRemoteMedia(any())
        verify(playerView, never()).switchMode(any())
        verify(player, never()).isPlaying()
        verify(player, never()).pause()
    }


    @Test
    fun `should set PlayerView mode to REMOTE, when connected to remote player`() {
        castListener.onSessionStarted(casterSession)


        verify(playerView).switchMode(PlayerControllerMode.REMOTE_CONTROLLER)
    }


    @Test
    fun `should pause local player, when connected to remote player`() {
        whenever(player.isPlaying()).thenReturn(true)
        videoPlayerMediator.playVideo(getSampleEventEntity("id_0"))

        castListener.onSessionStarted(casterSession)

        verify(player).pause()
    }

    @Test
    fun `should not pause local player if it's not playing, when connected to remote player`() {
        whenever(player.isPlaying()).thenReturn(false)
        videoPlayerMediator.playVideo(getSampleEventEntity("id_0"))

        castListener.onSessionStarted(casterSession)

        verify(player, never()).pause()
    }

    @Test
    fun `should set PlayerView mode to EXO_MODE, when disconnecting from remote player`() {
        castListener.onSessionEnding(casterSession)


        verify(playerView).switchMode(PlayerControllerMode.EXO_MODE)
    }

    @Test
    fun `onStart and onResume of cast session, stop Youbora`() {
        castListener.onSessionStarted(casterSession)
        castListener.onSessionResumed(casterSession)


        verify(youboraClient, times(2)).stop()
    }

    @Test
    fun `onEnded of cast session, start Youbora`() {
        castListener.onSessionEnded(casterSession)


        verify(youboraClient).start()
    }

    /**endregion */

    /**region Fake data*/
    companion object {
        private fun getSampleStreamList(): List<Stream> {
            return listOf(Stream("stream_id_0", Long.MAX_VALUE.toString(), "stream_url", null))
        }

        fun getSampleEventEntity(
            id: String
        ): EventEntity {
            return getSampleEventEntity(id, emptyList(), null, EventStatus.EVENT_STATUS_UNSPECIFIED)
        }

        fun getSampleEventEntity(
            streams: List<Stream>,
            status: EventStatus = EventStatus.EVENT_STATUS_UNSPECIFIED
        ): EventEntity {
            return getSampleEventEntity("42", streams, null, status)
        }

        fun getSampleEventEntity(
            id: String,
            streams: List<Stream>,
            timelineIds: String? = null,
            status: EventStatus = EventStatus.EVENT_STATUS_UNSPECIFIED
        ): EventEntity {
//        EventEntity(id=1eUBgUbXhriLFCT6A8E5a6Lv0R7, title=Test Title 0, description=Desc txt, thumbnail_url=,
//        location=Location(physical=Physical(city=Amsterdam, continent_code=EU, coordinates=Coordinates(latitude=52.3666969, longitude=4.8945398), country_code=NL, venue=)),
//        organiser=Org text, start_time=2020-07-11T07:32:46Z, status=EVENT_STATUS_SCHEDULED, streams=[Stream(fullUrl=https://rendered-europe-west.mls.mycujoo.tv/shervin/ckcfwmo4g000j0131mvc1zchu/master.m3u8)],
//        timezone=America/Los_Angeles, timeline_ids=[], metadata=tv.mycujoo.domain.entity.Metadata@ea3de11, is_test=false)
            val location =
                Location(Physical("", "", Coordinates(0.toDouble(), 0.toDouble()), "", ""))
            return EventEntity(
                id,
                "",
                "",
                "",
                null,
                location,
                "",
                null,
                status,
                streams,
                "",
                arrayListOf<String>().apply { timelineIds?.let { add(it) } },
                Metadata(),
                false
            )
        }

        fun getSampleStream(url: String?, errorCodeAndMessage: ErrorCodeAndMessage? = null) =
            Stream("id_0", "1200000", url, null, errorCodeAndMessage)

        const val SAMPLE_UUID = "aa-bb-cc-dd-ee"

    }

    /**endregion */


}