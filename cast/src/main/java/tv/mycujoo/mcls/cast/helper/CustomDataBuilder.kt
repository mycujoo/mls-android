package tv.mycujoo.mcls.cast.helper

import org.json.JSONObject
import tv.mycujoo.domain.entity.Widevine
import tv.mycujoo.mcls.enum.C.Companion.CAST_CUSTOM_PLAYLIST_URL
import tv.mycujoo.mcls.enum.C.Companion.CAST_EVENT_ID_KEY
import tv.mycujoo.mcls.enum.C.Companion.CAST_IDENTITY_TOKEN
import tv.mycujoo.mcls.enum.C.Companion.CAST_LICENSE_URL_KEY
import tv.mycujoo.mcls.enum.C.Companion.CAST_PROTECTION_SYSTEM_KEY
import tv.mycujoo.mcls.enum.C.Companion.CAST_PSEUDO_USER_ID_KEY
import tv.mycujoo.mcls.enum.C.Companion.CAST_PUBLIC_KEY_KEY
import tv.mycujoo.mcls.enum.C.Companion.DRM_WIDEVINE

class CustomDataBuilder {
    companion object {
        /**
         * Builds given info into JSONObject,
         * May be used for sending info to remote media client
         */
        fun build(
            id: String,
            publicKey: String?,
            pseudoUserId: String?,
            identityToken: String?
        ): JSONObject = JSONObject()
            .put(CAST_EVENT_ID_KEY, id)
            .put(CAST_PUBLIC_KEY_KEY, publicKey)
            .put(CAST_PSEUDO_USER_ID_KEY, pseudoUserId)
            .put(CAST_IDENTITY_TOKEN, identityToken)

        fun build(customPlaylistUrl: String): JSONObject = JSONObject()
            .put(CAST_CUSTOM_PLAYLIST_URL, customPlaylistUrl)
    }
}
