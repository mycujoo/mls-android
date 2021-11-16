package tv.mycujoo.mcls.tv.api

import tv.mycujoo.mcls.ima.IIma
import tv.mycujoo.mcls.manager.IPrefManager
import tv.mycujoo.mcls.manager.Logger
import tv.mycujoo.mcls.network.socket.IReactorSocket
import tv.mycujoo.mcls.player.MediaFactory
import java.util.*
import javax.inject.Inject

class MLSTvInternalBuilder @Inject constructor(
    val mediaFactory: MediaFactory,
    val reactorSocket: IReactorSocket,
    val prefManager: IPrefManager,
) {

    val ima: IIma? = null

    companion object {
        @JvmStatic
        private var uuid: String? = null
    }


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
