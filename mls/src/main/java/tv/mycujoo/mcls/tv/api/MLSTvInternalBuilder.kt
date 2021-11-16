package tv.mycujoo.mcls.tv.api

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import okhttp3.OkHttpClient
import tv.mycujoo.domain.repository.IEventsRepository
import tv.mycujoo.mcls.api.MLSConfiguration
import tv.mycujoo.mcls.data.IDataManager
import tv.mycujoo.mcls.enum.C
import tv.mycujoo.mcls.enum.LogLevel
import tv.mycujoo.mcls.ima.IIma
import tv.mycujoo.mcls.manager.IPrefManager
import tv.mycujoo.mcls.manager.Logger
import tv.mycujoo.mcls.manager.contracts.IViewHandler
import tv.mycujoo.mcls.network.socket.IReactorSocket
import tv.mycujoo.mcls.player.MediaFactory
import java.util.*
import javax.inject.Inject

class MLSTvInternalBuilder @Inject constructor(
    val logger: Logger,
    val viewHandler: IViewHandler,
    val mediaFactory: MediaFactory,
    val reactorSocket: IReactorSocket,
    val dispatcher: CoroutineScope,
    val okHttpClient: OkHttpClient,
    val prefManager: IPrefManager,
    val dataManager: IDataManager,
) {

    val ima: IIma? = null

    var uuid: String? = null


    init {
        uuid = prefManager.get("UUID") ?: UUID.randomUUID().toString()
        persistUUIDIfNotStoredAlready(uuid!!)

        ima?.setAdsLoaderProvider(
            mediaFactory.defaultMediaSourceFactory
        )

        reactorSocket.setUUID(uuid!!)
    }

    private fun persistUUIDIfNotStoredAlready(uuid: String) {
        val storedUUID = prefManager.get("UUID")
        if (storedUUID == null) {
            prefManager.persist("UUID", uuid)
        }
    }
}
