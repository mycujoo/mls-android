package tv.mycujoo.mls.network

import tv.mycujoo.mls.entity.HighlightAction
import tv.mycujoo.mls.entity.AnnotationSourceData
import tv.mycujoo.mls.entity.TimeLineItem
import tv.mycujoo.mls.entity.actions.AbstractAction
import tv.mycujoo.mls.entity.actions.ActionWrapper
import tv.mycujoo.mls.model.PlacardSpecs

interface Api {

    fun getPlacardsSpecs(): List<PlacardSpecs>


    fun getAnnotations(): List<AnnotationSourceData>
    fun getTimeLineMarkers(): List<TimeLineItem>

    fun getHighlights(): List<HighlightAction>

    fun getActions(): List<ActionWrapper>

}
