package tv.mycujoo.mls.core

/**
 * startTime format should be dd-MM-yyy '-' HH:mm
 */
data class ExternalEvent(
    val videoUrl: String,
    val title: String,
    val description: String,
    val startTime: String
)
