package tv.mycujoo.mcls.ima

import com.google.android.exoplayer2.MediaItem
import com.google.common.collect.ImmutableList
import tv.mycujoo.domain.entity.EventStatus
import java.util.*

data class ImaAdsIds(
    val adsId: Any
) {
    companion object {
        fun build(mediaItem: MediaItem): ImaAdsIds? {
            val adsConfiguration = mediaItem.playbackProperties?.adsConfiguration?:return null

            val adsIds =  if (adsConfiguration.adsId != null) adsConfiguration.adsId
            else ImmutableList.of(
                mediaItem.mediaId,
                mediaItem.playbackProperties!!.uri,
                adsConfiguration.adTagUri
            )
            if (adsIds == null) return null

            return ImaAdsIds(adsIds)
        }
    }

}
