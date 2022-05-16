package tv.mycujoo

import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.rules.activityScenarioRule
import dagger.hilt.android.testing.HiltAndroidRule
import org.junit.Before
import org.junit.Rule
import tv.mycujoo.mcls.R
import tv.mycujoo.mcls.api.MLSTVConfiguration
import tv.mycujoo.mcls.entity.msc.TVVideoPlayerConfig
import tv.mycujoo.mcls.tv.api.MLSTV
import tv.mycujoo.mcls.tv.api.MLSTvBuilder
import tv.mycujoo.ui.MLSTVFragment

open class E2ETvTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    var scenarioRule = activityScenarioRule<TvTestActivity>()

    protected val videoFragment = MLSTVFragment()

    protected lateinit var mMLSTV: MLSTV

    @Before
    fun startup() {
        hiltRule.inject()

        scenarioRule.scenario.onActivity { activity ->
            activity
                .supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container, videoFragment, "PLAYBACK")
                .commit()

            mMLSTV = MLSTvBuilder()
                .withMLSTvFragment(videoFragment)
                .publicKey("publicKey")
                .customPseudoUserId(TEST_PSEUDO_USER_ID)
                .identityToken(TEST_IDENTITY_TOKEN)
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

        scenarioRule.scenario.moveToState(Lifecycle.State.RESUMED)
    }

    companion object {
        const val TEST_PSEUDO_USER_ID = "123"
        const val TEST_IDENTITY_TOKEN = "456"
    }
}