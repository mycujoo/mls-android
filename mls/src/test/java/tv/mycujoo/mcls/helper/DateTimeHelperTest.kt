package tv.mycujoo.mcls.helper

import org.joda.time.DateTime
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import kotlin.test.assertEquals

class DateTimeHelperTest {

    @Before
    fun setUp() {
    }

    @Test
    fun givenServerDateTimeShouldReturnDataAndTimeInStringFormat() {
        val result = DateTimeHelper.getDateTime("2020-07-11T07:32:46Z")


        assertEquals("11-07-2020 - 07:32", result)
    }

    @Test
    fun internalWork() {
        val localDateTime = DateTime.parse("2020-07-11T07:32:46Z").toLocalDateTime()


        assertEquals(2020, localDateTime.year)
        assertEquals(7, localDateTime.monthOfYear)
        assertEquals(11, localDateTime.dayOfMonth)
        assertEquals(7, localDateTime.hourOfDay)
        assertEquals(32, localDateTime.minuteOfHour)
        assertEquals(46, localDateTime.secondOfMinute)
    }

    @Ignore("Should be fixed on the CI level")
    @Test
    fun `formatDatetime formats given DateTime`() {
        val dateTime = DateTime("2020-07-11T07:32:46Z")
        val formattedDatetime = DateTimeHelper.formatDatetime(dateTime)

        assertEquals("11-07-2020 - 09:32", formattedDatetime)

    }
}