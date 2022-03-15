package tv.mycujoo.data.mapper

import org.joda.time.DateTime
import tv.mycujoo.data.model.*
import tv.mycujoo.domain.entity.*

class EventMapper {
    companion object {
        fun mapEventSourceDataToEventEntity(sourceData: EventSourceData): EventEntity {
            val location = mapLocationSourceDataToLocationEntity(sourceData.locationSourceData)
            val date = DateTime.parse(sourceData.start_time)
            val eventStatus = EventStatus.fromValueOrUnspecified(sourceData.status)

            val streams = sourceData.streams.map { mapStreamSourceToStreamEntity(it) }
            val metaData = mapMetaDataSourceDataToMetaDataEntity(sourceData.metadata)

            return EventEntity(
                sourceData.id,
                sourceData.title,
                sourceData.description,
                sourceData.thumbnailUrl,
                sourceData.poster_url,
                location,
                sourceData.organiser,
                date,
                eventStatus,
                streams,
                sourceData.timezone,
                sourceData.timeline_ids,
                metaData,
                sourceData.is_test,
                sourceData.is_protected,
            )
        }

        private fun mapMetaDataSourceDataToMetaDataEntity(metadata: MetadataSourceData): Metadata {
            return Metadata()
        }

        private fun mapStreamSourceToStreamEntity(sourceData: StreamSourceData): Stream {
            val widevine = mapWidevineSourceDataToWidevineEntity(sourceData.drm?.widevine)
            val errorCodeAndMessage =
                mapErrorCodeAndMessageSourceDataToErrorCodeAndMessageEntity(sourceData.errorCodeAndMessage)
            return Stream(
                sourceData.id,
                sourceData.dvrWindowString,
                sourceData.fullUrl,
                widevine,
                errorCodeAndMessage
            )
        }

        private fun mapErrorCodeAndMessageSourceDataToErrorCodeAndMessageEntity(sourceData: ErrorCodeAndMessageSourceData?): ErrorCodeAndMessage? {
            if (sourceData?.code == null || sourceData.message == null) {
                return null
            }
            return ErrorCodeAndMessage(sourceData.code, sourceData.message)
        }

        private fun mapWidevineSourceDataToWidevineEntity(sourceData: WidevineSourceData?): Widevine? {
            if (sourceData?.fullUrl == null || sourceData.licenseUrl == null) {
                return null
            }
            return Widevine(sourceData.fullUrl, sourceData.licenseUrl)
        }


        private fun mapLocationSourceDataToLocationEntity(sourceData: LocationSourceData): Location {
            val physical = mapPhysicalSourceDataToPhysicalEntity(sourceData.physicalSourceData)
            return Location(physical)
        }

        private fun mapPhysicalSourceDataToPhysicalEntity(sourceData: PhysicalSourceData): Physical {
            val coordinates = mapCoordinatesSourceCodeToCoordinatesEntity(sourceData.coordinates)
            return Physical(
                sourceData.city,
                sourceData.continent_code,
                coordinates,
                sourceData.country_code,
                sourceData.venue
            )
        }

        private fun mapCoordinatesSourceCodeToCoordinatesEntity(sourceData: CoordinatesSourceData): Coordinates {
            return Coordinates(sourceData.latitude, sourceData.longitude)
        }
    }
}