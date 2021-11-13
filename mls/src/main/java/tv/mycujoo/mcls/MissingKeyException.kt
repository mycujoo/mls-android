package tv.mycujoo.mcls

import java.lang.Exception

class MissingKeyException : Exception() {
    override fun toString(): String {
        return "Missing Key, Please see https://github.com/mycujoo/mls-android"
    }
}
