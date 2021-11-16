package tv.mycujoo.mlsapp.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.leanback.app.VideoSupportFragment
import tv.mycujoo.mcls.api.MLSTVConfiguration
import tv.mycujoo.mcls.entity.msc.TVVideoPlayerConfig
import tv.mycujoo.mcls.tv.api.MLSTV
import tv.mycujoo.mcls.tv.api.MLSTvBuilder
import tv.mycujoo.mlsapp.BuildConfig

class PlaybackFragment : VideoSupportFragment() {

    lateinit var mlstv: MLSTV

    override fun onResume() {
        super.onResume()
        mlstv = MLSTvBuilder()
            .withVideoFragment(this)
            .setConfiguration(
                MLSTVConfiguration(
                    1000L,
                    TVVideoPlayerConfig(
                        primaryColor = "#ff0000", secondaryColor = "#fff000",
                        autoPlay = true,
                        showBackForwardsButtons = true,
                        showSeekBar = true,
                        showTimers = true,
                        showLiveViewers = true,
                    )
                )
            )
            .build()
        mlstv.getVideoPlayer()
            .playVideo(BuildConfig.MCLS_TEST_EVENT_ID)
    }
}