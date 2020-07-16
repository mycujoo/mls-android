package tv.mycujoo.mlsapp.activity

import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.activity_main.*
import tv.mycujoo.domain.entity.EventEntity
import tv.mycujoo.domain.entity.EventStatus
import tv.mycujoo.domain.entity.OrderByEventsParam
import tv.mycujoo.mls.api.MLS
import tv.mycujoo.mls.api.MLSConfiguration
import tv.mycujoo.mls.api.PlayerEventsListener
import tv.mycujoo.mls.core.UIEventListener
import tv.mycujoo.mls.widgets.PlayerViewWrapper
import tv.mycujoo.mlsapp.R


class MainActivity : AppCompatActivity() {

    private lateinit var MLS: MLS

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
                if (fullScreen){
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE
                    playerViewWrapper.screenMode(PlayerViewWrapper.ScreenMode.LANDSCAPE)
                } else {
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT
                    playerViewWrapper.screenMode(PlayerViewWrapper.ScreenMode.LANDSCAPE)
                }
            }
        }

        MLS =
            tv.mycujoo.mls.api.MLS.Builder()
                .publicKey("3HFCBP4EQJME2EH8H0SBH9RCST0IR269")
                .withActivity(this)
                .setPlayerEventsListener(playerEventsListener)
                .setUIEventListener(uiEventListener)
                .setConfiguration(MLSConfiguration(accuracy = 1000L))
//                .hasAnalyticPlugin(true)
                .build()

        MLS.getDataProvider().getEventsLiveData()
            .observe(this, Observer { eventList -> onEventListUpdated(eventList) })

    }

    private fun onEventListUpdated(eventList: List<EventEntity>) {
        Log.i("MainActivity", "onEventListUpdated")
        eventList.firstOrNull()?.let {
            MLS.loadVideo(it)
        }
    }

    override fun onStart() {
        super.onStart()
        MLS.onStart(playerViewWrapper)
    }

    override fun onResume() {
        super.onResume()
        MLS.onResume(playerViewWrapper)

        val playerStatus = MLS.getVideoPlayer().getPlayerStatus()
        val currentPosition = playerStatus.getCurrentPosition()
        val duration = playerStatus.getDuration()

//        MLS.loadVideo(Uri.parse("https://playlists.mycujoo.football/eu/ck8u05tfu1u090hew2kgobnud/master.m3u8"))
//        MLS.loadVideo(Uri.parse("https://raw-rendered-europe-west.mls.mycujoo.tv/3619/ckbuult7c00010121qjpp3rej/index.m3u8"))
//        MLS.loadVideo(Uri.parse("https://playlists.mycujoo.football/as/ck3axeudv3m1a0hfyzlu4dw3x/master.m3u8"))

        MLS.getDataProvider().fetchEvents(
            10,
            null,
            listOf(EventStatus.EVENT_STATUS_SCHEDULED, EventStatus.EVENT_STATUS_CANCELLED),
            OrderByEventsParam.ORDER_TITLE_ASC
        )

    }


    override fun onPause() {
        super.onPause()
        MLS.onPause()
    }

    override fun onStop() {
        super.onStop()
        MLS.onStop()
    }
}
