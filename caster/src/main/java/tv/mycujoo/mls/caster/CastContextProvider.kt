package tv.mycujoo.mls.caster

import android.content.Context
import com.google.android.gms.cast.framework.CastContext

class CastContextProvider(val context: Context) : ICastContextProvider {
    override fun getCastContext(): CastContext {
        return CastContext.getSharedInstance(context)
    }
}