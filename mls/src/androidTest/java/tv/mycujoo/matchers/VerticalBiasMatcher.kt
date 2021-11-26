package tv.mycujoo.matchers

import android.view.View
import android.view.ViewGroup
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher

class VerticalBiasMatcher(private val expectedVerticalBias: Float) : TypeSafeMatcher<View>() {
    override fun describeTo(description: Description?) {
        description?.appendText("expecting vertical bias to be: ")
        description?.appendValue(expectedVerticalBias)
    }

    override fun matchesSafely(item: View?): Boolean {
        item?.let { view ->
            val screenHeight = (view.parent as ViewGroup).height

            return when ((screenHeight * expectedVerticalBias).toInt()) {
                in view.top..view.bottom -> {
                    true
                }
                else -> false
            }
        }

        return false
    }
}