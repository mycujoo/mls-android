package tv.mycujoo.mls.mediator

import android.os.Handler
import android.os.Looper
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.Player.DISCONTINUITY_REASON_SEEK
import com.google.android.exoplayer2.Player.STATE_READY
import tv.mycujoo.domain.entity.ActionEntity
import tv.mycujoo.domain.entity.models.ActionType.HIDE_OVERLAY
import tv.mycujoo.domain.entity.models.ActionType.SHOW_OVERLAY
import tv.mycujoo.domain.usecase.GetActionsFromJSONUseCase
import tv.mycujoo.mls.core.ActionBuilder
import tv.mycujoo.mls.core.AnnotationListener
import tv.mycujoo.mls.core.IActionBuilder
import tv.mycujoo.mls.helper.IDownloaderClient
import tv.mycujoo.mls.manager.ViewIdentifierManager
import tv.mycujoo.mls.player.IPlayer
import tv.mycujoo.mls.widgets.PlayerViewWrapper
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit


class AnnotationMediator(
    private var playerViewWrapper: PlayerViewWrapper,
    identifierManager: ViewIdentifierManager,
    player: IPlayer,
    downloaderClient: IDownloaderClient
) : IAnnotationMediator {

    private var scheduler: ScheduledExecutorService

    /**region Fields*/
    internal var actionBuilder: IActionBuilder

    private lateinit var eventListener: Player.EventListener
    private var hasPendingSeek: Boolean = false
    /**endregion */

    /**region Initialization*/
    init {
        initEventListener(player)

        val annotationListener = AnnotationListener(playerViewWrapper, identifierManager)

        actionBuilder = ActionBuilder(
            annotationListener,
            downloaderClient,
            identifierManager
        )

        feed()

        val handler = Handler(Looper.getMainLooper())
        scheduler = Executors.newScheduledThreadPool(1)

        val exoRunnable = Runnable {
            if (player.isPlaying()) {
                val currentPosition = player.currentPosition()

                actionBuilder.setCurrentTime(currentPosition, player.isPlaying())
                actionBuilder.buildCurrentTimeRange()

                actionBuilder.processTimers()

                actionBuilder.computeVariableNameValueTillNow()

                playerViewWrapper.updateTime(currentPosition, player.duration())
            }
        }

        val scheduledRunnable = Runnable {
            handler.post(exoRunnable)
        }
        scheduler.scheduleAtFixedRate(scheduledRunnable, 1000L, 1000L, TimeUnit.MILLISECONDS)

    }

    private fun feed() {
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
    }

    private fun initEventListener(player: IPlayer) {
        eventListener = object : Player.EventListener {

            override fun onPositionDiscontinuity(reason: Int) {
                super.onPositionDiscontinuity(reason)
                val time = player.currentPosition()

                actionBuilder.setCurrentTime(time, player.isPlaying())

                if (reason == DISCONTINUITY_REASON_SEEK) {
                    hasPendingSeek = true
                }

                playerViewWrapper.updateTime(time, player.duration())
            }

            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                super.onPlayerStateChanged(playWhenReady, playbackState)

                if (playbackState == STATE_READY) {
                    playerViewWrapper.updateTime(player.currentPosition(), player.duration())
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
        player.addListener(eventListener)
    }

    override fun initPlayerView(playerViewWrapper: PlayerViewWrapper) {
        this.playerViewWrapper = playerViewWrapper
        playerViewWrapper.onSizeChangedCallback = onSizeChangedCallback
    }
    /**endregion */

    /**region Over-ridden Functions*/
    override fun release() {
        scheduler.shutdown()
    }

    override var onSizeChangedCallback = {
        actionBuilder.setCurrentTime(player.currentPosition(), player.isPlaying())
        actionBuilder.removeAll()
        actionBuilder.buildCurrentTimeRange()
        actionBuilder.buildLingerings()
    }
    /**endregion */
}