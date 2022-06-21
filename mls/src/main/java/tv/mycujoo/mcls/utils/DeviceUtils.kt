package tv.mycujoo.mcls.utils

import android.content.Context
import android.os.Build
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import tv.mycujoo.mcls.enum.DeviceType

object DeviceUtils {
    fun detectTVDeviceType(context: Context): DeviceType {
        // Check if Google Play Services is Supported
        val isGooglePlayDevice = GoogleApiAvailability
            .getInstance()
            .isGooglePlayServicesAvailable(context)

        if (isGooglePlayDevice == ConnectionResult.SUCCESS) {
            return DeviceType.ANDROID_TV
        }

        if (Build.MODEL.contains("AFT", true)) {
            return DeviceType.FIRE_TV
        }

        return DeviceType.AOSP
    }
}