package tv.mycujoo.mcls.cast

import android.content.Context
import com.google.android.gms.cast.framework.CastOptions
import com.google.android.gms.cast.framework.OptionsProvider
import com.google.android.gms.cast.framework.SessionProvider
import com.google.android.gms.cast.framework.media.CastMediaOptions

/**
 * Extend this class to use MLS-Cast.
 */
abstract class MLSCastOptionsProviderAbstract : OptionsProvider {

    override fun getCastOptions(context: Context): CastOptions {
        val mediaOptions = CastMediaOptions.Builder()
            .build()

        return CastOptions.Builder()
            .setReceiverApplicationId(context.getString(R.string.mls_cast_app_id))
            .setCastMediaOptions(mediaOptions)
            .build()
    }

    override fun getAdditionalSessionProviders(context: Context): MutableList<SessionProvider> {
        return mutableListOf()
    }
}