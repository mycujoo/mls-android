package tv.mycujoo.mls.cordinator

import com.google.android.exoplayer2.SimpleExoPlayer

interface CoordinatorInterface {
    var onSizeChangedCallback: () -> Unit

    fun onSeekHappened(exoPlayer: SimpleExoPlayer)
}