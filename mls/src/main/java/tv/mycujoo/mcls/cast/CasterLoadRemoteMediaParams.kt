package tv.mycujoo.mcls.cast

data class CasterLoadRemoteMediaParams(
    val id: String,
    val title: String,
    val thumbnailUrl: String,
    val isPlaying: Boolean,
    val currentPosition: Long,

    val identityToken: String? = null,
    val pseudoUserId: String? = null,
    val publicKey: String? = null,

    val customPlaylistUrl: String? = null,
)
