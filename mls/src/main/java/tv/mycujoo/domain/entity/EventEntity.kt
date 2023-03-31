package tv.mycujoo.domain.entity

import org.joda.time.DateTime
import tv.mycujoo.data.entity.ServerConstants.Companion.ERROR_CODE_GEOBLOCKED
import tv.mycujoo.data.entity.ServerConstants.Companion.ERROR_CODE_NO_ENTITLEMENT
import tv.mycujoo.data.entity.ServerConstants.Companion.ERROR_CODE_UNSPECIFIED
import tv.mycujoo.mcls.enum.StreamStatus
import tv.mycujoo.mcls.helper.DateTimeHelper


data class EventEntity(
    val id: String,
    val title: String,
    val description: String?,
    val thumbnailUrl: String?,
    val poster_url: String?,
    val physical: Physical?,
    val organiser: String?,
    val start_time: DateTime?,
    val status: EventStatus,
    val streams: List<Stream>,
    val timezone: String?,
    val timeline_ids: List<String>,
    val metadata: Metadata?,
    val is_test: Boolean,
    val isNativeMLS: Boolean = true,
    val is_protected: Boolean = false
) {
    fun streamStatus(): StreamStatus {
        if (streams.isEmpty()) {
            return StreamStatus.NO_STREAM_URL
        }
        val stream = streams[0]
        if (stream.hasError()) {
            if (stream.isGeoBlocked()) {
                return StreamStatus.GEOBLOCKED
            }
            if (stream.isNoEntitlement()) {
                return StreamStatus.NO_ENTITLEMENT
            }
            return StreamStatus.UNKNOWN_ERROR
        }

        if (stream.isStreamPlayable()) {
            return StreamStatus.PLAYABLE
        }

        return StreamStatus.UNKNOWN_ERROR
    }

    fun getFormattedStartTimeDate(): String? {
        start_time?.let { return DateTimeHelper.formatDatetime(it) }
        return null
    }
}

data class Stream(
    val id: String,
    val dvrWindowString: String?,
    val fullUrl: String?,
    val widevine: Widevine?,
    val errorCodeAndMessage: ErrorCodeAndMessage? = null
) {
    fun getDvrWindowSize(): Long {
        if (dvrWindowString == null) {
            return Long.MAX_VALUE
        }
        return try {
            dvrWindowString.toLong()
        } catch (e: Exception) {
            Long.MAX_VALUE
        }
    }

    fun isStreamPlayable(): Boolean {
        return isStreamRawPlayable(this) ||
                isStreamWidevinePlayable(this)
    }

    fun hasError(): Boolean {
        return errorCodeAndMessage?.code.isNullOrEmpty().not()
    }

    fun isGeoBlocked(): Boolean {
        errorCodeAndMessage?.let {
            if (it.code == ERROR_CODE_GEOBLOCKED) {
                return true
            }
        }
        return false
    }

    fun isNoEntitlement(): Boolean {
        if (errorCodeAndMessage?.code == ERROR_CODE_NO_ENTITLEMENT) {
            return true
        }
        return false
    }

    fun hasUnknownError(): Boolean {
        if (errorCodeAndMessage?.code == ERROR_CODE_UNSPECIFIED) {
            return true
        }
        return false
    }

    private fun isStreamRawPlayable(stream: Stream): Boolean {
        return stream.fullUrl.isNullOrEmpty().not()
    }

    private fun isStreamWidevinePlayable(stream: Stream): Boolean {
        stream.widevine?.let { widevine ->
            return widevine.licenseUrl.isNullOrEmpty().not() &&
                    widevine.fullUrl.isNullOrEmpty().not()
        }
        return false
    }
}


data class Widevine(
    val fullUrl: String?,
    val licenseUrl: String?
)

data class ErrorCodeAndMessage(
    val code: String?,
    val message: String?
)

class Metadata(
)

data class Physical(
    val city: String,
    val continent_code: String,
    val coordinates: Coordinates,
    val country_code: String,
    val venue: String
)

data class Coordinates(
    val latitude: Double,
    val longitude: Double
)

