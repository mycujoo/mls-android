package tv.mycujoo.mls.helper

import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaMetadata
import org.json.JSONObject

class MediaInfoBuilder {
    companion object {
        private const val M3U8_MIME_TYPE = "application/x-mpegURL"

        @JvmStatic
        fun build(url: String, title: String?, customData: JSONObject): MediaInfo {
            val movieMetadata = MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE)
            movieMetadata.putString(MediaMetadata.KEY_TITLE, title ?: "")
            return MediaInfo.Builder(url)
                .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                .setContentType(M3U8_MIME_TYPE)
                .setCustomData(customData)
                .setMetadata(movieMetadata)
                .build()
        }
    }
}