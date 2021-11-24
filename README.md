
 [ ![Download](https://api.bintray.com/packages/mycujoo/mls/tv.mycujoo.mls/images/download.svg) ](https://bintray.com/mycujoo/mls/tv.mycujoo.mls/_latestVersion)
# MLS (MyCujoo Live Services) in Android platform MLS-android

### For usage showcase please refer to Sample-app [https://github.com/mycujoo/mls-android-sample-app]

### Overview
MLS Android SDK enables apps to play videos that are hosted on MyCujoo Live Service platform while making displaying of annotations possible. MLS will handle all possible features an app needs to broadcast an event. From retrieving events list to displaying the video itself & annotations on it.


#### Init MLS SDK main component

Add dependency to SDK in app-level build.gradle file:

    implementation 'tv.mycujoo.mls:mls:MLS_LATEST_VERSION_HERE'

#### Add MLSPlayerView

In xml layout file of your activity (or fragment) add MLSPayerView. 

        <tv.mycujoo.mcls.widgets.MLSPlayerView
            android:id="@+id/mlsPlayerView"
            android:layout_width="match_parent"
            android:layout_height="match_parent" />

in order to communicate with SDK, MLS class must be instantiated. Init MLS whenever you have a reference to an Activity:

    private lateinit var MLS: MLS
        
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // code ommited for brevity
        
        // constraint MLSPlayerView here
    
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
            .setYouboraAccount("YOUBORA_ACCOUNT_CODE") // Or You can add it via the Manifest
            .setPlayerEventsListener(playerEventsListener)
            .setUIEventListener(uiEventListener)
            .setConfiguration(MLSConfiguration())
            .build()


##### Notes: 

You Can provide the public key from the AndroidManifest using this meta tag:

       <application 
           ...>
           ...
           <meta-data
                   android:name="tv.mycujoo.MLS_PUBLIC_KEY"
                   android:value="YOUR_PUBLIC_KEY_HERE" />
       </application>

And You can set your Youbora Account via the manifest

      <application
         ...>
         ...
         <meta-data
                  android:name="tv.mycujoo.MLS_YOUBORA_ACCOUNT"
                  android:value="YOUBORA_ACCOUNT_CODE_HERE" />
      </application>

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


​      

## Android TV Player

1. Implement the library

   ```groovy
   implementation 'tv.mycujoo.mls:mls:MLS_LATEST_VERSION_HERE'
   ```

   

2. Add Public Key to the Manifest using:

   ```xml
   <application 
       ...>
       ...
       <meta-data
               android:name="tv.mycujoo.MLS_PUBLIC_KEY"
               android:value="YOUR_PUBLIC_KEY_HERE" />
   </application>
   ```

3. Add the Playback Fragment in the Activity XML using:

   ```xml
   <androidx.fragment.app.FragmentContainerView
           android:id="@+id/playback_fragment"
           android:tag="playback_tag"
           android:layout_width="match_parent"
           android:layout_height="match_parent"
           android:name="tv.mycujoo.ui.PlaybackFragment" />
   ```

4. Play your event fast and easy with this code in the Activity's Kotlin Class

   ```kotlin
   fun onCreate(...) {
     (supportFragmentManager.findFragmentByTag("playback_tag") as PlaybackFragment).playEvent(
               EVENT_ID_HERE
           )
   }
   
   
   ```

### Modules
#### Cast Module (Google Cast support)
If you are interested to support Google Cast in MLS, import MLS-Cast module and provide it to builder of MLS-component.
[Here](https://github.com/mycujoo/mls-android/blob/master/cast/README.md) is the document on how to use it.
Also, [VideoActivityWithCaster](https://github.com/mycujoo/mls-android-sample-app/blob/master/app/src/main/java/tv/mycujoo/mlssampleapp/VideoActivityWithCaster.kt#L86) in [Sample-App](https://github.com/mycujoo/mls-android-sample-app) demonstrates it as an full example.

#### IMA Module (Google Interactive media ads support)
To support Google Interactive media ads in MLS, add this module to your project dependencies and provide an Ima class to MLS builder.
[Here](https://github.com/mycujoo/mls-android/blob/master/ima/README.md) is the document on how to use it.
