package tv.mycujoo.mlsapp.activity

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import tv.mycujoo.mlsapp.databinding.ActivityTvMainBinding

class TvMainActivity : FragmentActivity() {
    lateinit var uiBinding: ActivityTvMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        uiBinding = ActivityTvMainBinding.inflate(layoutInflater)
        setContentView(uiBinding.root)

    }
}