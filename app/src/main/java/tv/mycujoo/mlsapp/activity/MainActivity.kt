package tv.mycujoo.mlsapp.activity

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.activity_main.*
import tv.mycujoo.domain.entity.*
import tv.mycujoo.mls.api.MLS
import tv.mycujoo.mls.api.MLSBuilder
import tv.mycujoo.mls.api.MLSConfiguration
import tv.mycujoo.mls.api.PlayerEventsListener
import tv.mycujoo.mls.core.UIEventListener
import tv.mycujoo.mls.widgets.MLSPlayerView
import tv.mycujoo.mlsapp.R


class MainActivity : AppCompatActivity() {

    private lateinit var MLS: MLS
    var isFullScreen = false

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
                .setConfiguration(MLSConfiguration())
                .build()

        MLS.getDataProvider().getEventsLiveData()
            .observe(this, Observer { eventList -> onEventListUpdated(eventList) })


        mainActivityPlayButton.setOnClickListener {
            MLS.getVideoPlayer().playVideo("EVENT_ID_HERE")
        }
        mainActivityPlayButton2.setOnClickListener {
            MLS.getDataProvider().fetchEvents(
                10,
                null,
                listOf(EventStatus.EVENT_STATUS_SCHEDULED, EventStatus.EVENT_STATUS_CANCELLED),
                OrderByEventsParam.ORDER_TITLE_ASC,
                fetchEventCallback = { eventList: List<EventEntity>, previousPageToken: String, nextPageToken: String ->
                    MLS.getVideoPlayer().playVideo(eventList.first())
                }
            )
        }

    }

    private fun onEventListUpdated(eventList: List<EventEntity>) {
        Log.i("MainActivity", "onEventListUpdated")
        eventList.firstOrNull()?.let {
            MLS.getVideoPlayer().playVideo(it)
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
        if (isFullScreen) {
            mlsPlayerView.screenMode(MLSPlayerView.ScreenMode.Landscape(MLSPlayerView.RESIZE_MODE_FILL))
        } else {
            mlsPlayerView.screenMode(MLSPlayerView.ScreenMode.Portrait(MLSPlayerView.RESIZE_MODE_FIT))
        }
    }

    private fun getSampleEventEntity(id: String): EventEntity {
        val location = Location(Physical("", "", Coordinates(0.toDouble(), 0.toDouble()), "", ""))
        return EventEntity(
            id,
            "",
            "",
            "",
            location,
            "",
            "",
            EventStatus.EVENT_STATUS_UNSPECIFIED,
            emptyList(),
            "",
            emptyList(),
            Metadata(),
            false
        )
    }


    val svgString = """
        <?xml version="1.0" encoding="UTF-8"?>
        <svg width="457px" height="69px" viewBox="0 0 457 69" version="1.1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink">
            <!-- Generator: Sketch 63.1 (92452) - https://sketch.com -->
            <title>Group 3</title>
            <desc>Created with Sketch.</desc>
            <g id="💬-Annotation-Overlays" stroke="none" stroke-width="1" fill="none" fill-rule="evenodd">
                <g id="overlay-02-copy-3" transform="translate(-28.000000, -28.000000)">
                    <g id="Group-3" transform="translate(28.000000, 28.000000)">
                        <path d="M348,0 L450.009709,0 C453.87034,-7.5670452e-15 457,3.12966 457,6.99029126 L457,62.0097087 C457,65.87034 453.87034,69 450.009709,69 L348,69 L348,69 L348,0 Z" id="Rectangle" fill="#4C4C4E"></path>
                        <path d="M6.99029126,0 L112,0 L112,0 L112,69 L6.99029126,69 C3.12966,69 -1.01853501e-14,65.87034 0,62.0097087 L0,6.99029126 C-1.36096939e-15,3.12966 3.12966,4.26190014e-15 6.99029126,0 Z" id="Rectangle-Copy-8" fill-opacity="0.9" fill="#ffff01"></path>
                        <text id="{{T1}}" font-family="Rubik-Medium, Rubik" font-size="24" font-weight="400" fill="#FFFFFF">
                            <tspan x="24.056" y="43">Brusque</tspan>
                        </text>
                        <rect id="Rectangle-Copy-10" fill-opacity="0.9" fill="#000000" x="236" y="0" width="112" height="69"></rect>
                        <rect id="Rectangle-Copy-13" fill-opacity="0.9" fill="#FFFFFF" x="112" y="0" width="124" height="69"></rect>
                        <text id="{{T2}}" font-family="Rubik-Medium, Rubik" font-size="24" font-weight="400" fill="#FFFFFF">
                            <tspan x="258.184" y="43">Manaus</tspan>
                        </text>
                        <text id="0" font-family="Rubik-Medium, Rubik" font-size="24" font-weight="400" fill="#3A3A3A">
                            <tspan x="196.184" y="42">        Home Score        </tspan>
                        </text>
                        <text id="0" font-family="Rubik-Medium, Rubik" font-size="24" font-weight="400" fill="#3A3A3A">
                            <tspan x="136.184" y="42">        Away Score      </tspan>
                        </text>
                        <text id="-" font-family="Rubik-Medium, Rubik" font-size="24" font-weight="400" fill="#3A3A3A">
                            <tspan x="168.48" y="42">-</tspan>
                        </text>
                        <text id="10:10" font-family="sounds" font-size="24" font-weight="400" fill="#FFFFFF">
                            <tspan x="372.248" y="43">        Timer    </tspan>
                        </text>
                    </g>
                </g>
            </g>
        </svg>
    """.trimIndent()
}
