package tv.mycujoo.mls.utils

import android.content.Intent
import android.content.res.Resources
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.launchActivity
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import tv.mycujoo.domain.usecase.GetActionsFromJSONUseCase
import tv.mycujoo.mls.BlankActivity
import tv.mycujoo.mls.R

@RunWith(AndroidJUnit4::class)
class ParsingRawResourceTest {

    private lateinit var resources: Resources

    @Before
    fun setUp() {
        val intent = Intent(ApplicationProvider.getApplicationContext(), BlankActivity::class.java)
        val scenario = launchActivity<BlankActivity>(intent)
        scenario.onActivity { activity ->
            resources = activity.resources
        }
    }

    @Test
    fun parseRawResource() {
//        val inputStream = resources.openRawResource(R.raw.actions)
//        val string = StringUtils.inputStreamToString(inputStream)
//        string

        val actionResponse = GetActionsFromJSONUseCase.fromRawResource(resources)
        assert(actionResponse != null)
    }

    @Test
    fun test2() {
    }
}