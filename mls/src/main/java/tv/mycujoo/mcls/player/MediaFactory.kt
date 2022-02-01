package tv.mycujoo.mcls.player

import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.extractor.ts.DefaultTsPayloadReaderFactory
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.hls.DefaultHlsExtractorFactory
import com.google.android.exoplayer2.source.hls.HlsMediaSource

class MediaFactory(
    val defaultMediaSourceFactory: DefaultMediaSourceFactory,
    private val hlsMediaFactory: HlsMediaSource.Factory,
    private val mediaItemBuilder: MediaItem.Builder
) {
    fun createHlsMediaSource(mediaItem: MediaItem): MediaSource {
        val hlsExtractorFactory = DefaultHlsExtractorFactory(
            DefaultTsPayloadReaderFactory.FLAG_ALLOW_NON_IDR_KEYFRAMES,
            true
        )
        return hlsMediaFactory
            .setExtractorFactory(hlsExtractorFactory)
            .createMediaSource(mediaItem)
    }

    fun createMediaItem(uri: String): MediaItem {
        return mediaItemBuilder.setUri(uri).build()
    }
}