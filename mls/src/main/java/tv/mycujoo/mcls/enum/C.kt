package tv.mycujoo.mcls.enum

/**
 * Define constants that are going to be used across SDK
 */
class C {
    companion object {

        const val UUID_PREF_KEY = "UUID"
        const val PUBLIC_KEY_PREF_KEY = "PUBLIC_KEY"

        const val NETWORK_ERROR_MESSAGE = "Network error"
        const val INTERNAL_ERROR_MESSAGE = "Internal error"
        const val EVENT_UPDATE_MESSAGE = "New viewers counter update is available for this event."
        const val VIEWERS_COUNT_UPDATE_MESSAGE =
            "New viewers counter update is available for this event."
        const val TIMELINE_UPDATE_MESSAGE = "New timeline update is available for this event."

        const val ONE_SECOND_IN_MS = 1000L

        const val CAST_EVENT_ID_KEY = "eventId"
        const val CAST_PUBLIC_KEY_KEY = "publicKey"
        const val CAST_PSEUDO_USER_ID_KEY = "pseudoUserId"
        const val CAST_LICENSE_URL_KEY = "licenseUrl"
        const val CAST_PROTECTION_SYSTEM_KEY = "protectionSystem"

        const val DRM_WIDEVINE = "widevine"


        // developer messages
        const val PUBLIC_KEY_MUST_BE_SET_IN_MLS_BUILDER_MESSAGE = "Public key must be set!"
        const val ACTIVITY_IS_NOT_SET_IN_MLS_BUILDER_MESSAGE =
            "'Activity' should be provided before setting 'Ima'."
        const val AD_UNIT_MUST_START_WITH_SLASH_IN_MLS_BUILDER_MESSAGE =
            "Ad-unit provided must start with a forward-slash."


    }
}