package tv.mycujoo.mlsapp.activity

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import tv.mycujoo.mls.api.MLS
import tv.mycujoo.mls.api.PlayerEventsListener
import tv.mycujoo.mls.core.UIEventListener
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
            }
        }

        MLS =
            tv.mycujoo.mls.api.MLS.Builder()
                .publicKey("USER_PUBLIC_KEY_123")
                .withActivity(this)
                .setPlayerEventsListener(playerEventsListener)
                .setUIEventListener(uiEventListener)
//                .hasAnalyticPlugin(true)
//                .defaultPlayerController(true)
//                .highlightList(HighlightListParams(highlightsRecyclerView))
                .build()


    }

    override fun onStart() {
        super.onStart()
        MLS.onStart(playerViewWrapper)
    }

    override fun onResume() {
        super.onResume()
        MLS.onResume(playerViewWrapper)

        val playerController = MLS.getVideoPlayer().getPlayerController()

        playButton?.setOnClickListener { playerController.play() }
        pauseButton?.setOnClickListener { playerController.pause() }
        nextButton?.setOnClickListener { playerController.next() }
        prevButton?.setOnClickListener { playerController.previous() }

        val playerStatus = MLS.getVideoPlayer().getPlayerStatus()
        val currentPosition = playerStatus.getCurrentPosition()
        val duration = playerStatus.getDuration()

        MLS.loadVideo(Uri.parse("https://playlists.mycujoo.football/eu/ck8u05tfu1u090hew2kgobnud/master.m3u8"))

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
