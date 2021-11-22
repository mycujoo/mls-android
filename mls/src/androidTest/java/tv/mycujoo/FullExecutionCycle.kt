package tv.mycujoo

import android.app.Activity
import android.content.res.Configuration
import android.view.View
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.activityScenarioRule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import tv.mycujoo.mcls.R
import tv.mycujoo.mcls.api.MLS
import tv.mycujoo.mcls.api.MLSBuilder
import tv.mycujoo.mcls.api.MLSConfiguration
import tv.mycujoo.mcls.entity.msc.VideoPlayerConfig
import tv.mycujoo.mcls.widgets.MLSPlayerView

@HiltAndroidTest
class FullExecutionCycle {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    var scenarioRule = activityScenarioRule<TestActivity>()

    private lateinit var mMLSPlayerView: MLSPlayerView
    private lateinit var mMLS: MLS

    @Before
    fun startup() {
        hiltRule.inject()

        scenarioRule.scenario.onActivity { activity ->

            mMLSPlayerView = activity.findViewById(R.id.testMlsPlayerView)

            constraintMLSPlayerView(activity.resources.configuration.orientation, activity)
            mMLS = MLSBuilder()
                .publicKey("ss")
                .withActivity(activity)
                .setConfiguration(
                    MLSConfiguration(
                        1000L,
                        VideoPlayerConfig(
                            primaryColor = "#0000ff", secondaryColor = "#fff000",
                            autoPlay = false,
                            enableControls = true,
                            showPlayPauseButtons = true,
                            showBackForwardsButtons = true,
                            showSeekBar = true,
                            showTimers = true,
                            showFullScreenButton = true,
                            showLiveViewers = true,
                            showEventInfoButton = true
                        )
                    )
                )
                .build()
        }
    }

    @Test
    fun testInitialStartup() {
        onView(withId(mMLSPlayerView.id)).check(
            matches(isDisplayed())
        )

//        mMLS.getVideoPlayer().playVideo("")

        Thread.sleep(10000)
    }


    private fun constraintMLSPlayerView(orientation: Int, activity: Activity) {
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            val constraintSet = ConstraintSet()
            constraintSet.clone(
                activity.findViewById<ConstraintLayout>(R.id.testActivityRootLayout)
            )
            constraintSet.constrainWidth(
                R.id.testMlsPlayerView,
                ConstraintSet.MATCH_CONSTRAINT_SPREAD
            )
            constraintSet.constrainHeight(R.id.testMlsPlayerView, 800)

            constraintSet.applyTo(activity.findViewById(R.id.testActivityRootLayout))

            mMLSPlayerView.setScreenResizeMode(resizeMode = MLSPlayerView.ResizeMode.RESIZE_MODE_FIXED_HEIGHT)
            mMLSPlayerView.setFullscreen(isFullscreen = true)
        } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            val constraintSet = ConstraintSet()
            constraintSet.clone(activity.findViewById<ConstraintLayout>(R.id.testActivityRootLayout))
            constraintSet.constrainWidth(
                R.id.testMlsPlayerView,
                ConstraintSet.MATCH_CONSTRAINT_SPREAD
            )
            constraintSet.constrainHeight(mMLSPlayerView.id, 800)

            constraintSet.applyTo(activity.findViewById(R.id.testActivityRootLayout))


            mMLSPlayerView.setScreenResizeMode(resizeMode = MLSPlayerView.ResizeMode.RESIZE_MODE_FIT)
            mMLSPlayerView.setFullscreen(isFullscreen = false)
        }
    }
}
