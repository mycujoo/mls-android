package tv.mycujoo.mls.entity.actions

import com.google.gson.annotations.SerializedName

@Deprecated("Use Action instead")
data class ActionSourceData(
    @SerializedName("actionAbstractId") var actionAbstractId: String? = null,
    @SerializedName("metadata") var metadata: List<MetaData>? = null
) {
}
