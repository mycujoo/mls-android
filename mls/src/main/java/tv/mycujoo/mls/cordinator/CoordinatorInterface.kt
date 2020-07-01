package tv.mycujoo.mls.cordinator

import com.google.android.exoplayer2.SimpleExoPlayer

interface CoordinatorInterface {
    fun onSeekHappened(exoplayer: SimpleExoPlayer)
}