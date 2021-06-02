package tv.mycujoo.mcls.cast.helper

import android.net.Uri
import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaMetadata
import com.google.android.gms.common.images.WebImage
import org.json.JSONObject

class MediaInfoBuilder {
    companion object {
        private const val M3U8_MIME_TYPE = "application/x-mpegURL"

        /**
         * Builds given info into MediaInfo,
         * May be used for sending info to remote media client
         * @see MediaInfo
         */
        @JvmStatic
        fun build(
            url: String,
            title: String?,
            thumbnailUrl: String,
            customData: JSONObject
        ): MediaInfo {
            val movieMetadata = MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE)
            movieMetadata.putString(MediaMetadata.KEY_TITLE, title ?: "")
            movieMetadata.addImage(WebImage((Uri.parse(thumbnailUrl))))
            return MediaInfo.Builder(url)
                .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                .setContentType(M3U8_MIME_TYPE)
                .setCustomData(customData)
                .setMetadata(movieMetadata)
                .build()
        }
    }
}