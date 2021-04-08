package tv.mycujoo.mcls.matcher

import android.net.Uri
import org.mockito.ArgumentMatcher

class UriArgumentMatcher(private val uriString: String) : ArgumentMatcher<Uri> {
    override fun matches(argument: Uri?): Boolean {
        return uriString == argument.toString()
    }
}