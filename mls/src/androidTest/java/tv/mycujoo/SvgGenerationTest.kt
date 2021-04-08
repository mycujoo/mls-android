package tv.mycujoo

import android.content.Intent
import android.view.View
import android.widget.FrameLayout
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.launchActivity
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.caverock.androidsvg.SVG
import com.caverock.androidsvg.SVGImageView
import com.caverock.androidsvg.SVGParseException
import junit.framework.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import tv.mycujoo.mcls.BlankActivity
import tv.mycujoo.mcls.R
import java.util.concurrent.TimeUnit


@RunWith(AndroidJUnit4::class)
class SvgGenerationTest {

    private lateinit var svgImageView: SVGImageView

    @Before
    fun setUp() {
        val intent = Intent(ApplicationProvider.getApplicationContext(), BlankActivity::class.java)
        val scenario = launchActivity<BlankActivity>(intent)
        scenario.onActivity { activity ->
            val frameLayout = activity.findViewById<FrameLayout>(R.id.blankActivity_rootView)
            svgImageView = SVGImageView(frameLayout.context)
            svgImageView.id = View.generateViewId()
            frameLayout.addView(svgImageView)
        }

    }


    @Test
    fun generateSvg() {
        val timeMillisBeforeTest = System.currentTimeMillis()
        svgImageView.post {
            try {

                for (i in 0..1000) {
                    val svg = SVG.getFromString(getTimeSvgString())
                    svgImageView.setSVG(svg)
                }

            } catch (e: SVGParseException) {
                e.printStackTrace()
                fail("failed executing test generateSvg() " + e.localizedMessage)
            }
            val timeMillisAfterTest = System.currentTimeMillis()
            println("Duration: ${timeMillisAfterTest - timeMillisBeforeTest} ms")
        }


    }

    fun getTimeSvgString(): String {
//        val currentTimeMillis = System.currentTimeMillis()
//        val time: String = getDurationBreakdown(currentTimeMillis)
        return "<svg height=\"30\" width=\"200\"><rect width=\"200\" height=\"30\" style=\"fill:rgb(211,211,211);stroke-width:3;stroke:rgb(128, 128, 128)\" /><text x=\"0\" y=\"15\" fill=\"red\">Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.</text></svg>"
    }

    fun getDurationBreakdown(millis: Long): String {
        require(millis >= 0) { "Duration must be greater than zero!" }
        val days = TimeUnit.MILLISECONDS.toDays(millis)
        val hours = TimeUnit.MILLISECONDS.toHours(millis) % 24
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
        val milliseconds = millis % 1000
        return String.format(
            "%d Minutes %d Seconds %d Milliseconds",
            minutes, seconds, milliseconds
        )
    }


}