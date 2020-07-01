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

            val target = (screenHeight * expectedVerticalBias).toInt()
            when (target) {
                in view.top..view.bottom -> {
                    return true
                }
                else -> return false
            }
        }

        return false
    }
}