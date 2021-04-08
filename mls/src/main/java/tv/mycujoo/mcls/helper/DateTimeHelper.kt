package tv.mycujoo.mcls.helper

import org.joda.time.DateTime

class DateTimeHelper {

    companion object {
        private const val DATE_FORMAT = "dd-MM-yyy '-' HH:mm"
        fun getDateTime(input: String): String? {
            return try {
                val localDateTime = DateTime.parse(input).toLocalDateTime()
                localDateTime.toString(DATE_FORMAT)
            } catch (e: Exception) {
                null
            }
        }

        fun formatDatetime(input: DateTime): String? {
            return try {
                input.toString(DATE_FORMAT)
            } catch (e: Exception) {
                null
            }
        }
    }

}
