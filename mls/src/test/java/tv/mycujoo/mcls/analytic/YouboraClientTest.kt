package tv.mycujoo.mcls.analytic

import androidx.fragment.app.FragmentActivity
import com.google.android.exoplayer2.ExoPlayer
import com.npaw.ima.ImaAdapter
import com.npaw.youbora.lib6.adapter.PlayerAdapter
import com.npaw.youbora.lib6.plugin.Options
import com.npaw.youbora.lib6.plugin.Plugin
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import tv.mycujoo.domain.entity.*
import tv.mycujoo.mcls.enum.LogLevel
import tv.mycujoo.mcls.manager.Logger
import tv.mycujoo.mcls.player.IPlayer
import tv.mycujoo.mcls.utils.UserPreferencesUtils
import tv.mycujoo.mcls.utils.UuidUtils
import kotlin.test.assertEquals

class YouboraClientTest {

    private lateinit var youboraClient: YouboraClient

    @Mock
    lateinit var plugin: Plugin

    @Mock
    lateinit var logger: Logger

    @Mock
    lateinit var uuidUtils: UuidUtils

    @Mock
    lateinit var userPreferencesUtils: UserPreferencesUtils

    @Mock
    lateinit var player: IPlayer

    @Mock
    lateinit var activity: FragmentActivity

    @Mock
    lateinit var imaAdapter: ImaAdapter

    private var options = Options()

    @Mock
    lateinit var playerAdapter: PlayerAdapter<ExoPlayer>

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        whenever(plugin.options).thenReturn(options)
        whenever(plugin.adapter).thenReturn(playerAdapter)
        whenever(uuidUtils.getUuid()).thenReturn("uuid")

        whenever(logger.getLogLevel()).thenReturn(LogLevel.VERBOSE)

        youboraClient = YouboraClient(
            logger,
            userPreferencesUtils,
            plugin,
            player,
            activity,
            imaAdapter
        )
        youboraClient.attachYouboraToPlayer(null, true)
    }

    @Test
    fun `null event should not be logged`() {
        youboraClient.logEvent(null, false) {

        }

        verify(plugin, never()).options
    }

    @Test
    fun `given valid event, should log needed params`() {
        val eventEntity = getSampleEventEntity(getSampleStreamList())

        youboraClient.logEvent(eventEntity, false) {

        }

        assertEquals(eventEntity.title, options.contentTitle)
        assertEquals(eventEntity.streams.firstOrNull()?.toString(), options.contentResource)


        assertEquals(eventEntity.id, options.contentCustomDimension2)
        assertEquals("MLS", options.contentCustomDimension14)
        assertEquals(eventEntity.streams.firstOrNull()?.id, options.contentCustomDimension15)
    }

    @Test
    fun `given start, should start plugin`() {
        youboraClient.start()

        verify(plugin.adapter).fireResume()
    }

    @Test
    fun `given stop, should stop plugin`() {
        youboraClient.stop()

        verify(plugin).fireStop()
    }

    companion object {
        const val PUBLIC_KEY = "public_key_0"
        const val UUID = "uuid_0"

        private fun getSampleStreamList(): List<Stream> {
            return listOf(Stream("stream_id_0", Long.MAX_VALUE.toString(), "stream_url", null))
        }

        fun getSampleEventEntity(
            streams: List<Stream>,
            status: EventStatus = EventStatus.EVENT_STATUS_UNSPECIFIED
        ): EventEntity {
//        EventEntity(id=1eUBgUbXhriLFCT6A8E5a6Lv0R7, title=Test Title 0, description=Desc txt, thumbnail_url=,
//        location=Location(physical=Physical(city=Amsterdam, continent_code=EU, coordinates=Coordinates(latitude=52.3666969, longitude=4.8945398), country_code=NL, venue=)),
//        organiser=Org text, start_time=2020-07-11T07:32:46Z, status=EVENT_STATUS_SCHEDULED, streams=[Stream(fullUrl=https://rendered-europe-west.mls.mycujoo.tv/shervin/ckcfwmo4g000j0131mvc1zchu/master.m3u8)],
//        timezone=America/Los_Angeles, timeline_ids=[], metadata=tv.mycujoo.domain.entity.Metadata@ea3de11, is_test=false)

            val location =
                Location(Physical("", "", Coordinates(0.toDouble(), 0.toDouble()), "", ""))
            return EventEntity(
                "42",
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
                emptyList(),
                Metadata(),
                false
            )
        }

        const val SAMPLE_UUID = "aa-bb-cc-dd-ee"


    }
}