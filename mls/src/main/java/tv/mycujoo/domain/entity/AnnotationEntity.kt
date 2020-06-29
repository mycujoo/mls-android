package tv.mycujoo.domain.entity

data class AnnotationEntity(
    val id: String,
    var offset: Long,
    var timeLineId: String,
    var actions: List<ActionEntity>
)