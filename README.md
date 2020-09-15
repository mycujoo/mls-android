
[![](https://jitpack.io/v/mycujoo/mls-android.svg)](https://jitpack.io/#mycujoo/mls-android)
# MLS (MyCujoo Live Services) in Android platform MLS-android

### For usage showcase please refer to Sample-app [https://github.com/mycujoo/mls-android-sample-app]

### Overview
MLS Android SDK enables apps to play videos that are hosted on MyCujoo Live Service platform while making displaying of annotations possible. MLS will handle all possible features an app needs to broadcast an event. From retrieving events list to displaying the video itself & annotations on it.


#### Init MLS SDK main component

Add dependency to SDK in app-level build.gradle file:

    implementation 'tv.mycujoo.mls:mls:LATEST_VERSION_HERE'

in order to communicate with SDK, MLS class must be instantiated. Init MLS whenever you have a reference to an Activity:

    private lateinit var MLS: MLS
        
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // code ommited for brevity

        val playerEventsListener = object : PlayerEventsListener {
            override fun onIsPlayingChanged(playing: Boolean) {
            }
            override fun onPlayerStateChanged(playbackState: Int) {
            }
        }

        val uiEventListener = object : UIEventListener {
            override fun onFullScreenButtonClicked(fullScreen: Boolean) {
            }
        }

        // create MLS component
        MLS = MLSBuilder().publicKey("YOUR_PUBLIC_KEY_HERE")
            .withActivity(this)
            .setPlayerEventsListener(playerEventsListener)
            .setUIEventListener(uiEventListener)
            .setConfiguration(MLSConfiguration())
            .build()
      

#### Attach & detach PlayerView

        override fun onStart() {
            super.onStart()
            MLS.onStart(playerView)
        }
    
        override fun onResume() {
            super.onResume()
            MLS.onResume(playerView)
        }
    
        override fun onPause() {
            super.onPause()
            MLS.onPause()
        }
    
        override fun onStop() {
            super.onStop()
            MLS.onStop()
        }
#### Adopt to device configuration change [**Optional**]

        override fun onConfigurationChanged(newConfig: Configuration) {
            super.onConfigurationChanged(newConfig)
            // change view display mode
            if (isFullScreen) {
                playerView.screenMode(MLSPlayerView.ScreenMode.Landscape(MLSPlayerView.RESIZE_MODE_FILL))
            } else {
                playerView.screenMode(MLSPlayerView.ScreenMode.Portrait(MLSPlayerView.RESIZE_MODE_FIT))
            }
        }
#### Get Events
        // use Data-Provider to fetch events
        val dataProvider = MLS.getDataProvider()
        dataProvider.fetchEvents(
            10,
            fetchEventCallback = { eventList: List<EventEntity>, previousPageToken: String, nextPageToken: String ->
                MLS.getVideoPlayer().playVideo(eventList.first())
            })


#### Play video

        // use VideoPlayer to play video
        val videoPlayer = MLS.getVideoPlayer()
        videoPlayer.playVideo("EVENT_ID_HERE") // or event object itself




