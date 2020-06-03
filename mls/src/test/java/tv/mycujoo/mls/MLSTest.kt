package tv.mycujoo.mls

import android.content.Context
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations


class MLSTest {

    @Mock
    lateinit var context: Context


    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
    }

    @Ignore
    @Test(expected = IllegalArgumentException::class)
    fun `given invalid public_key, should throw IllegalArgumentException`() {
    }

    @Ignore
    @Test
    fun `given valid public_key, should return MyCujooLiveStream`() {
    }


    @Ignore
    @Test
    fun `given invalid context, should throw IllegalArgumentException`() {
    }


}
