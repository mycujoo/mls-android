package tv.mycujoo.mls.cast

import android.content.Context
import com.google.android.gms.cast.framework.CastOptions
import com.google.android.gms.cast.framework.OptionsProvider
import com.google.android.gms.cast.framework.SessionProvider
import com.google.android.gms.cast.framework.media.CastMediaOptions
import com.google.android.gms.cast.framework.media.NotificationOptions

/**
 * Extend this class to use MLS-Cast.
 * Activity name & Receiver App id must be provided.
 */
abstract class MLSCastOptionsProviderAbstract : OptionsProvider {

    abstract fun getActivityName(): String
    abstract fun getReceiverAppId(): String


    override fun getCastOptions(context: Context?): CastOptions {
        requireNotNull(context)
        val notificationOptions = NotificationOptions.Builder()
            .setTargetActivityClassName(getActivityName())
            .build()
        val mediaOptions = CastMediaOptions.Builder()
            .setNotificationOptions(notificationOptions)
            .setExpandedControllerActivityClassName(getActivityName())
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