package tv.mycujoo.mcls.helper

import org.amshove.kluent.shouldBeEqualTo
import org.junit.Test
import tv.mycujoo.domain.entity.EventStatus

class EventFilterFactoryTest {

    @Test
    fun testEventStatusFilterWithSingleStatusFilter() {
        val filter = EventFilterFactory()
            .withEventStatus(listOf(EventStatus.EVENT_STATUS_SCHEDULED))
            .build()

        filter shouldBeEqualTo "status:EVENT_STATUS_SCHEDULED"
    }

    @Test
    fun testEventStatusFilterWithMultipleStatusFilter() {
        val filter = EventFilterFactory()
            .withEventStatus(listOf(EventStatus.EVENT_STATUS_SCHEDULED, EventStatus.EVENT_STATUS_FINISHED))
            .build()

        filter shouldBeEqualTo "status:(EVENT_STATUS_SCHEDULED OR EVENT_STATUS_FINISHED)"
    }
}