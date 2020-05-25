package tv.mycujoo.mls.entity.actions

import com.google.gson.annotations.SerializedName

class MetaData {
    @SerializedName("key")
    var key: String? = null

    @SerializedName("value")
    var value: String? = null
}
