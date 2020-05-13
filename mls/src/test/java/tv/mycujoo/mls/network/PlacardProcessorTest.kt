package tv.mycujoo.mls.network

import org.junit.Before
import org.junit.Test
import tv.mycujoo.mls.core.PlacardProcessor

class PlacardProcessorTest {

    lateinit var api: Api
    lateinit var processor: PlacardProcessor

    @Before
    fun setUp() {
        api = RemoteApi()
        val processor = PlacardProcessor(api)
    }

    @Test
    fun `given LiveModePlacard01, should process`() {
        processor.process()
    }
}