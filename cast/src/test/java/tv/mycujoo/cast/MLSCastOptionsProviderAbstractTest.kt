package tv.mycujoo.cast

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import tv.mycujoo.cast.EmptyMLSCastOptionsProvider.Companion.SAMPLE_APP_ID

class MLSCastOptionsProviderAbstractTest {

    private lateinit var castOptionsProvider: EmptyMLSCastOptionsProvider

    @Before
    fun setUp() {
        castOptionsProvider = EmptyMLSCastOptionsProvider()
    }

    @Test
    fun getReceiverAppId() {
        assertEquals(SAMPLE_APP_ID, castOptionsProvider.getReceiverAppId())
    }
}