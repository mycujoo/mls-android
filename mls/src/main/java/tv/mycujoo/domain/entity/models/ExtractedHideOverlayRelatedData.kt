package tv.mycujoo.domain.entity.models

import tv.mycujoo.domain.entity.AnimationType

data class ExtractedHideOverlayRelatedData(
    val id: String,
    val outroAnimationType: AnimationType,
    val outroAnimationDuration: Long
)