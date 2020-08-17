package tv.mycujoo.mls.analytic

import android.util.Log
import com.npaw.youbora.lib6.plugin.Plugin
import tv.mycujoo.domain.entity.EventEntity

class YouboraClient(private val uuid: String, private val plugin: Plugin) {

    fun logEvent(event: EventEntity?, live: Boolean) {
        if (event == null) {
            Log.e("YouboraClient", "event is null")
            return
        }
        plugin.options.username = uuid
        plugin.options.contentTitle = event.title
        plugin.options.contentResource = event.streams.firstOrNull()?.toString()
        plugin.options.contentIsLive = live


        plugin.options.contentCustomDimension2 = event.id
        plugin.options.contentCustomDimension14 = "MLS"
        plugin.options.contentCustomDimension15 = event.streams.firstOrNull()?.id
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