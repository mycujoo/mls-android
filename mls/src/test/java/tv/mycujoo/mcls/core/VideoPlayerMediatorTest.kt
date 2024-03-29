package tv.mycujoo.mcls.core

import android.content.res.Resources
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import okhttp3.OkHttpClient
import okhttp3.WebSocket
import org.junit.*
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.invocation.InvocationOnMock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.*
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
import tv.mycujoo.mcls.cast.ICast
import tv.mycujoo.mcls.cast.ICastListener
import tv.mycujoo.mcls.cast.ICasterSession
import tv.mycujoo.mcls.cast.ISessionManagerListener
import tv.mycujoo.mcls.data.IDataManager
import tv.mycujoo.mcls.entity.msc.VideoPlayerConfig
import tv.mycujoo.mcls.helper.OverlayViewHelper
import tv.mycujoo.mcls.manager.Logger
import tv.mycujoo.mcls.manager.ViewHandler
import tv.mycujoo.mcls.matcher.SeekParameterArgumentMatcher
import tv.mycujoo.mcls.mediator.AnnotationMediator
import tv.mycujoo.mcls.network.socket.BFFRTSocket
import tv.mycujoo.mcls.network.socket.MainWebSocketListener
import tv.mycujoo.mcls.network.socket.ReactorSocket
import tv.mycujoo.mcls.player.IPlayer
import tv.mycujoo.mcls.player.MediaDatum
import tv.mycujoo.mcls.utils.ThreadUtils
import tv.mycujoo.mcls.utils.UserPreferencesUtils
import tv.mycujoo.mcls.utils.UuidUtils
import tv.mycujoo.mcls.widgets.MLSPlayerView
import tv.mycujoo.mcls.widgets.PlayerControllerMode
import tv.mycujoo.mcls.widgets.RemotePlayerControllerView
import tv.mycujoo.mcls.widgets.mlstimebar.MLSTimeBar


@DelicateCoroutinesApi
@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class VideoPlayerMediatorTest {

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    /** region Mocks */
    @Mock
    private lateinit var playerView: MLSPlayerView

    @Mock
    private lateinit var remotePlayerControllerView: RemotePlayerControllerView

    @Mock
    lateinit var mMLSBuilder: MLSBuilder

    @Mock
    lateinit var userPreferencesUtils: UserPreferencesUtils

    @Mock
    lateinit var uuidUtils: UuidUtils

    @Mock
    lateinit var webSocket: WebSocket

    @Mock
    lateinit var viewHandler: ViewHandler

    @Mock
    lateinit var dataManager: IDataManager

    @Mock
    lateinit var activity: AppCompatActivity

    @Mock
    lateinit var resources: Resources

    @Mock
    lateinit var player: IPlayer

    @Mock
    lateinit var exoPlayer: ExoPlayer

    @Mock
    lateinit var timeBar: MLSTimeBar

    @Mock
    lateinit var okHttpClient: OkHttpClient

    @Mock
    lateinit var youboraClient: YouboraClient

    @Mock
    lateinit var mBFFRTSocket: BFFRTSocket

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

    @Mock
    lateinit var overlayViewHelper: OverlayViewHelper

    @Mock
    lateinit var annotationFactory: AnnotationFactory

    @Mock
    lateinit var threadUtils: ThreadUtils

    @Mock
    lateinit var testCoroutineScope: CoroutineScope
    /** endregion */

    /** region fields */
    private lateinit var castListener: ICastListener
    private lateinit var reactorSocket: ReactorSocket
    private lateinit var mainWebSocketListener: MainWebSocketListener
    private lateinit var exoPlayerMainEventListener: MainEventListener
    private lateinit var videoPlayerMediator: VideoPlayerMediator

    /** endregion */

    @Before
    fun setUp() {
        whenever(testCoroutineScope.coroutineContext)
            .thenReturn(StandardTestDispatcher())

        whenever(threadUtils.provideHandler())
            .thenReturn(Handler(Looper.getMainLooper()))

        whenever(okHttpClient.newWebSocket(any(), any())).thenReturn(webSocket)
        mainWebSocketListener = MainWebSocketListener()
        reactorSocket =
            ReactorSocket(okHttpClient, mainWebSocketListener, uuidUtils, "wss://mls-rt.mycujoo.tv")

        whenever(mMLSBuilder.activity).thenReturn(activity)

        whenever(mMLSBuilder.mlsConfiguration).thenReturn(MLSConfiguration())
        whenever(mMLSBuilder.hasAnalytic).thenReturn(true)

        whenever(mMLSBuilder.publicKey).thenReturn("SAMPLE_PUBLIC_KEY")
        whenever(mMLSBuilder.getAnalyticsAccountCode()).thenReturn("analytics_code")

        whenever(playerView.context).thenReturn(activity)
        whenever(playerView.resources).thenReturn(resources)
        whenever(playerView.getTimeBar()).thenReturn(timeBar)
        whenever(playerView.getRemotePlayerControllerView()).thenReturn(remotePlayerControllerView)

        whenever(player.getDirectInstance()).thenReturn(exoPlayer)

        whenever(resources.getString(R.string.message_geoblocked_stream)).thenReturn("This stream cannot be watched in your area.")
        whenever(resources.getString(R.string.message_no_entitlement_stream)).thenReturn("Access to this stream is restricted.")


        whenever(player.addListener(any())).then { storeExoPlayerListener(it) }
        whenever(cast.initialize(any(), any()))
            .thenAnswer {
                storeCastListener(it)
                return@thenAnswer sessionManagerListener
            }
        videoPlayerMediator = VideoPlayerMediator(
            viewHandler,
            reactorSocket,
            testCoroutineScope,
            dataManager,
            logger,
            userPreferencesUtils,
            player,
            overlayViewHelper,
            youboraClient,
            annotationFactory,
            annotationMediator,
            mBFFRTSocket,
            threadUtils
        )
        videoPlayerMediator.initialize(playerView, mMLSBuilder, listOf(), cast)
    }

    private fun storeExoPlayerListener(invocationOnMock: InvocationOnMock) {
        if (invocationOnMock.arguments[0] is MainEventListener) {
            exoPlayerMainEventListener = invocationOnMock.arguments[0] as MainEventListener
        }
    }

    private fun storeCastListener(invocationOnMock: InvocationOnMock) {
        if (invocationOnMock.arguments[1] is ICastListener) {
            castListener = invocationOnMock.arguments[1] as ICastListener
        }
    }

    @Test
    fun `ensure seek_tolerance is applied`() {
        verify(exoPlayer).setSeekParameters(argThat(SeekParameterArgumentMatcher(mMLSBuilder.mlsConfiguration.seekTolerance)))
    }

    @Test
    fun `given event with id to play, should fetch event details`(): Unit = runBlocking {
        videoPlayerMediator.playVideo("1eUBgUbXhriLFCT6A8E5a6Lv0R7")
        Thread.sleep(50)

        verify(dataManager).getEventDetails("1eUBgUbXhriLFCT6A8E5a6Lv0R7", null)
    }

    @Test
    fun `given eventEntity to play, should fetch event details`(): Unit = runBlocking {
        val event: EventEntity = getSampleEventEntity(getSampleStreamList())
        videoPlayerMediator.playVideo(event.id)

        Thread.sleep(50)

        verify(dataManager, times(1)).getEventDetails(event.id)
    }

    @Test
    fun `given eventEntity to play, should not fetch event details`(): Unit =
        runBlocking {
            val event: EventEntity = getSampleEventEntity(getSampleStreamList())
            videoPlayerMediator.playVideo(event)


            verify(dataManager, never()).getEventDetails(event.id)
        }

    @Test
    fun `given event with streamUrl, should play video`(): Unit = runBlocking {
        val eventEntityDetails = getSampleEventEntity(getSampleStreamList())
        whenever(dataManager.getEventDetails(eventEntityDetails.id)).thenReturn(
            Result.Success(
                eventEntityDetails
            )
        )


        videoPlayerMediator.playVideo(eventEntityDetails)


        verify(player).play(any<MediaDatum.MediaData>())
    }

    @Test
    fun `given event with streamUrl, should never pull streamUrl`(): Unit = runTest {
        val eventEntityDetails = getSampleEventEntity(getSampleStreamList())
        whenever(dataManager.getEventDetails(eventEntityDetails.id)).thenReturn(
            Result.Success(
                eventEntityDetails
            )
        )

        videoPlayerMediator.playVideo(eventEntityDetails)
        this.advanceTimeBy(61000L)
        videoPlayerMediator.cancelPulling()

        verify(dataManager, never()).getEventDetails(eventEntityDetails.id, null)
    }

    @Test
    fun `given event without streamUrl, should not play video`(): Unit = runBlocking {
        val event: EventEntity = getSampleEventEntity(emptyList())
        whenever(dataManager.getEventDetails(event.id)).thenReturn(Result.Success(event))


        videoPlayerMediator.playVideo(event)
        videoPlayerMediator.cancelPulling()


        verify(exoPlayer, never()).setMediaItem(any(), any<Boolean>())
    }

    @Test
    fun `given event to play which has timelineId, should fetchActions`(): Unit = runBlocking {
        val event: EventEntity = getSampleEventEntity(
            id = "42",
            streams = emptyList(),
            timelineIds = "timeline_id_01"
        )
        whenever(dataManager.getEventDetails(event.id)).thenReturn(Result.Success(event))


        videoPlayerMediator.playVideo(event.id)
        videoPlayerMediator.cancelPulling()

        Thread.sleep(50)

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
    fun `given event to play which does not have timelineId, should not call fetchActions`(): Unit =
        runBlocking {
            val event: EventEntity =
                getSampleEventEntity(id = "42", streams = emptyList(), timelineIds = null)
            whenever(dataManager.getEventDetails(event.id)).thenReturn(Result.Success(event))


            videoPlayerMediator.playVideo(event.id)
            videoPlayerMediator.cancelPulling()


            verify(dataManager, never()).getActions(any(), any())
        }

    @Test
    fun `given event without streamUrl, should display event info`(): Unit = runBlocking {
        val event: EventEntity = getSampleEventEntity(emptyList())
        whenever(dataManager.getEventDetails(event.id)).thenReturn(Result.Success(event))

        videoPlayerMediator.playVideo(event)
        videoPlayerMediator.cancelPulling()

        verify(playerView).showPreEventInformationDialog()
    }

    @Test
    fun `event with geoBlocked-stream, displays custom information dialog`(): Unit = runBlocking {
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
    fun `event with noEntitlement-stream, displays custom information dialog`(): Unit =
        runBlocking {
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
    fun `event with unknownError-stream, displays pre-event information dialog`(): Unit =
        runBlocking {
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
    fun `given event without streamUrl, should pull streamUrl periodically`(): Unit = runTest {
        val event: EventEntity = getSampleEventEntity(emptyList())
        whenever(dataManager.getEventDetails(event.id)).thenReturn(Result.Success(event))

        videoPlayerMediator.playVideo(event)
        this.advanceTimeBy(61000L)
        videoPlayerMediator.cancelPulling()

        verify(dataManager, times(2)).getEventDetails(event.id)
    }

    @Test
    fun `given event to play, should connect to reactor service`(): Unit = runBlocking {
        val event: EventEntity = getSampleEventEntity(emptyList())
        whenever(dataManager.getEventDetails(event.id)).thenReturn(Result.Success(event))


        videoPlayerMediator.playVideo(event)
        videoPlayerMediator.cancelPulling()


        verify(webSocket).send("joinEvent;${event.id}")
    }

    @Test
    fun `given 2nd event to play, should re-connect to reactor service`(): Unit = runBlocking {
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

    @Test
    fun `update viewers counter in VOD stream, should hide viewers counter in player wrapper`(): Unit =
        runBlocking {
            val event =
                getSampleEventEntity(getSampleStreamList(), EventStatus.EVENT_STATUS_SCHEDULED)
            whenever(dataManager.getEventDetails(event.id)).thenReturn(Result.Success(event))


            videoPlayerMediator.playVideo(event)
            exoPlayerMainEventListener.onPlayWhenReadyChanged(true, 0)
            mainWebSocketListener.onMessage(webSocket, "eventTotal;ck2343whlc43k0g90i92grc0u;17")


            verify(playerView).hideViewersCounter()
        }

    @Test
    fun `update viewers counter in LIVE stream, should update player view`(): Unit = runBlocking {
        val event = getSampleEventEntity(getSampleStreamList(), EventStatus.EVENT_STATUS_SCHEDULED)
        whenever(dataManager.getEventDetails(event.id)).thenReturn(Result.Success(event))
        videoPlayerMediator.config(VideoPlayerConfig.default())
        whenever(player.isLive()).thenReturn(true)


        videoPlayerMediator.playVideo(event)
        exoPlayerMainEventListener.onPlayWhenReadyChanged(true, 0)
        mainWebSocketListener.onMessage(webSocket, "eventTotal;ck2343whlc43k0g90i92grc0u;17")


        verify(playerView).updateViewersCounter("17")
    }

    @Test
    fun `given ready player state, should log event if needed`() {
        val event = getSampleEventEntity(getSampleStreamList(), EventStatus.EVENT_STATUS_STARTED)
        whenever(dataManager.currentEvent).thenReturn(event)


        exoPlayerMainEventListener.onPlayWhenReadyChanged(true, STATE_READY)


        verify(youboraClient).logEvent(event, false) {

        }
    }

    @Test
    fun `given ready player state, should log event only once`() {
        val event = getSampleEventEntity(getSampleStreamList(), EventStatus.EVENT_STATUS_STARTED)
        whenever(dataManager.currentEvent).thenReturn(event)


        exoPlayerMainEventListener.onPlayWhenReadyChanged(true, STATE_READY)
        exoPlayerMainEventListener.onPlayWhenReadyChanged(true, STATE_READY)
        exoPlayerMainEventListener.onPlayWhenReadyChanged(true, STATE_READY)


        verify(youboraClient, times(1)).logEvent(event, false) {

        }
    }

    @Test
    fun `given player with any state other than ready, should not log event`() {
        exoPlayerMainEventListener.onPlayWhenReadyChanged(true, STATE_IDLE)
        exoPlayerMainEventListener.onPlayWhenReadyChanged(true, STATE_BUFFERING)
        exoPlayerMainEventListener.onPlayWhenReadyChanged(true, STATE_ENDED)


        verify(youboraClient, never()).logEvent(any(), any()) {

        }
    }

    /**region Cast*/

    @Test
    fun `should load remote media, when connected to remote player`(): Unit = runBlocking {
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
        castListener.onSessionStarted(mock())


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
        videoPlayerMediator.playVideo(getSampleEventEntity(getSampleStreamList()))

        castListener.onSessionStarted(casterSession)

        verify(player, times(1)).pause()
    }

    @Test
    fun `should not pause local player if it's not playing, when connected to remote player`() {
        whenever(player.isPlaying()).thenReturn(false)
        videoPlayerMediator.playVideo(getSampleEventEntity(getSampleStreamList()))

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

    }

    /**endregion */


}