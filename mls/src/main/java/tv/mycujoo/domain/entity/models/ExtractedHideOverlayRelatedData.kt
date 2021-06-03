package tv.mycujoo.domain.entity.models

import tv.mycujoo.domain.entity.AnimationType

/**
 * Extracted data needed to create a HideOverlayAction
 */
data class ExtractedHideOverlayRelatedData(
    val id: String,
    val outroAnimationType: AnimationType,
    val outroAnimationDuration: Long
)