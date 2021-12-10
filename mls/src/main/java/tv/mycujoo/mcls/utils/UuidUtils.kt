package tv.mycujoo.mcls.utils

import tv.mycujoo.mcls.enum.C
import tv.mycujoo.mcls.manager.IPrefManager
import java.util.*
import javax.inject.Inject

class UuidUtils @Inject constructor(
    val prefManager: IPrefManager
) {

    var savedUuid: String? = null

    /**
     * Gets a Null Safe, Always with a Uuid result
     *
     */
    fun getUuid(): String {
        // For Runtime Safety
        val resultUuid = savedUuid.orEmpty()

        return if (resultUuid.isEmpty()) {
            val newUuid = prefManager.get(C.UUID_PREF_KEY) ?: UUID.randomUUID().toString()
            savedUuid = newUuid
            persistUUIDIfNotStoredAlready(newUuid)
            newUuid
        } else {
            resultUuid
        }
    }

    /**
     * store UUID in shared pref, if it's NOT already stored
     * @param uuid user's unique identifier
     */
    private fun persistUUIDIfNotStoredAlready(uuid: String) {
        val storedUUID = prefManager.get(C.UUID_PREF_KEY)
        if (storedUUID == null) {
            prefManager.persist(C.UUID_PREF_KEY, uuid)
        }
    }
}