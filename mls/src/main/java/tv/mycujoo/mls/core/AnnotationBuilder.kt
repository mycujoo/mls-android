package tv.mycujoo.mls.core

import tv.mycujoo.domain.entity.ActionEntity
import tv.mycujoo.domain.entity.OverlayObject

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
     * If the action has animation, it will be handled.
     */

    /**
     * build actions, which their 'offset' is passed, but not their entire duration [offset + duration + outro-animation-duration]
     * if the action does not have duration, the possible related HideAction should be considered
     */

    /**
     * builds all Removal types of action from start to end
     */

    /**
     * builds all Removal types of action from start to current time
     */

    /**
     * build actions which are about to go to their outro animation.
     * outro-animation can be either in the same action, or in another hide-action object.
     */

    /**
     * build actions which are in their Intro animation time
     * intro-animation is always in the same action.
     */

    /**
     * build actions which are in their Outro animation time.
     */

    // re-write
    abstract fun addOverlayObjects(overlayObject: List<OverlayObject>)
    abstract fun buildCurrentTimeRange()
    // remove ALL, clear screen
    abstract fun removeAll()
    abstract fun buildLingerings()


}