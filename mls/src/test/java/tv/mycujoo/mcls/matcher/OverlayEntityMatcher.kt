package tv.mycujoo.mcls.matcher

import org.mockito.ArgumentMatcher
import tv.mycujoo.domain.entity.OverlayEntity

class OverlayEntityMatcher(private val id: String) : ArgumentMatcher<OverlayEntity> {
    override fun matches(argument: OverlayEntity?): Boolean {
        return argument!!.id == id
    }
}