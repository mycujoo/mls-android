package tv.mycujoo.matchers

import android.view.View
import android.view.ViewGroup
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher

class HorizontalBiasMatcher(private val expectedVerticalBias: Float) : TypeSafeMatcher<View>() {
    override fun describeTo(description: Description?) {
        description?.appendText("expecting horizontal bias to be")
        description?.appendValue(expectedVerticalBias)
    }

    override fun matchesSafely(item: View?): Boolean {
        item?.let { view ->
            val screenWidth = (view.parent as ViewGroup).width

            return when ((screenWidth * expectedVerticalBias).toInt()) {
                in view.left..view.right -> {
                    true
                }
                else -> false
            }
        }

        return false
    }
}