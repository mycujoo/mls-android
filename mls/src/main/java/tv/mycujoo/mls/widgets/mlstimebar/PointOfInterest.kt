package tv.mycujoo.mls.widgets.mlstimebar

data class PointOfInterest(
    val offset: Long,
    val seekOffset : Long,
    val title: List<String>,
    val poiType: PointOfInterestType
)