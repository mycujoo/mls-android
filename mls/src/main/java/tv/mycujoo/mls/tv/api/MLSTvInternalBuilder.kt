package tv.mycujoo.mls.tv.api

import android.app.Activity
import kotlinx.coroutines.CoroutineScope
import okhttp3.OkHttpClient
import tv.mycujoo.mls.data.IDataManager
import tv.mycujoo.mls.di.DaggerMlsComponent
import tv.mycujoo.mls.di.NetworkModule
import tv.mycujoo.mls.manager.IPrefManager
import tv.mycujoo.mls.network.socket.IReactorSocket
import tv.mycujoo.mls.network.socket.MainWebSocketListener
import tv.mycujoo.mls.network.socket.ReactorSocket
import javax.inject.Inject

class MLSTvInternalBuilder(activity: Activity) {
    @Inject
    lateinit var eventsRepository: tv.mycujoo.domain.repository.EventsRepository

    @Inject
    lateinit var dispatcher: CoroutineScope

    @Inject
    lateinit var okHttpClient: OkHttpClient

    @Inject
    lateinit var dataManager: IDataManager

    @Inject
    lateinit var prefManager: IPrefManager

    lateinit var reactorSocket: IReactorSocket
    private lateinit var mainWebSocketListener: MainWebSocketListener

    init {
        val dependencyGraph =
            DaggerMlsComponent.builder().networkModule(NetworkModule(activity)).build()
        dependencyGraph.inject(this)

        mainWebSocketListener = MainWebSocketListener()
        reactorSocket = ReactorSocket(okHttpClient, mainWebSocketListener)
    }

}
