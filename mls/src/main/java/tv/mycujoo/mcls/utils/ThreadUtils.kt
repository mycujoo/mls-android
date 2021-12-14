package tv.mycujoo.mcls.utils

import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService

class ThreadUtils {
    fun getScheduledExecutorService(): ScheduledExecutorService {
        return Executors.newScheduledThreadPool(1)
    }

    fun provideHandler(): Handler {
        return Handler(Looper.myLooper() ?: Looper.getMainLooper())
    }
}