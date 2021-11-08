package tv.mycujoo.mcls

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import tv.mycujoo.mcls.api.MLSBuilder
import javax.inject.Inject

@AndroidEntryPoint
open class MLSActivity : AppCompatActivity() {

    @Inject
    protected lateinit var MLSBuilder: MLSBuilder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: MLS")
    }

    companion object {
        private const val TAG = "MLSActivity"
    }
}