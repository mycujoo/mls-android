package tv.mycujoo.mlsapp.activity

import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import tv.mycujoo.mls.api.MLS
import tv.mycujoo.mls.api.PlayerEventsListener
import tv.mycujoo.mls.model.ConfigParams
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


        MLS =
            tv.mycujoo.mls.api.MLS.Builder()
                .publicKey("USER_PUBLIC_KEY_123")
                .withActivity(this)
                .setPlayerEventsListener(playerEventsListener)
//                .hasAnalyticPlugin(true)
//                .defaultPlayerController(true)
//                .highlightList(HighlightListParams(highlightsRecyclerView))
                .build()


        playButton?.setOnClickListener { MLS.getVideoPlayer().getPlayerController().play() }
        pauseButton?.setOnClickListener { MLS.getVideoPlayer().getPlayerController().pause() }
        nextButton?.setOnClickListener { MLS.getVideoPlayer().getPlayerController().next() }
        prevButton?.setOnClickListener { MLS.getVideoPlayer().getPlayerController().previous() }


    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        MLS.onConfigurationChanged(
            ConfigParams(
                newConfig,
                hasPortraitActionBar = true,
                hasLandscapeActionBar = false
            ),
            window.decorView,
            supportActionBar
        )
    }

    override fun onStart() {
        super.onStart()
        MLS.onStart(playerViewWrapper)
    }

    override fun onResume() {
        super.onResume()
        MLS.onResume(playerViewWrapper)

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
