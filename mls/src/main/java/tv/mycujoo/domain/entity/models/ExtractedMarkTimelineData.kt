package tv.mycujoo.domain.entity.models

/**
 * Extracted data needed to create a ShowTimelineMarker
 */
data class ExtractedMarkTimelineData(
    val seekOffset: Long,
    val label: String,
    var color: String
)
