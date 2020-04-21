package tv.mycujoo.mls

import android.content.Context
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import tv.mycujoo.mls.api.MyCujooLiveServiceImpl
import tv.mycujoo.mls.api.MyCujooLiveServiceImpl.Companion.PUBLIC_KEY


class MyCujooLiveServiceImplTest {

    @Mock
    lateinit var context: Context

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `given invalid public_key, should throw IllegalArgumentException`() {
        MyCujooLiveServiceImpl.init("", context)
    }

    @Test
    fun `given valid public_key, should return MyCujooLiveStream`() {
        assertNotNull(MyCujooLiveServiceImpl.init(PUBLIC_KEY, context))
    }


    @Test
    fun `given invalid context, should throw IllegalArgumentException`() {
        MyCujooLiveServiceImpl.init(PUBLIC_KEY, context)
    }


}
