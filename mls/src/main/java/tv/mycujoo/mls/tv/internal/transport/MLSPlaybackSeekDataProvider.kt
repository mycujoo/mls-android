package tv.mycujoo.mls.tv.internal.transport

import androidx.leanback.widget.PlaybackSeekDataProvider

class MLSPlaybackSeekDataProvider(
    private val interval: Long
) :
    PlaybackSeekDataProvider() {
    private var mSeekPositions: LongArray = longArrayOf()

    fun setSeekPositions(duration: Long) {
        val size = (duration / interval).toInt() + 1
        val pos = LongArray(size)
        for (i in pos.indices) {
            pos[i] = i * duration / pos.size
        }
        mSeekPositions = pos
    }

    override fun getSeekPositions(): LongArray {
        return mSeekPositions
    }


}