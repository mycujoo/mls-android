[ ![Download](https://api.bintray.com/packages/mycujoo/mls/tv.mycujoo.mls-caster/images/download.svg) ](https://bintray.com/mycujoo/mls/tv.mycujoo.mls-caster/_latestVersion)
# MLS Caster module
In order to use Google Cast support in MLS-SDK, import Caster module by adding the following to your app level build.gradle file:

        implementation 'tv.mycujoo.mls-android:mls:MLS_CASTER_LATEST_VERSION_HERE'
then, instantiate Caster and provide it while building MLS-component:

        .setCaster(Caster(mainActivity_miniControllerPlaceHolder))
        
For example:

        MLS = MLSBuilder().publicKey("YOUR_PUBLIC_KEY_HERE")
            .withActivity(this)
            .setPlayerEventsListener(playerEventsListener)
            .setUIEventListener(uiEventListener)
            .setConfiguration(mlsConfiguration) // customize MLSConfiguration by providing
            .setCaster(Caster(miniControllerPlaceHolder))
            .build()

In this example mainActivity_miniControllerPlaceHolder is a ViewStub where you want the Cast Mini-controller to be inflated.
Don't forget to use 'true' for showCastButton in VideoPlayer configuration.

        // Customize configuration, or use default values
        val videoPlayerConfig = VideoPlayerConfig(
            primaryColor = "#FFFF00",
            secondaryColor = "#32CD32",
            autoPlay = false,
            enableControls = true,
            showPlayPauseButtons = true,
            showBackForwardsButtons = true,
            showSeekBar = true,
            showTimers = true,
            showFullScreenButton = false,
            showLiveViewers = true,
            showEventInfoButton = true,
            showCastButton = true // IMPORTANT!
        )
        val mlsConfiguration =
            MLSConfiguration(seekTolerance = 1000L, videoPlayerConfig = videoPlayerConfig)