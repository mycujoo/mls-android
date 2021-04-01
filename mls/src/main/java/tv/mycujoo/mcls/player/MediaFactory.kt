package tv.mycujoo.mcls.player

import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource

class MediaFactory(
    val defaultMediaSourceFactory: DefaultMediaSourceFactory,
    private val hlsMediaFactory: HlsMediaSource.Factory,
    private val mediaItemBuilder: MediaItem.Builder
) {
    fun createHlsMediaSource(mediaItem: MediaItem): MediaSource {
        return hlsMediaFactory.createMediaSource(mediaItem)
    }

    fun createMediaItem(uri: String): MediaItem {
        return mediaItemBuilder.setUri(uri).build()
    }
}