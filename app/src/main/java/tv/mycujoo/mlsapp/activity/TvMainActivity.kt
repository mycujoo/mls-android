package tv.mycujoo.mlsapp.activity

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import tv.mycujoo.mlsapp.BuildConfig
import tv.mycujoo.mlsapp.databinding.ActivityTvMainBinding
import tv.mycujoo.ui.PlaybackFragment

class TvMainActivity : FragmentActivity() {
    lateinit var uiBinding: ActivityTvMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        uiBinding = ActivityTvMainBinding.inflate(layoutInflater)
        setContentView(uiBinding.root)


        (supportFragmentManager.findFragmentByTag("playback_tag") as PlaybackFragment).playEvent(
            BuildConfig.MCLS_TEST_EVENT_ID
        )
    }
}