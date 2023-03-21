package tv.mycujoo

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import dagger.android.AndroidInjection
import tv.mycujoo.mcls.R

class TestActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidInjection.inject(this)
        setContentView(R.layout.activity_test)
    }
}

