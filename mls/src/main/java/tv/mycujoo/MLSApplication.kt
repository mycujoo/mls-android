package tv.mycujoo

import android.app.Application
import androidx.annotation.CallSuper
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.android.internal.managers.ApplicationComponentManager
import dagger.hilt.android.internal.modules.ApplicationContextModule
import dagger.hilt.internal.UnsafeCasts

/**
 *
 * This is used as an entry point to create the dependency graph tree.
 * SingletonComponent get created from here
 *
 */

@HiltAndroidApp
class MLSApplication : Application()
