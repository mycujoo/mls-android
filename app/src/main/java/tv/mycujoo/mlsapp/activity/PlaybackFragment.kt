package tv.mycujoo.mlsapp.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.leanback.app.VideoSupportFragment
import tv.mycujoo.mcls.tv.api.MLSTV
import tv.mycujoo.mcls.tv.api.MLSTvBuilder
import tv.mycujoo.mlsapp.BuildConfig

class PlaybackFragment : VideoSupportFragment() {

    lateinit var mlstv: MLSTV

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        mlstv = MLSTvBuilder()
            .withVideoFragment(this)
            .build()
        mlstv.getVideoPlayer().playVideo(BuildConfig.MCLS_TEST_EVENT_ID)
    }
}