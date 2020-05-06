package tv.mycujoo.mls.model

import android.content.res.Configuration

data class ConfigParams(
    val config: Configuration,
    val portraitActionBar: Boolean,
    val landscapeActionBar: Boolean
)