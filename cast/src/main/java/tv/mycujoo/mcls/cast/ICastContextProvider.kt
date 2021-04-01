package tv.mycujoo.mcls.cast

import com.google.android.gms.cast.framework.CastContext

interface ICastContextProvider {
    fun getCastContext(): CastContext
}