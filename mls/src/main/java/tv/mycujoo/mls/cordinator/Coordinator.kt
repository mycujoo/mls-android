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
import tv.mycujoo.domain.usecase.GetActionsFromJSONUseCase
import tv.mycujoo.mls.core.ActionBuilder
import tv.mycujoo.mls.core.ActionBuilderImpl
import tv.mycujoo.mls.core.AnnotationListener
import tv.mycujoo.mls.helper.ActionEntityFactory
import tv.mycujoo.mls.manager.ViewIdentifierManager
import tv.mycujoo.mls.widgets.PlayerViewWrapper

class Coordinator(
    private val identifierManager: ViewIdentifierManager,
    private val exoPlayer: SimpleExoPlayer,
    private val handler: Handler,
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

            override fun updateLingeringOverlay(
                overlayObject: OverlayObject,
                animationPosition: Long,
                isPlaying: Boolean
            ) {
                playerViewWrapper.updateLingeringOverlay(
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

            override fun applySetVariable(setVariableEntity: SetVariableEntity) {
                identifierManager.applySetVariable(setVariableEntity.variable)
            }
        }


        actionBuilder = ActionBuilderImpl(
            annotationListener,
            okHttpClient,
            identifierManager
        )


        val actionsList = ArrayList<ActionEntity>()

        GetActionsFromJSONUseCase.mappedResult().forEach { newActionEntity ->
            actionsList.add(
                ActionEntityFactory.create(newActionEntity)
            )
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

        actionBuilder.addOverlayObjects(actionsList.filter { it.type == SHOW_OVERLAY }
            .map { createOverlayObject(it) })

        actionBuilder.addSetVariableEntities(GetActionsFromJSONUseCase.mappedSetVariables())


        val runnable = object : Runnable {
            override fun run() {
                actionBuilder.setCurrentTime(exoPlayer.currentPosition, exoPlayer.isPlaying)
                actionBuilder.buildCurrentTimeRange()
//                actionBuilder.buildSetVariables()
                handler.postDelayed(this, 200L)
            }
        }

        handler.postDelayed(runnable, 200L)

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
                actionBuilder.setCurrentTime(exoPlayer.currentPosition, exoPlayer.isPlaying)
                if (reason == DISCONTINUITY_REASON_SEEK) {
                    hasPendingSeek = true
                }
            }

            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                super.onPlayerStateChanged(playWhenReady, playbackState)
                if (playbackState == STATE_READY && hasPendingSeek) {
                    hasPendingSeek = false
//                    actionBuilder.removeAll()
                    actionBuilder.removeLeftOvers()
                    actionBuilder.buildCurrentTimeRange()

                    actionBuilder.buildLingerings()
                    actionBuilder.buildSetVariables()


                }
            }
        }
        exoPlayer.addListener(seekInterruptionEventListener)
    }
    /**endregion */

    /**region Over-ridden Functions*/
    override var onSizeChangedCallback = {
        actionBuilder.setCurrentTime(exoPlayer.currentPosition, exoPlayer.isPlaying)
        actionBuilder.removeAll()
        actionBuilder.buildLingerings()
    }
    /**endregion */
}