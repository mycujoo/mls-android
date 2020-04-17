package tv.mycujoo.mls.api

import android.content.Context
import android.net.Uri
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.util.Util


class MyCujooLiveService private constructor() {

    private lateinit var exoPlayer: SimpleExoPlayer
    private lateinit var context: Context

    fun getPlayer(): SimpleExoPlayer? {
        if (this::exoPlayer.isInitialized) {
            return exoPlayer
        } else {
            return null
        }
    }

    fun playView(uri: Uri) {




        var mediaSource = HlsMediaSource.Factory(DefaultHttpDataSourceFactory(Util.getUserAgent(context, "mls")))
            .createMediaSource(uri)

        // Produces DataSource instances through which media data is loaded.
        val dataSourceFactory: DataSource.Factory = DefaultDataSourceFactory(
            context,
            Util.getUserAgent(context, "mls")
        )
        // This is the MediaSource representing the media to be played.
        val videoSource: MediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(uri)
        // Prepare the player with the source.
        exoPlayer.prepare(mediaSource)
    }

    private constructor(publicKey: String, context: Context) : this() {
        this.context = context
        println(publicKey)
        exoPlayer = SimpleExoPlayer.Builder(context).build()
    }


    companion object {

        const val PUBLIC_KEY = "pk_test_123"

        fun init(publicKey: String, context: Context): MyCujooLiveService {
            if (publicKey == PUBLIC_KEY) {
                return MyCujooLiveService(publicKey, context)
            }
            throw IllegalArgumentException()
        }
    }

}