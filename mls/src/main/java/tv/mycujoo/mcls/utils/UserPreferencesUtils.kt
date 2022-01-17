package tv.mycujoo.mcls.utils

import tv.mycujoo.mcls.enum.C
import tv.mycujoo.mcls.manager.IPrefManager
import javax.inject.Inject

class UserPreferencesUtils @Inject constructor(
    val prefManager: IPrefManager
) {

    private var mPseudoUserId: String? = null
    private var loggedInUserId: String? = null

    /**
     * Generate Pseudo User Id when not persisted, and persist one if  not exist
     */
    fun getPseudoUserId() = mPseudoUserId ?: run {
        val storedId = prefManager.get(C.PSEUDO_USER_ID_PREF_KEY)
        if (storedId != null) {
            storedId
        } else {
            val id = generatePseudoUserId()
            prefManager.persist(C.PSEUDO_USER_ID_PREF_KEY, id)
            id
        }
    }

    fun setPseudoUserId(pseudoUserId: String) {
        prefManager.persist(C.PSEUDO_USER_ID_PREF_KEY, pseudoUserId)
        this.mPseudoUserId = pseudoUserId
    }

    fun getLoggedInUserId() = loggedInUserId ?: prefManager.get(C.USER_ID_PREF_KEY)

    fun setLoggedInUserId(userId: String) {
        prefManager.persist(C.USER_ID_PREF_KEY, userId)
        loggedInUserId = userId
    }

    /**
     * Random User Id Generator
     */
    private fun generatePseudoUserId(): String {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        val length = 26
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }
}
