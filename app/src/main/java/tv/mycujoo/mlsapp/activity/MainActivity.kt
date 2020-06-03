package tv.mycujoo.mlsapp.activity

import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import tv.mycujoo.mls.api.HighlightListParams
import tv.mycujoo.mls.api.MyCujooLiveService
import tv.mycujoo.mls.api.PlayerEvents
import tv.mycujoo.mls.model.ConfigParams
import tv.mycujoo.mlsapp.R


class MainActivity : AppCompatActivity() {

    private lateinit var myCujooLiveService: MyCujooLiveService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val playerEvents = object : PlayerEvents {


            override fun onPlay() {
                Log.i("PlayerEvents", "onPlay()")
            }

            override fun onPause() {
                Log.i("PlayerEvents", "onPause()")
            }

            override fun onEnd() {
                Log.i("PlayerEvents", "onEnd()")
            }
        }


        myCujooLiveService =
            MyCujooLiveService.Builder()
                .publicKey("USER_PUBLIC_KEY_123")
                .hasAnalyticPlugin(true)
                .withActivity(this)
                .withContext(this)
                .defaultPlayerController(true)
                .highlightList(HighlightListParams(highlightsRecyclerView))
                .setPlayerEvents(playerEvents)
                .build()


        playButton?.setOnClickListener { myCujooLiveService.getPlayerController().playerPlay() }
        pauseButton?.setOnClickListener { myCujooLiveService.getPlayerController().playerPause() }
        nextButton?.setOnClickListener { myCujooLiveService.getPlayerController().playerNext() }
        prevButton?.setOnClickListener { myCujooLiveService.getPlayerController().playerPrevious() }


        myCujooLiveService.loadVideo(Uri.parse("https://playlists.mycujoo.football/eu/ck8u05tfu1u090hew2kgobnud/master.m3u8"))


    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        myCujooLiveService.onConfigurationChanged(
            ConfigParams(newConfig, hasPortraitActionBar = true, hasLandscapeActionBar = false),
            window.decorView,
            supportActionBar
        )
    }

    override fun onStart() {
        super.onStart()
        myCujooLiveService.onStart(playerViewWrapper)
    }

    override fun onResume() {
        super.onResume()
        myCujooLiveService.onResume(playerViewWrapper)
    }


    override fun onPause() {
        super.onPause()
        myCujooLiveService.onPause()
    }

    override fun onStop() {
        super.onStop()
        myCujooLiveService.onStop()
    }
}
