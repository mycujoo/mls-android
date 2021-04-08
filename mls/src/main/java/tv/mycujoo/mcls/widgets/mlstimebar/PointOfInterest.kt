package tv.mycujoo.mcls.widgets.mlstimebar

data class PointOfInterest(
    val offset: Long,
    val seekOffset : Long,
    val title: String,
    val poiType: PointOfInterestType
)