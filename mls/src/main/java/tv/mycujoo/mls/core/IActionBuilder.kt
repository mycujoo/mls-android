package tv.mycujoo.mls.core

import tv.mycujoo.data.entity.ActionCollections
import tv.mycujoo.domain.entity.IncrementVariableEntity
import tv.mycujoo.domain.entity.OverlayEntity
import tv.mycujoo.domain.entity.OverlayObject
import tv.mycujoo.domain.entity.SetVariableEntity

abstract class IActionBuilder {
    /**
     * set current time & playing status of player
     */
    abstract fun setCurrentTime(time: Long, playing: Boolean)

    /**
     * adds OverlayObject which will be processed to create Show Overlays
     * Or directly add OverlayEntity list.
     */
    abstract fun addOverlayObjects(overlayObject: List<OverlayObject>)
    abstract fun addOverlayEntities(overlayEntities: List<OverlayEntity>)

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
     * remove those overlay which should not be displayed in current time
     */
    abstract fun removeLeftOvers()


    /**
     * build all actions which are lingering from past
     */
    abstract fun buildLingerings()


    /**region Variables*/
    abstract fun addSetVariableEntities(setVariables: List<SetVariableEntity>)
    abstract fun addIncrementVariableEntities(incrementVariables: List<IncrementVariableEntity>)

    abstract fun addActionCollections(actionCollections: ActionCollections)


    /**
     * compute all variables from the beginning until current time
     */
    abstract fun computeVariableNameValueTillNow()
    /**endregion */

    /**
     * process all timer related actions until current time
     */
    abstract fun processTimers()

}