package tv.mycujoo.mlsapp.activity

import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import tv.mycujoo.mlsapp.databinding.ActivityTvMainBinding

class TvMainActivity : AppCompatActivity() {
    lateinit var uiBinding: ActivityTvMainBinding

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        uiBinding = ActivityTvMainBinding.inflate(layoutInflater)
        setContentView(uiBinding.root)
    }
}