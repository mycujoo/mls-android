package tv.mycujoo.mls.helper

import org.joda.time.DateTime

class DateTimeHelper {

    companion object {
        fun getDateTime(input: String): String {
            try {
                val localDateTime = DateTime.parse(input).toLocalDateTime()
                return localDateTime.toString("dd-MM-yyy '-' HH:mm")
            } catch (e: Exception) {
                return ""
            }
        }
    }

}
