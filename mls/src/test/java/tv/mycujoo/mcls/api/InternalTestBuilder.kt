package tv.mycujoo.mcls.api

import android.app.Activity
import org.mockito.Mockito.mock
import tv.mycujoo.mcls.core.InternalBuilder
import tv.mycujoo.mcls.di.DaggerTestMlsComponent
import tv.mycujoo.mcls.enum.LogLevel
import tv.mycujoo.mcls.manager.ViewHandler
import tv.mycujoo.mcls.manager.contracts.IViewHandler

class InternalTestBuilder(private val activity: Activity) :
    InternalBuilder(activity, null, LogLevel.MINIMAL) {

    var mockViewHandler: IViewHandler = mock(ViewHandler::class.java)

    override fun initialize() {
        val appModule = TestAppModule()
        val networkModule = TestNetworkModule(activity)
        val repositoryModule = TestRepositoryModule()
        val dependencyGraph =
            DaggerTestMlsComponent.builder()
                .appModule(appModule)
                .networkModule(networkModule)
                .repositoryModule(repositoryModule)
                .build()
        dependencyGraph.inject(this)

        viewHandler = mockViewHandler
    }
}