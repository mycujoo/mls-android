package tv.mycujoo.mls.caster

import android.content.Context
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.CastSession
import com.google.android.gms.cast.framework.SessionManager
import com.google.android.gms.cast.framework.SessionManagerListener
import com.google.android.gms.cast.framework.media.RemoteMediaClient
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import kotlin.test.assertSame

class CasterTest {

    private lateinit var caster: Caster
    private lateinit var sessionManagerListener: SessionManagerListener<CastSession>

    @Mock
    lateinit var castProvider: ICastContextProvider

    @Mock
    lateinit var castListener: ICastListener

    @Mock
    lateinit var context: Context

    @Mock
    lateinit var castContext: CastContext

    @Mock
    lateinit var sessionManager: SessionManager

    @Mock
    lateinit var castSession: CastSession

    @Mock
    lateinit var remoteMediaClient: RemoteMediaClient

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        whenever(castProvider.getCastContext()).thenReturn(castContext)
        whenever(castContext.sessionManager).thenReturn(sessionManager)
        whenever(sessionManager.currentCastSession).thenReturn(castSession)
        whenever(castSession.remoteMediaClient).thenReturn(remoteMediaClient)

        caster = Caster(castProvider)
        sessionManagerListener = caster.initialize(context, castListener)
    }

    @Test
    fun `caster STARTED, must notify listener ON_CONNECTED`() {
        sessionManagerListener.onSessionStarted(castSession, "")

        verify(castListener).onConnected(any())
    }

    @Test
    fun `caster START_FAILED, must notify listener ON_DISCONNECTED`() {
        sessionManagerListener.onSessionStartFailed(castSession, 0)

        verify(castListener).onDisconnected(any())
    }

    @Test
    fun `caster RESUMED, must notify listener ON_CONNECTED`() {
        sessionManagerListener.onSessionResumed(castSession, false)

        verify(castListener).onConnected(any())
    }

    @Test
    fun `caster RESUMED_FAILED, must notify listener ON_DISCONNECTED`() {
        sessionManagerListener.onSessionResumeFailed(castSession, 0)

        verify(castListener).onDisconnected(any())
    }

    @Test
    fun `caster SESSION_ENDING, must notify listener ON_CONNECTING`() {
        sessionManagerListener.onSessionEnding(castSession)

        verify(castListener).onDisconnecting(any())
    }

    @Test
    fun `caster SESSION_ENDED, must notify listener ON_DISCONNECTED`() {
        sessionManagerListener.onSessionEnded(castSession, 0)

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
}