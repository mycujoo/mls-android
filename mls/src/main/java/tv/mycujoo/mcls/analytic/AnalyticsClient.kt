package tv.mycujoo.mcls.analytic

import tv.mycujoo.domain.entity.EventEntity

interface AnalyticsClient {

    fun logEvent(event: EventEntity?, live: Boolean, onError: (String) -> Unit)

    fun start()

    fun stop()
}
