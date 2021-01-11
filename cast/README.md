[ ![Download](https://api.bintray.com/packages/mycujoo/mls/cast/images/download.svg) ](https://bintray.com/mycujoo/mls/cast/_latestVersion)
# MLS Cast module
In order to use Google Cast support in MLS-SDK, import MLS-Cast module by adding the following to your app level build.gradle file:

        implementation 'tv.mycujoo.mls-android:cast:[MLS_CASTER_LATEST_VERSION_HERE]'
        
Inherit MLSCastOptionsProviderAbstract class in your app and provide it's class path in app Manifest file as below:

        <meta-data
            android:name="com.google.android.gms.cast.framework.OPTIONS_PROVIDER_CLASS_NAME"
            android:value="[CLASS_PATH_HERE]" />
            
then, instantiate MLS-Cast and provide it while building MLS-component:

        .setCaster(
            Cast(
                miniControllerViewStub, // Optional: a ViewStub to host Cast mini-controller
                mediaRouteButton        // Optional: a MediaRouteButton component which MLS-Cast will setup. No need to call setup on user side.
                ))
        
For example:

        MLS = MLSBuilder().publicKey("YOUR_PUBLIC_KEY_HERE")
            .withActivity(this)
            .setPlayerEventsListener(playerEventsListener)
            .setUIEventListener(uiEventListener)
            .setConfiguration(mlsConfiguration) // customize MLSConfiguration by providing
            .setCaster(Caster(miniControllerViewStub))
            .build()