package tv.mycujoo.mlsapp.activity

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import android.content.res.Configuration.ORIENTATION_PORTRAIT
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintSet
import kotlinx.android.synthetic.main.activity_main.*
import tv.mycujoo.domain.entity.EventEntity
import tv.mycujoo.domain.entity.EventStatus
import tv.mycujoo.domain.entity.OrderByEventsParam
import tv.mycujoo.mls.api.MLS
import tv.mycujoo.mls.api.MLSBuilder
import tv.mycujoo.mls.api.MLSConfiguration
import tv.mycujoo.mls.api.PlayerEventsListener
import tv.mycujoo.mls.core.UIEventListener
import tv.mycujoo.mls.entity.msc.VideoPlayerConfig
import tv.mycujoo.mls.widgets.MLSPlayerView
import tv.mycujoo.mlsapp.R


class MainActivity : AppCompatActivity() {

    private lateinit var MLS: MLS
    var isFullScreen = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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

        MLS =
            MLSBuilder()
                .publicKey("YOUR_PUBLIC_KEY_HERE")
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
                            showEventInfoButton = false,
                            showCastButton = true
                        )
                    )
                )
                .build()



        mainActivityPlayButton.setOnClickListener {
            MLS.getVideoPlayer().playVideo("EVENT_ID_HERE")
        }
        mainActivityPlayButton2.setOnClickListener {
            MLS.getDataProvider().fetchEvents(
                pageSize = 10,
                pageToken = null,
                eventStatus = listOf(
                    EventStatus.EVENT_STATUS_SCHEDULED,
                    EventStatus.EVENT_STATUS_CANCELLED
                ),
                orderBy = OrderByEventsParam.ORDER_TITLE_ASC,
                fetchEventCallback = { eventList: List<EventEntity>, previousPageToken: String, nextPageToken: String ->
                    MLS.getVideoPlayer().playVideo(eventList.first())
                }
            )
        }

    }


    override fun onStart() {
        super.onStart()
        MLS.onStart(mlsPlayerView)
    }

    override fun onResume() {
        super.onResume()
        MLS.onResume(mlsPlayerView)
    }


    override fun onPause() {
        super.onPause()
        MLS.onPause()
    }

    override fun onStop() {
        super.onStop()
        MLS.onStop()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        constraintMLSPlayerView(newConfig.orientation)

    }

    private fun constraintMLSPlayerView(orientation: Int) {
        if (orientation == ORIENTATION_LANDSCAPE) {
            val constraintSet = ConstraintSet()
            constraintSet.clone(mainActivityRootLayout)
            constraintSet.constrainWidth(mlsPlayerView.id, ConstraintSet.MATCH_CONSTRAINT_SPREAD)
            constraintSet.constrainHeight(mlsPlayerView.id, 800)

            constraintSet.applyTo(mainActivityRootLayout)


            mlsPlayerView.setScreenResizeMode(resizeMode = MLSPlayerView.ResizeMode.RESIZE_MODE_FIXED_HEIGHT)
            mlsPlayerView.setFullscreen(isFullscreen = true)
        } else if (orientation == ORIENTATION_PORTRAIT) {
            val constraintSet = ConstraintSet()
            constraintSet.clone(mainActivityRootLayout)
            constraintSet.constrainWidth(mlsPlayerView.id, ConstraintSet.MATCH_CONSTRAINT_SPREAD)
            constraintSet.constrainHeight(mlsPlayerView.id, 800)

            constraintSet.applyTo(mainActivityRootLayout)


            mlsPlayerView.setScreenResizeMode(resizeMode = MLSPlayerView.ResizeMode.RESIZE_MODE_FIT)
            mlsPlayerView.setFullscreen(isFullscreen = false)
        }
    }

}
