package tv.mycujoo.mcls.api

import android.app.Activity
import org.mockito.Mockito.mock
import tv.mycujoo.DaggerMLSApplication_HiltComponents_SingletonC
import tv.mycujoo.mcls.core.InternalBuilder
import tv.mycujoo.mcls.manager.ViewHandler
import tv.mycujoo.mcls.manager.contracts.IViewHandler

class InternalTestBuilder(private val activity: Activity) :
    InternalBuilder(activity) {

    var mockViewHandler: IViewHandler = mock(ViewHandler::class.java)

    override fun initialize() {
        val appModule = TestAppModule()
        val networkModule = TestNetworkModule()
//        val repositoryModule = TestRepositoryModule()
//            DaggerMLSApplication_HiltComponents_SingletonC.builder()
//                .appModule(appModule)
//                .networkModule(networkModule)
//                .repositoryModule(repositoryModule)
//                .build()

        viewHandler = mockViewHandler
    }
}