package tv.mycujoo.mls.analytic

import android.util.Log
import com.npaw.youbora.lib6.plugin.Plugin
import tv.mycujoo.mls.model.Event

class YouboraClient(var publicKey: String, val plugin: Plugin) {


    val userName: String? = null

    fun logEvent(event: Event?) {
        if (event == null) {
            Log.e("YouboraClient", "event is null")
            return
        }
        plugin.options.username = userName.orEmpty()
        plugin.options.contentTitle = event.name
        plugin.options.contentResource = event.streamUrl

        plugin.options.adCustomDimension1 = publicKey
        plugin.options.adCustomDimension2 = event.id
        plugin.options.adCustomDimension3 = event.location
        plugin.options.adCustomDimension4 = event.status
    }

    fun stop() {
        plugin.fireStop()
    }

    fun start() {
        if (plugin.adapter != null){
            plugin.adapter.fireResume()
        }
    }

}