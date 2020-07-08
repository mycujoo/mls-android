package tv.mycujoo.domain.entity

data class NEWAnnotationEntity(
    val id: String,
    var offset: Long,
    var timeLineId: String,
    var actions: List<NEWActionEntity>
)