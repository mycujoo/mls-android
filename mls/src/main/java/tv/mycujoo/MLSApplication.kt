package tv.mycujoo

import android.app.Application
import androidx.annotation.CallSuper
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.android.internal.managers.ApplicationComponentManager
import dagger.hilt.android.internal.modules.ApplicationContextModule
import dagger.hilt.internal.UnsafeCasts

@HiltAndroidApp
class MLSApplication : Application()
