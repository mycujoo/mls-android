package tv.mycujoo.mls.api

import android.app.Activity
import android.content.res.AssetManager
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Before
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import tv.mycujoo.mls.widgets.PlayerViewWrapper

class MLSTest {

    lateinit var MLS: MLS

    @Mock
    lateinit var activity: Activity

    @Mock
    lateinit var assetManager: AssetManager

    @Mock
    lateinit var playerWrapper: PlayerViewWrapper


    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        whenever(activity.assets).thenReturn(assetManager)


        MLS =
            MLSTestBuilder()
                .publicKey("3HFCBP4EQJME2EH8H0SBH9RCST0IR269")
                .withActivity(activity)
                .setConfiguration(MLSConfiguration(accuracy = 1000L))
                .build()
    }

}