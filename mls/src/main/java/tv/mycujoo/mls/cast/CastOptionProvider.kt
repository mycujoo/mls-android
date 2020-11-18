package tv.mycujoo.mls.cast

import android.content.Context
import com.google.android.gms.cast.framework.CastOptions
import com.google.android.gms.cast.framework.OptionsProvider
import com.google.android.gms.cast.framework.SessionProvider
import com.google.android.gms.cast.framework.media.CastMediaOptions
import com.google.android.gms.cast.framework.media.NotificationOptions
import tv.mycujoo.mls.BlankActivity

class CastOptionProvider() :
    OptionsProvider {
    private val activityName: String = BlankActivity::javaClass.name
    private val receiverAppId: String = "25B901A3"

    override fun getCastOptions(context: Context?): CastOptions {
        requireNotNull(context)
        val notificationOptions = NotificationOptions.Builder()
            .setTargetActivityClassName(activityName)
            .build()
        val mediaOptions = CastMediaOptions.Builder()
            .setNotificationOptions(notificationOptions)
            .setExpandedControllerActivityClassName(activityName)
            .build()

        val castOption = CastOptions.Builder()
            .setReceiverApplicationId(receiverAppId)
            .setCastMediaOptions(mediaOptions)
            .build()
        return castOption
    }

    override fun getAdditionalSessionProviders(context: Context?): MutableList<SessionProvider> {
        return emptyList<SessionProvider>().toMutableList()
    }
}