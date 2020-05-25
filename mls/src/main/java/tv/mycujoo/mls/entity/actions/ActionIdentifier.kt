package tv.mycujoo.mls.entity.actions

import com.google.gson.annotations.SerializedName

data class ActionIdentifier(
    @SerializedName("actionAbstractId") var actionAbstractId: String? = null,
    @SerializedName("metadata") var metadata: List<MetaData>? = null
) {
}
