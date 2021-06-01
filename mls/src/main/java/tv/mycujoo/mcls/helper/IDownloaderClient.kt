package tv.mycujoo.mcls.helper

import tv.mycujoo.domain.entity.Action

/**
 * Interface to download SVG
 */
interface IDownloaderClient {
    fun download(
        showOverlayAction: Action.ShowOverlayAction,
        callback: (Action.ShowOverlayAction) -> Unit
    )
}
