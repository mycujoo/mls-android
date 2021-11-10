package tv.mycujoo.data.jsonadapter

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import org.joda.time.DateTime

class JodaJsonAdapter {
    @ToJson fun toJson(dateTime: DateTime): String {
        return dateTime.toString()
    }

    @FromJson
    fun fromJson(dateTime: String): DateTime {
        return DateTime.parse(dateTime)
    }
}