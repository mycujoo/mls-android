package tv.mycujoo.mls.core

import tv.mycujoo.domain.entity.ActionEntity

abstract class AnnotationBuilder {
    /**
     * adds ActionEntity which will be processed to create Show Overlays
     */
    abstract fun addPendingShowActions(actions: List<ActionEntity>)
    /**
     * adds ActionEntity which will be processed to create Hide Overlays
     */
    abstract fun addPendingHideActions(actions: List<ActionEntity>)

    /**
     * set current time & playing status of player
     */
    abstract fun setCurrentTime(time: Long, playing: Boolean)

    /**
     * build Show actions which belongs to current time (now until next second)
     * If the action has animation, it will be handled
     */
    abstract fun buildPendingAnnotationsForCurrentTime()

    /**
     * build actions, which their 'offset' is passed, but not their duration [offset + duration]
     * if the action does not have duration, the possible related HideAction should be considered
     */
    abstract fun buildLingeringAnnotationsUpToCurrentTime()

    /**
     * builds all Removal types of action from start to end
     */
    abstract fun buildRemovalAnnotations()
    /**
     * builds all Removal types of action from start to current time
     */
    abstract fun buildRemovalAnnotationsUpToCurrentTime()

    /**
     * returns actions which are about to go to their outro animation
     */
    abstract fun buildPendingOutroAnimations()

    /**
     * returns actions which are in their Intro animation time
     */
    abstract fun buildLingeringIntroAnimations(isPlaying: Boolean)
    /**
     * returns actions which are in their Outro animation time
     */
    abstract fun buildLingeringOutroAnimations(isPlaying: Boolean)



}