package tv.mycujoo.mls.core

import android.app.Activity
import android.content.res.AssetManager
import androidx.test.espresso.idling.CountingIdlingResource
import kotlinx.coroutines.CoroutineScope
import okhttp3.OkHttpClient
import tv.mycujoo.mls.api.DataProviderImpl
import tv.mycujoo.mls.di.DaggerMlsComponent
import tv.mycujoo.mls.di.NetworkModule
import tv.mycujoo.mls.manager.IPrefManager
import tv.mycujoo.mls.manager.ViewIdentifierManager
import javax.inject.Inject

open class InternalBuilder(private val activity: Activity) {
    @Inject
    lateinit var eventsRepository: tv.mycujoo.domain.repository.EventsRepository

    @Inject
    lateinit var dispatcher: CoroutineScope

    @Inject
    lateinit var okHttpClient: OkHttpClient

    @Inject
    lateinit var dataProvider: DataProviderImpl

    @Inject
    lateinit var prefManager: IPrefManager

    lateinit var viewIdentifierManager: ViewIdentifierManager

    open fun initialize() {
        val dependencyGraph =
            DaggerMlsComponent.builder().networkModule(NetworkModule(activity)).build()
        dependencyGraph.inject(this)

        viewIdentifierManager = ViewIdentifierManager(dispatcher, CountingIdlingResource("ViewIdentifierManager"))

    }

    fun getAssetManager(): AssetManager = activity.assets
}
