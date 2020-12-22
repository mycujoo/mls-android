package tv.mycujoo.mls.helper

import tv.mycujoo.domain.entity.Action

interface IDownloaderClient {
    fun download(
        showOverlayAction: Action.ShowOverlayAction,
        callback: (Action.ShowOverlayAction) -> Unit
    )
}
