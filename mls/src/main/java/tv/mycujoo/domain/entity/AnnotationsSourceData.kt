package tv.mycujoo.domain.entity

import com.google.gson.annotations.SerializedName

data class AnnotationsSourceData(
    @SerializedName("annotations") val annotations: List<AnnotationSourceData>?
)