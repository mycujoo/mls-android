package tv.mycujoo.mcls.tv.api

import android.content.Context
import tv.mycujoo.mcls.data.IDataManager
import tv.mycujoo.mcls.enum.C
import tv.mycujoo.mcls.manager.IPrefManager
import javax.inject.Inject

class HeadlessMLS @Inject constructor(
    val context: Context,
    private val dataManager: IDataManager,
    private val prefManager: IPrefManager
) {

    fun getDataManager(): IDataManager {
        return dataManager
    }

    fun setIdentityToken(identityToken: String) {
        prefManager.persist(C.IDENTITY_TOKEN_PREF_KEY, identityToken)
    }

    fun removeIdentityToken() {
        prefManager.delete(C.IDENTITY_TOKEN_PREF_KEY)
    }
}