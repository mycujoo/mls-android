package tv.mycujoo.mls.manager

import android.util.Log
import tv.mycujoo.mls.enum.LogLevel
import tv.mycujoo.mls.enum.LogLevel.*
import tv.mycujoo.mls.enum.MessageLevel

class Logger(private var logLevel: LogLevel) {

    fun setLogLevel(logLevel: LogLevel) {
        this.logLevel = logLevel
    }

    fun log(messageLevel: MessageLevel, message: String?) {
        if (message == null) {
            return
        }
        when (logLevel) {
            MINIMAL -> {
                // do nothing
            }
            INFO -> {
                when (messageLevel) {
                    MessageLevel.VERBOSE,
                    MessageLevel.DEBUG -> {
                        // do nothing
                    }
                    MessageLevel.INFO -> {
                        Log.i("MLS-SDK", message)

                    }
                    MessageLevel.WARNING -> {
                        Log.w("MLS-SDK", message)

                    }
                    MessageLevel.ERROR -> {
                        Log.e("MLS-SDK", message)
                    }
                }
            }
            VERBOSE -> {
                when (messageLevel) {
                    MessageLevel.VERBOSE -> {
                        Log.v("MLS-SDK", message)
                    }
                    MessageLevel.DEBUG -> {
                        Log.d("MLS-SDK", message)
                    }
                    MessageLevel.INFO -> {
                        Log.i("MLS-SDK", message)

                    }
                    MessageLevel.WARNING -> {
                        Log.w("MLS-SDK", message)

                    }
                    MessageLevel.ERROR -> {
                        Log.e("MLS-SDK", message)
                    }
                }
            }
        }
    }
}