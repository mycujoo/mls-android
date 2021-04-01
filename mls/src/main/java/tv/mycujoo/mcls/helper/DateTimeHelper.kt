package tv.mycujoo.mcls.helper

import org.joda.time.DateTime

class DateTimeHelper {

    companion object {
        fun getDateTime(input: String): String {

            val localDateTime = DateTime.parse(input).toLocalDateTime()

            return localDateTime.toString("dd-MM-yyy '-' HH:mm")
        }
    }

}
