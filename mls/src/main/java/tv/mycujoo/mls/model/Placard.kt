package tv.mycujoo.mls.model

data class Placard(
    val id: Int,
    val name: String,
    val metadata: String,
    val actions: List<String>
) {

}
