package tv.mycujoo.mcls.cast

import tv.mycujoo.domain.entity.Widevine

data class CasterLoadRemoteMediaParams(
    val id: String,
    val publicKey: String,
    val uuid: String?,
    val widevine: Widevine?,
    val fullUrl: String,
    val title: String,
    val thumbnailUrl: String,
    val isPlaying: Boolean,
    val currentPosition: Long
)
