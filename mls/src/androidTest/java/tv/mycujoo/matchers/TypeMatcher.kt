package tv.mycujoo.matchers

import org.hamcrest.BaseMatcher
import org.hamcrest.Description

class TypeMatcher(private val className: String?) : BaseMatcher<String>() {
    override fun describeTo(description: Description?) {
    }

    override fun matches(item: Any): Boolean {
        return (item as String).equals(className, true)
    }
}