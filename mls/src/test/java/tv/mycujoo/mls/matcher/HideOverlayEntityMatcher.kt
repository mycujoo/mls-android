package tv.mycujoo.mls.matcher

import org.mockito.ArgumentMatcher
import tv.mycujoo.domain.entity.HideOverlayActionEntity

class HideOverlayEntityMatcher(private val id: String) : ArgumentMatcher<HideOverlayActionEntity> {
    override fun matches(argument: HideOverlayActionEntity?): Boolean {
        return argument!!.id == id
    }
}