package tv.mycujoo.mls.api

import tv.mycujoo.domain.entity.EventEntity

interface PlayerController {
    fun play()
    fun pause()
    fun next()
    fun previous()
    fun displayEventInfo(eventEntity: EventEntity)
}