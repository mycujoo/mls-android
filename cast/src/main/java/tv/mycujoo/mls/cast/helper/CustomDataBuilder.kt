package tv.mycujoo.mls.cast.helper

import org.json.JSONObject
import tv.mycujoo.domain.entity.Widevine
import tv.mycujoo.mls.enum.C.Companion.CAST_EVENT_ID_KEY
import tv.mycujoo.mls.enum.C.Companion.CAST_LICENSE_URL_KEY
import tv.mycujoo.mls.enum.C.Companion.CAST_PROTECTION_SYSTEM_KEY
import tv.mycujoo.mls.enum.C.Companion.CAST_PSEUDO_USER_ID_KEY
import tv.mycujoo.mls.enum.C.Companion.CAST_PUBLIC_KEY_KEY
import tv.mycujoo.mls.enum.C.Companion.DRM_WIDEVINE

class CustomDataBuilder {
    companion object {
        fun build(id: String, publicKey: String, uuid: String?, widevine: Widevine?): JSONObject {
            val customData = JSONObject()
                .put(CAST_EVENT_ID_KEY, id)
                .put(CAST_PUBLIC_KEY_KEY, publicKey)
                .put(CAST_PSEUDO_USER_ID_KEY, uuid)
            if (widevine?.licenseUrl != null) {
                customData.put(CAST_LICENSE_URL_KEY, widevine.licenseUrl)
                    .put(CAST_PROTECTION_SYSTEM_KEY, DRM_WIDEVINE)
            }
            return customData
        }

    }
}
