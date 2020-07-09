package tv.mycujoo.mls.cordinator

import android.os.Handler
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import okhttp3.OkHttpClient
import tv.mycujoo.domain.entity.ActionEntity
import tv.mycujoo.domain.entity.models.ActionType.*
import tv.mycujoo.domain.mapper.HideOverlayMapper
import tv.mycujoo.domain.mapper.ShowOverlayMapper
import tv.mycujoo.domain.usecase.GetAnnotationFromJSONUseCase
import tv.mycujoo.mls.core.AnnotationBuilder
import tv.mycujoo.mls.core.AnnotationBuilderImpl
import tv.mycujoo.mls.core.AnnotationListener
import tv.mycujoo.mls.helper.ActionEntityFactory
import tv.mycujoo.mls.network.Api
import tv.mycujoo.mls.widgets.PlayerViewWrapper

class Coordinator(
    private val api: Api
) : CoordinatorInterface {

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
            override fun onNewActionAvailable(actionEntity: ActionEntity) {

                when (actionEntity.type) {
                    UNKNOWN -> {
                        // do nothing
                    }
                    SHOW_OVERLAY -> {
                        playerViewWrapper.showOverlay(ShowOverlayMapper.mapToEntity(actionEntity))
                    }
                    HIDE_OVERLAY -> {
                        playerViewWrapper.hideOverlay(HideOverlayMapper.mapToEntity(actionEntity))
                    }
                }
            }

            override fun onLingeringActionAvailable(actionEntity: ActionEntity) {
                when (actionEntity.type) {
                    UNKNOWN,
                    HIDE_OVERLAY -> {
                        // do nothing, should not happen
                    }
                    SHOW_OVERLAY -> {
                        playerViewWrapper.showLingeringOverlay(
                            ShowOverlayMapper.mapToEntity(
                                actionEntity
                            )
                        )
                    }
                }

            }

            override fun onLingeringIntroAnimationAvailable(
                actionEntity: ActionEntity,
                animationPosition: Long,
                isPlaying: Boolean
            ) {
                playerViewWrapper.onLingeringIntroAnimationAvailable(
                    ShowOverlayMapper.mapToEntity(
                        actionEntity
                    ), animationPosition, isPlaying
                )
            }

            override fun onLingeringOutroAnimationAvailableFromSeparateAction(
                relatedShowActionEntity: ActionEntity,
                hideActionEntity: ActionEntity,
                animationPosition: Long,
                isPlaying: Boolean
            ) {
                playerViewWrapper.onLingeringOutroAnimationAvailable(
                    ShowOverlayMapper.mapToEntity(
                        relatedShowActionEntity
                    ),
                    HideOverlayMapper.mapToEntity(hideActionEntity),
                    animationPosition,
                    isPlaying
                )
            }

            override fun onLingeringOutroAnimationAvailableFromSameAction(
                relatedShowActionEntity: ActionEntity,
                animationPosition: Long,
                isPlaying: Boolean
            ) {
                playerViewWrapper.onLingeringOutroAnimationAvailableFromSameCommand(
                    ShowOverlayMapper.mapToEntity(
                        relatedShowActionEntity
                    ),
                    animationPosition,
                    isPlaying
                )
            }

            override fun onNewOutroAnimationAvailableSeparateAction(
                relatedActionEntity: ActionEntity,
                hideActionEntity: ActionEntity
            ) {
                playerViewWrapper.onNewOutroAnimationAvailable(
                    relatedActionEntity,
                    hideActionEntity
                )
            }

            override fun onNewOutroAnimationAvailableSameCommand(actionEntity: ActionEntity) {
                playerViewWrapper.onNewOutroAnimationAvailable(
                    actionEntity
                )
            }

            override fun clearScreen(customIdList: List<String>) {
                playerViewWrapper.clearScreen(customIdList)
            }
        }


        annotationBuilder = AnnotationBuilderImpl(annotationListener, okHttpClient)
        annotationBuilder.buildPendingAnnotationsForCurrentTime()


        GetAnnotationFromJSONUseCase.mappedResult().forEach {
            val actionList = it.actions.map { newActionEntity ->
                ActionEntityFactory.create(newActionEntity)
            }

            actionList.filter { it.type == SHOW_OVERLAY }
                .let { showActions -> annotationBuilder.addPendingShowActions(showActions) }
            actionList.filter { it.type == HIDE_OVERLAY }
                .let { hideActions -> annotationBuilder.addPendingHideActions(hideActions) }
        }

        val runnable = object : Runnable {
            override fun run() {
                annotationBuilder.setCurrentTime(exoPlayer.currentPosition, exoPlayer.isPlaying)
                annotationBuilder.buildPendingAnnotationsForCurrentTime()
                annotationBuilder.buildPendingOutroAnimations()
                handler.postDelayed(this, 1000L)
            }
        }

        handler.postDelayed(runnable, 1000L)
    }

    private fun initEventListener(exoPlayer: SimpleExoPlayer) {
        seekInterruptionEventListener = object : Player.EventListener {

            override fun onPositionDiscontinuity(reason: Int) {
                super.onPositionDiscontinuity(reason)
                annotationBuilder.setCurrentTime(exoPlayer.currentPosition, exoPlayer.isPlaying)

                annotationBuilder.buildRemovalAnnotations()
//        annotationBuilder.buildRemovalAnnotationsUpToCurrentTime()

                annotationBuilder.buildLingeringAnnotationsUpToCurrentTime()
                annotationBuilder.buildPendingAnnotationsForCurrentTime()


                annotationBuilder.buildLingeringIntroAnimations(exoPlayer.isPlaying)
                annotationBuilder.buildLingeringOutroAnimations(exoPlayer.isPlaying)
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