package tv.mycujoo.mcls

import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@DelicateCoroutinesApi
@ExperimentalCoroutinesApi
class CoroutineTestRule : TestWatcher() {

    val standardTestDispatcher = StandardTestDispatcher(name = "UI Thread")

    override fun starting(description: Description?) {
        super.starting(description)
        Dispatchers.setMain(standardTestDispatcher)
    }

    override fun finished(description: Description?) {
        super.finished(description)
        Dispatchers.resetMain() // reset the main dispatcher to the original Main dispatcher
    }
}