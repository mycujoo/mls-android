package tv.mycujoo.mls.cordinator

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.Player.DISCONTINUITY_REASON_SEEK
import com.google.android.exoplayer2.Player.STATE_READY
import com.google.android.exoplayer2.SimpleExoPlayer
import okhttp3.OkHttpClient
import tv.mycujoo.domain.entity.*
import tv.mycujoo.domain.entity.models.ActionType.HIDE_OVERLAY
import tv.mycujoo.domain.entity.models.ActionType.SHOW_OVERLAY
import tv.mycujoo.domain.usecase.GetActionsFromJSONUseCase
import tv.mycujoo.mls.core.ActionBuilder
import tv.mycujoo.mls.core.ActionBuilderImpl
import tv.mycujoo.mls.core.AnnotationListener
import tv.mycujoo.mls.helper.DownloaderClient
import tv.mycujoo.mls.manager.ViewIdentifierManager
import tv.mycujoo.mls.widgets.PlayerViewWrapper
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit


class Coordinator(
    private val identifierManager: ViewIdentifierManager,
    private val exoPlayer: SimpleExoPlayer,
    private val okHttpClient: OkHttpClient
) : CoordinatorInterface {

    /**region Fields*/
    internal lateinit var playerViewWrapper: PlayerViewWrapper
    internal var actionBuilder: ActionBuilder

    private lateinit var seekInterruptionEventListener: Player.EventListener
    private var hasPendingSeek: Boolean = false
    /**endregion */

    /**region Initialization*/
    init {
        initEventListener(exoPlayer)

        val annotationListener = object : AnnotationListener {

            override fun addOverlay(overlayEntity: OverlayEntity) {
                overlayEntity.isOnScreen = true
                if (overlayEntity.introTransitionSpec.animationType == AnimationType.NONE) {
                    playerViewWrapper.onNewOverlayWithNoAnimation(overlayEntity)
                } else {
                    playerViewWrapper.onNewOverlayWithAnimation(overlayEntity)
                }
            }

            override fun removeOverlay(overlayEntity: OverlayEntity) {
                Log.d("Coordinator", "removeOverlay() for overlayEntity ${overlayEntity.id}")

                overlayEntity.isOnScreen = false
                if (overlayEntity.outroTransitionSpec.animationType == AnimationType.NONE) {
                    playerViewWrapper.onOverlayRemovalWithNoAnimation(overlayEntity)
                } else {
                    playerViewWrapper.onOverlayRemovalWithAnimation(overlayEntity)
                }
            }


            override fun addOrUpdateLingeringIntroOverlay(
                overlayEntity: OverlayEntity,
                animationPosition: Long,
                isPlaying: Boolean
            ) {
                overlayEntity.isOnScreen = true
                if (identifierManager.overlayObjectIsAttached(overlayEntity.id)) {
                    playerViewWrapper.updateLingeringIntroOverlay(
                        overlayEntity,
                        animationPosition,
                        isPlaying
                    )
                } else {
                    playerViewWrapper.addLingeringIntroOverlay(
                        overlayEntity,
                        animationPosition,
                        isPlaying
                    )
                }
            }

            override fun addOrUpdateLingeringOutroOverlay(
                overlayEntity: OverlayEntity,
                animationPosition: Long,
                isPlaying: Boolean
            ) {
                overlayEntity.isOnScreen = true
                if (identifierManager.overlayObjectIsAttached(overlayEntity.id)) {
                    playerViewWrapper.updateLingeringOutroOverlay(
                        overlayEntity,
                        animationPosition,
                        isPlaying
                    )
                } else {
                    playerViewWrapper.addLingeringOutroOverlay(
                        overlayEntity,
                        animationPosition,
                        isPlaying
                    )
                }

            }

            override fun addOrUpdateLingeringMidwayOverlay(overlayEntity: OverlayEntity) {
                overlayEntity.isOnScreen = true
                if (identifierManager.overlayObjectIsAttached(overlayEntity.id)) {
                    playerViewWrapper.updateLingeringMidwayOverlay(overlayEntity)
                } else {
                    playerViewWrapper.addLingeringMidwayOverlay(overlayEntity)
                }
            }

            override fun removeLingeringOverlay(overlayEntity: OverlayEntity) {
                overlayEntity.isOnScreen = false
                playerViewWrapper.removeLingeringOverlay(overlayEntity)
            }

            override fun clearScreen(idList: List<String>) {
                playerViewWrapper.clearScreen(idList)
            }
        }


        actionBuilder = ActionBuilderImpl(
            annotationListener,
            DownloaderClient(okHttpClient),
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

        actionBuilder.addOverlayObjects(actionsList.filter { it.type == SHOW_OVERLAY }
            .map { createOverlayObject(it) })

        actionBuilder.addSetVariableEntities(GetActionsFromJSONUseCase.mappedActionCollections().setVariableEntityList)
        actionBuilder.addIncrementVariableEntities(GetActionsFromJSONUseCase.mappedActionCollections().incrementVariableEntityList)

        actionBuilder.addActionCollections(GetActionsFromJSONUseCase.mappedActionCollections())


        val handler = Handler(Looper.getMainLooper())
        val scheduler: ScheduledExecutorService = Executors.newScheduledThreadPool(1)

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
    }

    private fun createOverlayObject(actionEntity: ActionEntity): OverlayObject {

        val svgData = SvgData(
            actionEntity.svgUrl,
            actionEntity.svgInputStream,
            null
        )
        val viewSpec = ViewSpec(actionEntity.position, actionEntity.size)
        val introTransitionSpec = TransitionSpec(
            actionEntity.offset,
            actionEntity.introAnimationType,
            actionEntity.introAnimationDuration
        )

        val outroTransitionSpec: TransitionSpec =
            if (actionEntity.duration != null && actionEntity.duration!! > 0L) {
                TransitionSpec(
                    actionEntity.offset + actionEntity.duration!!,
                    if (actionEntity.outroAnimationType == AnimationType.UNSPECIFIED) {
                        AnimationType.NONE
                    } else {
                        actionEntity.outroAnimationType
                    },
                    actionEntity.outroAnimationDuration
                )
            } else {
                TransitionSpec(
                    -1L,
                    AnimationType.UNSPECIFIED,
                    -1L
                )
            }



        return OverlayObject(
            actionEntity.id,
            svgData,
            viewSpec,
            introTransitionSpec,
            outroTransitionSpec,
            actionEntity.variablePlaceHolders
        )
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