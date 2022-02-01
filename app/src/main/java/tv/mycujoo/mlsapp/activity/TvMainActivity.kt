package tv.mycujoo.mlsapp.activity

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import tv.mycujoo.mcls.analytic.VideoAnalyticsCustomData
import tv.mycujoo.mcls.api.MLSTVConfiguration
import tv.mycujoo.mcls.entity.msc.TVVideoPlayerConfig
import tv.mycujoo.mcls.tv.api.MLSTV
import tv.mycujoo.mcls.tv.api.MLSTvBuilder
import tv.mycujoo.mlsapp.databinding.ActivityTvMainBinding
import tv.mycujoo.ui.MLSTVFragment

class TvMainActivity : FragmentActivity() {
    lateinit var uiBinding: ActivityTvMainBinding

    lateinit var mMLSTV: MLSTV
    private val videoFragment = MLSTVFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        uiBinding = ActivityTvMainBinding.inflate(layoutInflater)
        setContentView(uiBinding.root)

        mMLSTV = MLSTvBuilder()
            .withContext(this)
            .withMLSTvFragment(videoFragment)
            .publicKey("YOUR_PUBLIC_KEY_HERE")
            .identityToken("IDENTITY_TOKEN_HERE")
            .withVideoAnalyticsCustomData(
                VideoAnalyticsCustomData(
                    contentCustomDimension7 = "CUSTOM_DIMENSION_HERE"
                )
            )
            .setConfiguration(
                MLSTVConfiguration(
                    1000L,
                    TVVideoPlayerConfig(
                        primaryColor = "#ffffff", secondaryColor = "#fff000",
                        autoPlay = true,
                        showBackForwardsButtons = true,
                        showSeekBar = true,
                        showTimers = true,
                        showLiveViewers = true,
                    )
                )
            )
            .build()

        mMLSTV.getVideoPlayer().playVideo("EVENT_ID")
    }
}