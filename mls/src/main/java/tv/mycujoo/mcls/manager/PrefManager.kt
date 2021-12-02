package tv.mycujoo.mcls.manager

import android.content.SharedPreferences
import javax.inject.Inject

class PrefManager @Inject constructor(private val sharedPreferences: SharedPreferences) : IPrefManager {
    override fun persist(key: String, value: String) {
        sharedPreferences.edit().putString(key, value).apply()
    }

    override fun get(key: String): String? {
        return sharedPreferences.getString(key, null)
    }

    override fun delete(key: String) {
        val editor = sharedPreferences.edit()
        editor.remove(key)
        editor.apply()
    }
}

interface IPrefManager {
    fun persist(key: String, value: String)
    fun get(key: String): String?
    fun delete(key: String)
}
