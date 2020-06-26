package tv.mycujoo.mls.core

import com.google.android.exoplayer2.Player.STATE_BUFFERING
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.TimeBar
import tv.mycujoo.mls.entity.msc.VideoPlayerConfig
import tv.mycujoo.mls.widgets.PlayerViewWrapper

class VideoPlayerCoordinator(exoPlayer: SimpleExoPlayer, playerViewWrapper: PlayerViewWrapper) {

    init {
        val mainEventListener = object : MainEventListener {
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                super.onPlayerStateChanged(playWhenReady, playbackState)

                if (playbackState == STATE_BUFFERING && playWhenReady) {
                    playerViewWrapper.showBuffering()
                } else {
                    playerViewWrapper.hideBuffering()
                }

                if (exoPlayer.isCurrentWindowDynamic) {
                    // live stream
                    if (exoPlayer.currentPosition + 15000L >= exoPlayer.duration) {
                        playerViewWrapper.setLiveMode(PlayerViewWrapper.LiveState.LIVE_ON_THE_EDGE)
                    } else {
                        playerViewWrapper.setLiveMode(PlayerViewWrapper.LiveState.LIVE_TRAILING)
                    }

                } else {
                    // VOD
                    playerViewWrapper.setLiveMode(PlayerViewWrapper.LiveState.VOD)
                }

            }
        }

        exoPlayer.addListener(mainEventListener)

        playerViewWrapper.getTimeBar().addListener(object : TimeBar.OnScrubListener{
            override fun onScrubMove(timeBar: TimeBar, position: Long) {
                //do nothing
            }

            override fun onScrubStart(timeBar: TimeBar, position: Long) {
                //do nothing
            }

            override fun onScrubStop(timeBar: TimeBar, position: Long, canceled: Boolean) {
                if (exoPlayer.isCurrentWindowDynamic) {
                    // live stream
                    if (exoPlayer.currentPosition + 15000L >= exoPlayer.duration) {
                        playerViewWrapper.setLiveMode(PlayerViewWrapper.LiveState.LIVE_ON_THE_EDGE)
                    } else {
                        playerViewWrapper.setLiveMode(PlayerViewWrapper.LiveState.LIVE_TRAILING)
                    }

                } else {
                    // VOD
                    playerViewWrapper.setLiveMode(PlayerViewWrapper.LiveState.VOD)
                }
            }
        })


        val videoPlayerConfig = VideoPlayerConfig(
            "#32c5ff",
            "#6236ff",
            true,
            80F,
            true,
            liveViewers = false,
            eventInfoButton = true
        )
        playerViewWrapper.config(videoPlayerConfig)


    }

}
