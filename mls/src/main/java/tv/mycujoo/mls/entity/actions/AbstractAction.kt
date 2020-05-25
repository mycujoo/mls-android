package tv.mycujoo.mls.entity.actions

import com.google.gson.annotations.SerializedName

open class AbstractAction {
     @SerializedName("description")
     open lateinit var description : String
}