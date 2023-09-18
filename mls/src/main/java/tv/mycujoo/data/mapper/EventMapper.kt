package tv.mycujoo.data.mapper

import org.joda.time.DateTime
import tv.mycujoo.data.model.CoordinatesSourceData
import tv.mycujoo.data.model.ErrorCodeAndMessageSourceData
import tv.mycujoo.data.model.EventSourceData
import tv.mycujoo.data.model.MetadataSourceData
import tv.mycujoo.data.model.PhysicalSourceData
import tv.mycujoo.data.model.StreamSourceData
import tv.mycujoo.data.model.WidevineSourceData
import tv.mycujoo.domain.entity.Coordinates
import tv.mycujoo.domain.entity.ErrorCodeAndMessage
import tv.mycujoo.domain.entity.EventEntity
import tv.mycujoo.domain.entity.EventStatus
import tv.mycujoo.domain.entity.Metadata
import tv.mycujoo.domain.entity.Physical
import tv.mycujoo.domain.entity.Stream
import tv.mycujoo.domain.entity.Widevine

class EventMapper {
    companion object {
        fun mapEventSourceDataToEventEntity(sourceData: EventSourceData): EventEntity {
            val location = mapPhysicalSourceDataToPhysicalEntity(sourceData.physical)
            val date = DateTime.parse(sourceData.start_time)
            val eventStatus = EventStatus.fromValueOrUnspecified(sourceData.status)

            val streams = sourceData.streams.map { mapStreamSourceToStreamEntity(it) }
            val metaData = mapMetaDataSourceDataToMetaDataEntity(sourceData.metadata)

            return EventEntity(
                id = sourceData.id,
                title = sourceData.title,
                description = sourceData.description,
                thumbnailUrl = sourceData.thumbnailUrl,
                poster_url = sourceData.poster_url,
                physical = location,
                organiser = sourceData.organiser,
                start_time = date,
                status = eventStatus,
                streams = streams,
                timezone = sourceData.timezone,
                timeline_ids = sourceData.timeline_ids,
                metadata = metaData,
                is_test = sourceData.is_test,
                is_protected = sourceData.is_protected,
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


//        private fun mapLocationSourceDataToLocationEntity(sourceData: LocationSourceData): Location {
//            val physical = mapPhysicalSourceDataToPhysicalEntity(sourceData.physicalSourceData)
//            return Location(physical)
//        }

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