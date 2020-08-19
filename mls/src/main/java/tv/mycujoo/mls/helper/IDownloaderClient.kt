package tv.mycujoo.mls.helper

import tv.mycujoo.domain.entity.OverlayEntity

interface IDownloaderClient {
    fun download(
        overlayEntity: OverlayEntity,
        callback: (OverlayEntity) -> Unit
    )
}
