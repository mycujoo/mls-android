package tv.mycujoo.matchers

import android.view.View
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher

class ViewSizeMatcher(private val widthPercentage: Int) : TypeSafeMatcher<View>() {
    override fun describeTo(description: Description?) {
        description?.appendText("with SizeMatcher: ")
        description?.appendValue(widthPercentage)
    }

    override fun matchesSafely(view: View): Boolean {

        val width = view.width
        val height = view.height

        return widthPercentage == width
    }
}