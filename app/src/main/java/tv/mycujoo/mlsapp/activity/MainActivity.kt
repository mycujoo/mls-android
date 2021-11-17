package tv.mycujoo.mlsapp.activity

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import android.content.res.Configuration.ORIENTATION_PORTRAIT
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintSet
import org.joda.time.DateTime
import tv.mycujoo.domain.entity.EventEntity
import tv.mycujoo.domain.entity.EventStatus
import tv.mycujoo.domain.entity.OrderByEventsParam
import tv.mycujoo.domain.entity.Stream
import tv.mycujoo.mcls.api.MLS
import tv.mycujoo.mcls.api.MLSBuilder
import tv.mycujoo.mcls.api.MLSConfiguration
import tv.mycujoo.mcls.api.PlayerEventsListener
import tv.mycujoo.mcls.core.UIEventListener
import tv.mycujoo.mcls.entity.msc.VideoPlayerConfig
import tv.mycujoo.mcls.widgets.MLSPlayerView
import tv.mycujoo.mlsapp.BuildConfig.MCLS_TEST_EVENT_ID
import tv.mycujoo.mlsapp.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    lateinit var activityMainBindings: ActivityMainBinding

    private lateinit var mMLS: MLS
    var isFullScreen = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBindings = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBindings.root)

        constraintMLSPlayerView(resources.configuration.orientation)

        val playerEventsListener = object : PlayerEventsListener {
            override fun onIsPlayingChanged(playing: Boolean) {
                Log.i("PlayerEvents", "onIsPlayingChanged() $playing")
            }

            override fun onPlayerStateChanged(playbackState: Int) {
                Log.i("PlayerEvents", "onPlayerStateChanged() $playbackState")
            }
        }

        val uiEventListener = object : UIEventListener {
            override fun onFullScreenButtonClicked(fullScreen: Boolean) {
                Log.d("uiEventListener", "onFullScreenButtonClicked $fullScreen")
                isFullScreen = fullScreen
                requestedOrientation = if (fullScreen) {
                    ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE
                } else {
                    ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT
                }
            }
        }

        mMLS =
            MLSBuilder()
                .withActivity(this)
                .setPlayerEventsListener(playerEventsListener)
                .setUIEventListener(uiEventListener)
                .setConfiguration(
                    MLSConfiguration(
                        1000L,
                        VideoPlayerConfig(
                            primaryColor = "#ff0000", secondaryColor = "#fff000",
                            autoPlay = true,
                            enableControls = true,
                            showPlayPauseButtons = true,
                            showBackForwardsButtons = true,
                            showSeekBar = true,
                            showTimers = true,
                            showFullScreenButton = true,
                            showLiveViewers = true,
                            showEventInfoButton = false
                        )
                    )
                )
                .build()


        activityMainBindings.mainActivityPlayButton.setOnClickListener {
            mMLS.getVideoPlayer().playVideo(MCLS_TEST_EVENT_ID)
        }

        activityMainBindings.mainActivityPlayEventButton.setOnClickListener {
            mMLS.getVideoPlayer().playVideo(
                EventEntity(
                    id = "ckvwbcerfqyub0hbqwbkyrvk1",
                    title = "Brescia Calcio Femminile vs Pro Sesto Femminile",
                    description = "Brescia Calcio Femminile vs Pro Sesto Femminile - Serie B Femminile",
                    thumbnailUrl = "https://m-tv.imgix.net/07d68ec5a2469a64ef15e3a9c381bd1ff89b8d08b0ea0271fe3067361c6743df.jpg",
                    poster_url = null,
                    location = null,
                    organiser = null,
                    start_time = DateTime.parse("2021-11-14T14:30:00.000+01:00"),
                    status = EventStatus.EVENT_STATUS_FINISHED,
                    streams = listOf(
                        Stream(
                            id = "ckvwbcerfqyub0hbqwbkyrvk1",
                            dvrWindowString = null,
                            fullUrl = "https://playlists.mycujoo.football/eu/ckvwbajqyr3hu0gbljp9k4t9w/master.m3u8",
                            widevine = null,
                            errorCodeAndMessage = null
                        )
                    ),
                    timezone = null,
                    timeline_ids = listOf(),
                    metadata = null,
                    is_test = false,
                    isNativeMLS = false
                )
            )
        }


        activityMainBindings.mainActivityPlayButton2.setOnClickListener {
            mMLS.getDataProvider().fetchEvents(
                pageSize = 10,
                pageToken = null,
                eventStatus = listOf(
                    EventStatus.EVENT_STATUS_SCHEDULED,
                    EventStatus.EVENT_STATUS_CANCELLED
                ),
                orderBy = OrderByEventsParam.ORDER_TITLE_ASC,
                fetchEventCallback = { eventList: List<EventEntity>, _: String, _: String ->
                    mMLS.getVideoPlayer().playVideo(eventList.first())
                }
            )
        }
    }


    override fun onStart() {
        super.onStart()
        mMLS.onStart(activityMainBindings.mlsPlayerView)
    }

    override fun onResume() {
        super.onResume()
        mMLS.onResume(activityMainBindings.mlsPlayerView)
    }


    override fun onPause() {
        mMLS.onPause()
        super.onPause()
    }

    override fun onStop() {
        mMLS.onStop()
        super.onStop()
    }

    override fun onDestroy() {
        mMLS.onDestroy()
        super.onDestroy()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        constraintMLSPlayerView(newConfig.orientation)
    }

    private fun constraintMLSPlayerView(orientation: Int) {
        if (orientation == ORIENTATION_LANDSCAPE) {
            val constraintSet = ConstraintSet()
            constraintSet.clone(activityMainBindings.mainActivityRootLayout)
            constraintSet.constrainWidth(
                activityMainBindings.mlsPlayerView.id,
                ConstraintSet.MATCH_CONSTRAINT_SPREAD
            )
            constraintSet.constrainHeight(activityMainBindings.mlsPlayerView.id, 800)

            constraintSet.applyTo(activityMainBindings.mainActivityRootLayout)


            activityMainBindings.mlsPlayerView.setScreenResizeMode(resizeMode = MLSPlayerView.ResizeMode.RESIZE_MODE_FIXED_HEIGHT)
            activityMainBindings.mlsPlayerView.setFullscreen(isFullscreen = true)
        } else if (orientation == ORIENTATION_PORTRAIT) {
            val constraintSet = ConstraintSet()
            constraintSet.clone(activityMainBindings.mainActivityRootLayout)
            constraintSet.constrainWidth(
                activityMainBindings.mlsPlayerView.id,
                ConstraintSet.MATCH_CONSTRAINT_SPREAD
            )
            constraintSet.constrainHeight(activityMainBindings.mlsPlayerView.id, 800)

            constraintSet.applyTo(activityMainBindings.mainActivityRootLayout)


            activityMainBindings.mlsPlayerView.setScreenResizeMode(resizeMode = MLSPlayerView.ResizeMode.RESIZE_MODE_FIT)
            activityMainBindings.mlsPlayerView.setFullscreen(isFullscreen = false)
        }
    }
}
