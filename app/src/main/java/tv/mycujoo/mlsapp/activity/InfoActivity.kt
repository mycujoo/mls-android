package tv.mycujoo.mlsapp.activity

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import tv.mycujoo.mlsapp.databinding.ActivityInfoBinding
import tv.mycujoo.mlsapp.viewmodel.InfoViewModel


class InfoActivity : AppCompatActivity() {

    lateinit var activityInfoBindings: ActivityInfoBinding

    private val infoViewModel: InfoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityInfoBindings = ActivityInfoBinding.inflate(layoutInflater)
        setContentView(activityInfoBindings.root)

        infoViewModel.eventNameLiveData.observe(this) {
            activityInfoBindings.tvEventName.text = it
        }

        activityInfoBindings.btnGetEvent.setOnClickListener {
            infoViewModel.getEvent(this, "EVENT_ID_HERE")
        }
    }
}
