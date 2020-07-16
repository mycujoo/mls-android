package tv.mycujoo.mls.cordinator

import android.os.Handler
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.Player.DISCONTINUITY_REASON_SEEK
import com.google.android.exoplayer2.Player.STATE_READY
import com.google.android.exoplayer2.SimpleExoPlayer
import okhttp3.OkHttpClient
import tv.mycujoo.domain.entity.*
import tv.mycujoo.domain.entity.models.ActionType.HIDE_OVERLAY
import tv.mycujoo.domain.entity.models.ActionType.SHOW_OVERLAY
import tv.mycujoo.domain.usecase.GetAnnotationFromJSONUseCase
import tv.mycujoo.mls.core.AnnotationBuilder
import tv.mycujoo.mls.core.AnnotationBuilderImpl
import tv.mycujoo.mls.core.AnnotationListener
import tv.mycujoo.mls.helper.ActionEntityFactory
import tv.mycujoo.mls.manager.ViewIdentifierManager
import tv.mycujoo.mls.network.Api
import tv.mycujoo.mls.widgets.PlayerViewWrapper

class Coordinator(
    private val identifierManager: ViewIdentifierManager,
    private val api: Api
) : CoordinatorInterface {

    private var hasPendingSeek: Boolean = false

    /**region Fields*/
    internal lateinit var playerViewWrapper: PlayerViewWrapper
    internal lateinit var annotationBuilder: AnnotationBuilder
    internal lateinit var seekInterruptionEventListener: Player.EventListener
    /**endregion */

    /**region Initialization*/
    fun initialize(
        exoPlayer: SimpleExoPlayer,
        handler: Handler,
        okHttpClient: OkHttpClient
    ) {
        initEventListener(exoPlayer)

        val annotationListener = object : AnnotationListener {

            // re-write
            override fun onNewOverlay(overlayObject: OverlayObject) {
                if (overlayObject.introTransitionSpec.animationType == AnimationType.NONE) {
                    playerViewWrapper.onNewOverlayWithNoAnimation(overlayObject)
                } else {
                    playerViewWrapper.onNewOverlayWithAnimation(overlayObject)
                }
            }

            override fun onRemovalOverlay(overlayObject: OverlayObject) {
                if (overlayObject.outroTransitionSpec.animationType == AnimationType.NONE) {
                    playerViewWrapper.onOverlayRemovalWithNoAnimation(overlayObject)
                } else {
                    playerViewWrapper.onOverlayRemovalWithAnimation(overlayObject)
                }
            }

            override fun clearScreen(idList: List<String>) {
                playerViewWrapper.clearScreen(idList)
            }


            override fun onLingeringIntroOverlay(
                overlayObject: OverlayObject,
                animationPosition: Long,
                isPlaying: Boolean
            ) {
                playerViewWrapper.onLingeringIntroAnimationOverlay(
                    overlayObject,
                    animationPosition,
                    isPlaying
                )
            }

            override fun onLingeringOutroOverlay(
                overlayObject: OverlayObject,
                animationPosition: Long,
                isPlaying: Boolean
            ) {
                playerViewWrapper.onLingeringOutroAnimationOverlay(
                    overlayObject,
                    animationPosition,
                    isPlaying
                )
            }

            override fun onLingeringOverlay(overlayObject: OverlayObject) {
                playerViewWrapper.onNewOverlayWithNoAnimation(overlayObject)
            }
        }


        annotationBuilder = AnnotationBuilderImpl(
            annotationListener,
            okHttpClient,
            identifierManager
        )


        val actionsList = ArrayList<ActionEntity>()

        GetAnnotationFromJSONUseCase.mappedResult().forEach { newAnnotationEntity ->
            actionsList.addAll(newAnnotationEntity.actions.map { newActionEntity ->
                ActionEntityFactory.create(newActionEntity)
            })
        }

        actionsList.filter { it.type == HIDE_OVERLAY }
            .forEach { hideAction ->
                actionsList.firstOrNull { showAction -> hideAction.customId == showAction.customId && showAction.type == SHOW_OVERLAY }
                    ?.let {
                        it.outroAnimationType = hideAction.outroAnimationType
                        it.outroAnimationDuration = hideAction.outroAnimationDuration
                        it.duration = hideAction.offset
                    }
            }

        annotationBuilder.addOverlayObjects(actionsList.filter { it.type == SHOW_OVERLAY }
            .map { createOverlayObject(it) })

        val runnable = object : Runnable {
            override fun run() {
                annotationBuilder.setCurrentTime(exoPlayer.currentPosition, exoPlayer.isPlaying)
                annotationBuilder.buildCurrentTimeRange()
                handler.postDelayed(this, 200L)
            }
        }

        handler.postDelayed(runnable, 200L)
    }

    private fun createOverlayObject(actionEntity: ActionEntity): OverlayObject {

        val svgData = SvgData(
            actionEntity.svgUrl,
            actionEntity.svgInputStream
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
            outroTransitionSpec
        )
    }

    private fun initEventListener(exoPlayer: SimpleExoPlayer) {
        seekInterruptionEventListener = object : Player.EventListener {

            override fun onPositionDiscontinuity(reason: Int) {
                super.onPositionDiscontinuity(reason)
                annotationBuilder.setCurrentTime(exoPlayer.currentPosition, exoPlayer.isPlaying)
                if (reason == DISCONTINUITY_REASON_SEEK) {
                    hasPendingSeek = true
                }
            }

            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                super.onPlayerStateChanged(playWhenReady, playbackState)
                if (playbackState == STATE_READY && hasPendingSeek) {
                    hasPendingSeek = false
                    annotationBuilder.removeAll()
                    annotationBuilder.buildLingerings()
                }
            }
        }
        exoPlayer.addListener(seekInterruptionEventListener)
    }
    /**endregion */

    /**region Over-ridden Functions*/
    override fun onSeekHappened(exoPlayer: SimpleExoPlayer) {
        // remove
    }
    /**endregion */
}