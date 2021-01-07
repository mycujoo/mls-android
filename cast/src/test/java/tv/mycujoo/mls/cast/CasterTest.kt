package tv.mycujoo.mls.cast

import android.content.Context
import com.google.android.gms.cast.MediaSeekOptions
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.CastSession
import com.google.android.gms.cast.framework.SessionManager
import com.google.android.gms.cast.framework.media.RemoteMediaClient
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import kotlin.test.assertEquals
import kotlin.test.assertSame

class CasterTest {

    private lateinit var caster: Cast
    private lateinit var sessionManagerListener: ISessionManagerListener

    @Mock
    lateinit var castProvider: ICastContextProvider

    @Mock
    lateinit var castListener: ICastListener

    @Mock
    lateinit var sessionManagerWrapper: SessionManagerWrapper

    @Mock
    lateinit var context: Context

    @Mock
    lateinit var castContext: CastContext

    @Mock
    lateinit var sessionManager: SessionManager

    @Mock
    lateinit var castSession: CastSession

    @Mock
    lateinit var casterSession: CasterSession

    @Mock
    lateinit var remoteMediaClient: RemoteMediaClient

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        whenever(castProvider.getCastContext()).thenReturn(castContext)
        whenever(castContext.sessionManager).thenReturn(sessionManager)
        whenever(sessionManager.currentCastSession).thenReturn(castSession)
        whenever(castSession.remoteMediaClient).thenReturn(remoteMediaClient)

        caster = Cast(castProvider)
        sessionManagerListener = caster.initialize(context, castListener)
    }

    @Test
    fun `caster STARTED, must notify listener ON_CONNECTED`() {
        sessionManagerListener.onSessionStarted(casterSession, "")

        verify(castListener).onConnected(any())
    }

    @Test
    fun `caster START_FAILED, must notify listener ON_DISCONNECTED`() {
        sessionManagerListener.onSessionStartFailed(casterSession, 0)

        verify(castListener).onDisconnected(any())
    }

    @Test
    fun `caster RESUMED, must notify listener ON_CONNECTED`() {
        sessionManagerListener.onSessionResumed(casterSession, false)

        verify(castListener).onConnected(any())
    }

    @Test
    fun `caster RESUMED_FAILED, must notify listener ON_DISCONNECTED`() {
        sessionManagerListener.onSessionResumeFailed(casterSession, 0)

        verify(castListener).onDisconnected(any())
    }

    @Test
    fun `caster SESSION_ENDING, must notify listener ON_CONNECTING`() {
        sessionManagerListener.onSessionEnding(casterSession)

        verify(castListener).onDisconnecting(any())
    }

    @Test
    fun `caster SESSION_ENDED, must notify listener ON_DISCONNECTED`() {
        sessionManagerListener.onSessionEnded(casterSession, 0)

        verify(castListener).onDisconnected(any())
    }

    @Test
    fun getRemoteMediaClient() {
        assertSame(remoteMediaClient, castSession.remoteMediaClient)
    }

    @Test
    fun `onResume should add sessionManagerListener to sessionManager`() {
        caster.onResume()

        verify(sessionManager).addSessionManagerListener(any(), any<Class<CastSession>>())
    }

    @Test
    fun `onResume should update playback location as local`() {
        // castSession is not connected by default

        caster.onResume()

        verify(castListener).onPlaybackLocationUpdated(true)
    }

    @Test
    fun `onResume should update playback location as remote`() {
        whenever(castSession.isConnected).thenReturn(true)

        caster.onResume()

        verify(castListener).onPlaybackLocationUpdated(false)
    }

    @Test
    fun `onSeekTo calls remote-media-client with position as value`() {
        val position = 5000L
        caster.seekTo(position)


        val mediaSeekOptionsCaptor = ArgumentCaptor.forClass(MediaSeekOptions::class.java)
        verify(remoteMediaClient).seek(mediaSeekOptionsCaptor.capture())
        assertEquals(position, mediaSeekOptionsCaptor.value.position)
    }
}