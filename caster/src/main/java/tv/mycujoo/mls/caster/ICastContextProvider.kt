package tv.mycujoo.mls.caster

import com.google.android.gms.cast.framework.CastContext

interface ICastContextProvider {
    fun getCastContext(): CastContext
}