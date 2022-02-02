package tv.mycujoo

import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.launchActivity
import androidx.test.ext.junit.rules.activityScenarioRule
import dagger.hilt.android.testing.HiltAndroidRule
import org.junit.Before
import org.junit.Rule
import tv.mycujoo.mcls.R
import tv.mycujoo.mcls.api.MLS
import tv.mycujoo.mcls.api.MLSConfiguration
import tv.mycujoo.mcls.api.MLSTVConfiguration
import tv.mycujoo.mcls.entity.msc.TVVideoPlayerConfig
import tv.mycujoo.mcls.entity.msc.VideoPlayerConfig
import tv.mycujoo.mcls.tv.api.MLSTV
import tv.mycujoo.mcls.tv.api.MLSTvBuilder
import tv.mycujoo.mcls.widgets.MLSPlayerView
import tv.mycujoo.ui.MLSTVFragment
import javax.inject.Inject

open class E2ETvTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    protected lateinit var scenarioRule: ActivityScenario<TvTestActivity>

    protected val videoFragment = MLSTVFragment()

    @Inject
    lateinit var mlsTestBuilder: MLSTVTestBuilder

    protected lateinit var mMLSTV: MLSTV

    @Before
    fun startup() {
        hiltRule.inject()

        scenarioRule = launchActivity()

        scenarioRule.onActivity { activity ->
            activity
                .supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container, videoFragment, "PLAYBACK")
                .commit()
        }

        scenarioRule.moveToState(Lifecycle.State.RESUMED)
        scenarioRule.onActivity {
            mMLSTV = MLSTvBuilder()
                .withMLSTvFragment(videoFragment)
                .publicKey("publicKey")
                .withContext(ApplicationProvider.getApplicationContext())
                .setConfiguration(
                    MLSTVConfiguration(
                        1000L,
                        TVVideoPlayerConfig(
                            primaryColor = "#ff0000", secondaryColor = "#fff000",
                            autoPlay = true,
                            showBackForwardsButtons = true,
                            showSeekBar = true,
                            showTimers = true,
                            showLiveViewers = true,
                        )
                    )
                )
                .build()
        }
    }
}