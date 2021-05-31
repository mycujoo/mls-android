package tv.mycujoo.mcls

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

/**
 * A blank activity which is used in Tests to parse SVGs with 'androidsvg'
 */
class BlankActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blank)
    }
}
