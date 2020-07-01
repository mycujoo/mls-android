package tv.mycujoo.mls.model

data class Event(
    val id: String,
    val stream: Stream,
    val name: String,
    val location: String,
    val status: String
)