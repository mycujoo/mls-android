package tv.mycujoo.mls.core

import tv.mycujoo.domain.entity.IncrementVariableEntity
import tv.mycujoo.domain.entity.OverlayObject
import tv.mycujoo.domain.entity.SetVariableEntity

abstract class ActionBuilder {
    /**
     * set current time & playing status of player
     */
    abstract fun setCurrentTime(time: Long, playing: Boolean)

    /**
     * adds OverlayObject which will be processed to create Show Overlays
     */
    abstract fun addOverlayObjects(overlayObject: List<OverlayObject>)

    /**
     * build All actions (Show or Hide) which belongs to current time (now until next second)
     * If the action has animation, it will be handled.
     */
    abstract fun buildCurrentTimeRange()

    /**
     * Remove everything from screen
     */
    abstract fun removeAll()

    /**
     * build all actions which are lingering from past
     */
    abstract fun buildLingerings()


    /**region Variables*/
    abstract fun addSetVariableEntities(setVariables: List<SetVariableEntity>)
    abstract fun addIncrementVariableEntities(incrementVariables: List<IncrementVariableEntity>)

    abstract fun buildSetVariables()
    /**endregion */
}