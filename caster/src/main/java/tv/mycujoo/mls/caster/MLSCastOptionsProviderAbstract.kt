package tv.mycujoo.mls.caster

import android.content.Context
import com.google.android.gms.cast.framework.CastOptions
import com.google.android.gms.cast.framework.OptionsProvider
import com.google.android.gms.cast.framework.SessionProvider
import com.google.android.gms.cast.framework.media.CastMediaOptions

/**
 * Extend this class to use MLS-Cast.
 */
abstract class MLSCastOptionsProviderAbstract : OptionsProvider {

    override fun getCastOptions(context: Context?): CastOptions {
        requireNotNull(context)
        val mediaOptions = CastMediaOptions.Builder()
            .build()

        val castOption = CastOptions.Builder()
            .setReceiverApplicationId(context.getString(R.string.mls_cast_app_id))
            .setCastMediaOptions(mediaOptions)
            .build()
        return castOption
    }

    override fun getAdditionalSessionProviders(context: Context?): MutableList<SessionProvider> {
        return mutableListOf()
    }
}