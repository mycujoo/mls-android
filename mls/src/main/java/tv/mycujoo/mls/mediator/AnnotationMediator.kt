package tv.mycujoo.mls.mediator

import android.os.Handler
import android.os.Looper
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.Player.DISCONTINUITY_REASON_SEEK
import com.google.android.exoplayer2.Player.STATE_READY
import com.google.android.exoplayer2.SimpleExoPlayer
import tv.mycujoo.domain.entity.ActionEntity
import tv.mycujoo.domain.entity.models.ActionType.HIDE_OVERLAY
import tv.mycujoo.domain.entity.models.ActionType.SHOW_OVERLAY
import tv.mycujoo.domain.usecase.GetActionsFromJSONUseCase
import tv.mycujoo.mls.core.ActionBuilder
import tv.mycujoo.mls.core.AnnotationListener
import tv.mycujoo.mls.core.IActionBuilder
import tv.mycujoo.mls.helper.DownloaderClient
import tv.mycujoo.mls.manager.ViewIdentifierManager
import tv.mycujoo.mls.widgets.PlayerViewWrapper
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit


class AnnotationMediator(
    private var playerViewWrapper: PlayerViewWrapper,
    identifierManager: ViewIdentifierManager,
    private val exoPlayer: SimpleExoPlayer,
    downloaderClient: DownloaderClient
) : IAnnotationMediator {

    private var scheduler: ScheduledExecutorService

    /**region Fields*/
    internal var actionBuilder: IActionBuilder

    private lateinit var seekInterruptionEventListener: Player.EventListener
    private var hasPendingSeek: Boolean = false
    /**endregion */

    /**region Initialization*/
    init {
        initEventListener(exoPlayer)

        val annotationListener = AnnotationListener(playerViewWrapper, identifierManager)

        actionBuilder = ActionBuilder(
            annotationListener,
            downloaderClient,
            identifierManager
        )


        val actionsList = ArrayList<ActionEntity>()

        actionsList.addAll(GetActionsFromJSONUseCase.mappedActionCollections().actionEntityList)

        actionsList.filter { it.type == HIDE_OVERLAY }
            .forEach { hideAction ->
                actionsList.firstOrNull { showAction -> hideAction.customId == showAction.customId && showAction.type == SHOW_OVERLAY }
                    ?.let {
                        it.outroAnimationType = hideAction.outroAnimationType
                        it.outroAnimationDuration = hideAction.outroAnimationDuration
                        it.duration = hideAction.offset
                    }
            }

        actionBuilder.addOverlayBlueprints(actionsList.filter { it.type == SHOW_OVERLAY }
            .map { it.toOverlayBlueprint() })

        actionBuilder.addSetVariableEntities(GetActionsFromJSONUseCase.mappedActionCollections().setVariableEntityList)
        actionBuilder.addIncrementVariableEntities(GetActionsFromJSONUseCase.mappedActionCollections().incrementVariableEntityList)

        actionBuilder.addActionCollections(GetActionsFromJSONUseCase.mappedActionCollections())


        val handler = Handler(Looper.getMainLooper())
        scheduler = Executors.newScheduledThreadPool(1)

        val exoRunnable = Runnable {
            if (exoPlayer.isPlaying) {
                val currentPosition = exoPlayer.currentPosition

                actionBuilder.setCurrentTime(currentPosition, exoPlayer.isPlaying)
                actionBuilder.buildCurrentTimeRange()

                actionBuilder.processTimers()

                actionBuilder.computeVariableNameValueTillNow()

                playerViewWrapper.updateTime(currentPosition, exoPlayer.duration)
            }
        }

        val scheduledRunnable = Runnable {
            handler.post(exoRunnable)
        }
        scheduler.scheduleAtFixedRate(scheduledRunnable, 1000L, 1000L, TimeUnit.MILLISECONDS)

    }

    override fun initPlayerView(playerViewWrapper: PlayerViewWrapper) {
        this.playerViewWrapper = playerViewWrapper
        playerViewWrapper.onSizeChangedCallback = onSizeChangedCallback
    }

    override fun release() {
        scheduler.shutdown()
    }

    private fun initEventListener(exoPlayer: SimpleExoPlayer) {
        seekInterruptionEventListener = object : Player.EventListener {

            override fun onPositionDiscontinuity(reason: Int) {
                super.onPositionDiscontinuity(reason)
                val time = exoPlayer.currentPosition

                actionBuilder.setCurrentTime(time, exoPlayer.isPlaying)

                if (reason == DISCONTINUITY_REASON_SEEK) {
                    hasPendingSeek = true
                }

                playerViewWrapper.updateTime(time, exoPlayer.duration)
            }

            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                super.onPlayerStateChanged(playWhenReady, playbackState)

                if (playbackState == STATE_READY) {
                    playerViewWrapper.updateTime(exoPlayer.currentPosition, exoPlayer.duration)
                }

                if (playbackState == STATE_READY && hasPendingSeek) {
                    hasPendingSeek = false

                    actionBuilder.buildLingerings()

                    actionBuilder.buildCurrentTimeRange()

                    actionBuilder.computeVariableNameValueTillNow()

                    actionBuilder.processTimers()
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                super.onIsPlayingChanged(isPlaying)

                actionBuilder.processTimers()
            }
        }
        exoPlayer.addListener(seekInterruptionEventListener)
    }
    /**endregion */

    /**region Over-ridden Functions*/
    override var onSizeChangedCallback = {
        actionBuilder.setCurrentTime(exoPlayer.currentPosition, exoPlayer.isPlaying)
        actionBuilder.removeAll()
        actionBuilder.buildCurrentTimeRange()
        actionBuilder.buildLingerings()
    }
    /**endregion */
}