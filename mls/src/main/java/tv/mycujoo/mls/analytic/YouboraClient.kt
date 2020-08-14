package tv.mycujoo.mls.analytic

import android.util.Log
import com.npaw.youbora.lib6.plugin.Plugin
import tv.mycujoo.domain.entity.EventEntity

class YouboraClient(var publicKey: String, val uuid: String, val plugin: Plugin) {


    val userName: String? = null

    fun logEvent(event: EventEntity?) {
        if (event == null) {
            Log.e("YouboraClient", "event is null")
            return
        }
        plugin.options.username = uuid
        plugin.options.contentTitle = event.title
        plugin.options.contentResource = event.streams.firstOrNull()?.toString()

        plugin.options.adCustomDimension1 = publicKey
        plugin.options.adCustomDimension2 = event.id

        plugin.options.contentCustomDimension14 = "MLS"

//        plugin.options.adCustomDimension3 = event.location
//        plugin.options.adCustomDimension4 = event.status
    }

    fun stop() {
        plugin.fireStop()
    }

    fun start() {
        if (plugin.adapter != null) {
            plugin.adapter.fireResume()
        }
    }

}