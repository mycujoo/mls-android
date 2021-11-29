package tv.mycujoo.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.leanback.app.VideoSupportFragment
import tv.mycujoo.mcls.api.PlayerViewContract
import tv.mycujoo.mcls.databinding.FragmentMlsTvBinding

class MLSTVFragment : Fragment(), PlayerViewContract {

    lateinit var uiBinding: FragmentMlsTvBinding

    lateinit var videoSupportFragment: VideoSupportFragment
    lateinit var overlayHost: ConstraintLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        uiBinding = FragmentMlsTvBinding.inflate(layoutInflater, container, false)

        overlayHost = uiBinding.overlayHost
        videoSupportFragment = VideoSupportFragment()

        childFragmentManager
            .beginTransaction()
            .replace(uiBinding.playbackFragmentHost.id, videoSupportFragment)
            .commit()

        return uiBinding.root
    }

    override fun context(): Context = requireContext()
    override fun overlayHost(): ConstraintLayout = uiBinding.overlayHost
}