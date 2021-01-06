package tv.mycujoo.mls.caster

import com.google.android.gms.cast.framework.CastSession
import com.google.android.gms.cast.framework.media.RemoteMediaClient
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class CasterSessionTest {
    @Mock
    lateinit var castSession: CastSession
    @Mock
    lateinit var remoteMediaClient: RemoteMediaClient

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        whenever(castSession.remoteMediaClient).thenReturn(remoteMediaClient)
    }

    @Test
    fun `not initialized Caster-session returns null as remote-media-client`() {
        val casterSession = CasterSession()

        assertNull(casterSession.getRemoteMediaClient())
    }

    @Test
    fun `initialized Caster-session returns null as remote-media-client`() {
        val casterSession = CasterSession()
        casterSession.castSession = castSession

        assertNotNull(casterSession.getRemoteMediaClient())
    }
}