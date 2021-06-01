package tv.mycujoo.mcls.helper

import org.joda.time.DateTime

/**
 * Helps in printing Date in human friendly format
 */
class DateTimeHelper {

    companion object {

        private const val DATE_FORMAT = "dd-MM-yyy '-' HH:mm"

        /**
         * convert iso string to formatted string like: '10-08-2021 - 18:00'
         * @param input iso date in string
         */
        fun getDateTime(input: String): String? {
            return try {
                val localDateTime = DateTime.parse(input).toLocalDateTime()
                localDateTime.toString(DATE_FORMAT)
            } catch (e: Exception) {
                null
            }
        }

        /**
         * convert dateTime to formatted string like: '10-08-2021 - 18:00'
         * @param input dateTime
         */
        fun formatDatetime(input: DateTime): String? {
            return try {
                input.toString(DATE_FORMAT)
            } catch (e: Exception) {
                null
            }
        }
    }

}
