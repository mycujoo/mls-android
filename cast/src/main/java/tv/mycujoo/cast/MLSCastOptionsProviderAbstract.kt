package tv.mycujoo.cast

import android.content.Context
import com.google.android.gms.cast.framework.CastOptions
import com.google.android.gms.cast.framework.OptionsProvider
import com.google.android.gms.cast.framework.SessionProvider
import com.google.android.gms.cast.framework.media.CastMediaOptions

/**
 * Extend this class to use MLS-Cast.
 * Activity name & Receiver App id must be provided.
 */
abstract class MLSCastOptionsProviderAbstract : OptionsProvider {

    abstract fun getReceiverAppId(): String

    override fun getCastOptions(context: Context?): CastOptions {
        requireNotNull(context)
        val mediaOptions = CastMediaOptions.Builder()
            .build()

        val castOption = CastOptions.Builder()
            .setReceiverApplicationId(getReceiverAppId())
            .setCastMediaOptions(mediaOptions)
            .build()
        return castOption
    }

    override fun getAdditionalSessionProviders(context: Context?): MutableList<SessionProvider> {
        return mutableListOf()
    }
}