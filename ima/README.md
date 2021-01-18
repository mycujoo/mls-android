[ ![Download](https://api.bintray.com/packages/mycujoo/mls/ima/images/download.svg) ](https://bintray.com/mycujoo/mls/ima/_latestVersion)
# MLS IMA module
In order to use Google Interactive media ads support in MLS-SDK, import MLS-IMA module by adding the following to your app level build.gradle file:

        implementation 'tv.mycujoo.mls-android:ima:[MLS_IMA_LATEST_VERSION_HERE]'
            
then, instantiate MLS-IMA and provide it to MLS-Builder to build the MLS-component:

        .setIma(
            Ima(
                YOUR_AD_UNIT_HERE,      // needed: Ad unit provided by Google's IMA
                listener,               // optional: instance of ImaEventListener
                false                   // optional: for verbose logging set to true
                ))
        
Sample implementation of ImaEventListener (optional):

        var listener = object : ImaEventListener {
                            override fun onAdStarted() {}
                            override fun onAdPaused() {}
                            override fun onAdResumed() {}
                            override fun onAdCompleted() {}
                        }

Sample implementation of MLS with IMA

        MLS = MLSBuilder()
            .publicKey("YOUR_PUBLIC_KEY_HERE")
            .withActivity(this)
            .setPlayerEventsListener(playerEventsListener)
            .setUIEventListener(uiEventListener)
            .setConfiguration(mlsConfiguration) // customize MLSConfiguration
            .setIma(Ima(YOUR_AD_UNIT_HERE))
            .build()