package tv.mycujoo.domain.entity

import com.google.gson.annotations.SerializedName

data class Events(@SerializedName("events") val events: List<EventEntity>)
