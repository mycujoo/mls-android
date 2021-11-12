package tv.mycujoo.mlsapp.activity

import android.app.Activity
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import tv.mycujoo.mlsapp.R
import tv.mycujoo.mlsapp.databinding.ActivityTvMainBinding

class TvMainActivity : Activity() {
    lateinit var uiBinding: ActivityTvMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        uiBinding = ActivityTvMainBinding.inflate(layoutInflater)
        setContentView(uiBinding.root)
    }
}