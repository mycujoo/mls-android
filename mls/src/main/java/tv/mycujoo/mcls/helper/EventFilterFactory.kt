package tv.mycujoo.mcls.helper

import tv.mycujoo.domain.entity.EventStatus

class EventFilterFactory {

    var filters = mutableListOf<String>()

    fun withEventStatus(statuses: List<EventStatus>) = apply {
        if (statuses.isNotEmpty()) {
            val builder = StringBuilder()
            builder.append("status:")

            if (statuses.size == 1) {
                builder.append(statuses[0].name)
                filters.add(builder.toString())
                return@apply
            }

            builder.append("(")
            val statusesQuery = statuses.joinToString(" OR ") {
                it.name
            }

            builder.append(statusesQuery)

            if (statuses.size > 1) {
                builder.append(")")
            }

            filters.add(builder.toString())
        }
    }

    fun build(): String {
        return if (filters.isEmpty()) {
            ""
        } else {
            filters.joinToString(" ") { it }
        }
    }
}