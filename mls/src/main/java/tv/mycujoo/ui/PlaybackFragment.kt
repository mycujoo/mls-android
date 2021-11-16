package tv.mycujoo.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.leanback.app.VideoSupportFragment
import androidx.lifecycle.MutableLiveData
import tv.mycujoo.mcls.api.MLSTVConfiguration
import tv.mycujoo.mcls.entity.msc.TVVideoPlayerConfig
import tv.mycujoo.mcls.tv.api.MLSTV
import tv.mycujoo.mcls.tv.api.MLSTvBuilder

/**
 *
 * This Class Needs Refactoring, the event life cycle doesn't seem good
 * And There should be more control over the Config
 * And there should be a factory to inject with support fragment manager
 *
 */
class PlaybackFragment : VideoSupportFragment() {

    lateinit var mlstv: MLSTV

    lateinit var eventId: String
    val ready = MutableLiveData(false)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        ready.observe(viewLifecycleOwner) {
            if (it) mlstv.getVideoPlayer().playVideo(eventId)
        }
        return super.onCreateView(inflater, container, savedInstanceState)
    }

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
        ready.postValue(true)
    }

    fun playEvent(eventId: String) {
        this.eventId = eventId
    }
}
