package tv.mycujoo.mls.utils

import tv.mycujoo.domain.entity.EventEntity

class PlayerUtils {
    companion object {
        fun isStreamPlayable(eventEntity: EventEntity): Boolean {
            return isStreamRawPlayable(eventEntity) || isStreamWidevinePlayable(eventEntity)
        }

        private fun isStreamRawPlayable(eventEntity: EventEntity): Boolean {
            eventEntity.streams.firstOrNull()?.let { stream ->
                return stream.fullUrl != null
            }
            return false
        }

        private fun isStreamWidevinePlayable(eventEntity: EventEntity): Boolean {
            eventEntity.streams.firstOrNull()?.widevine?.let { widevine ->
                return widevine.licenseUrl != null && widevine.fullUrl != null
            }
            return false
        }
    }
}