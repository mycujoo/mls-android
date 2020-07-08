package tv.mycujoo.mls.core

import tv.mycujoo.domain.entity.ActionEntity

abstract class AnnotationBuilder {
    /**
     * adds ActionEntity which will be processed to create Overlays
     */
    abstract fun addPendingActions(actions: List<ActionEntity>)

    abstract fun setCurrentTime(time: Long, playing: Boolean)
    abstract fun buildPendingAnnotationsForCurrentTime()

    /**
     * returns list of actions, which their 'offset' is passed, but not their duration [offset + duration]
     */
    abstract fun buildLingeringAnnotations()

    /**
     * Should return list of all Removal types of action from start to end
     */
    abstract fun buildRemovalAnnotations()

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