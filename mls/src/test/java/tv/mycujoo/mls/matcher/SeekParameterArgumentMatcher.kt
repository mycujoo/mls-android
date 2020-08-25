package tv.mycujoo.mls.matcher

import com.google.android.exoplayer2.SeekParameters
import org.mockito.ArgumentMatcher

class SeekParameterArgumentMatcher(private val seekTolerance: Long) : ArgumentMatcher<SeekParameters> {
    override fun matches(argument: SeekParameters?): Boolean {
        return seekTolerance / 2 == argument?.toleranceBeforeUs && seekTolerance / 2 == argument.toleranceAfterUs
    }
}