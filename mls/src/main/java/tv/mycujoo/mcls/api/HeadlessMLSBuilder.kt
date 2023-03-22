package tv.mycujoo.mcls.api

import android.content.Context
import android.content.pm.PackageManager
import dagger.BindsInstance
import dagger.Component
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.newSingleThreadContext
import timber.log.Timber
import tv.mycujoo.mcls.BuildConfig
import tv.mycujoo.mcls.di.CoreModule
import tv.mycujoo.mcls.di.NetworkModule
import tv.mycujoo.mcls.di.NetworkModuleBinds
import tv.mycujoo.mcls.di.StorageModule
import tv.mycujoo.mcls.enum.C
import tv.mycujoo.mcls.enum.LogLevel
import tv.mycujoo.mcls.manager.IPrefManager
import tv.mycujoo.mcls.tv.api.HeadlessMLS
import tv.mycujoo.ui.MLSTVFragment
import javax.inject.Inject
import javax.inject.Singleton

class HeadlessMLSBuilder {
    init {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    @Inject
    lateinit var prefManager: IPrefManager

    @Inject
    lateinit var headlessMLS: HeadlessMLS

    internal lateinit var mlsTvFragment: MLSTVFragment

    internal var publicKey: String = ""
        private set
    internal var identityToken: String = ""
        private set
    internal var logLevel: LogLevel = LogLevel.MINIMAL
        private set
    internal var coroutineScope: CoroutineScope? = null

    fun publicKey(publicKey: String) = apply {
        if (publicKey == "YOUR_PUBLIC_KEY_HERE") {
            throw IllegalArgumentException(C.PUBLIC_KEY_MUST_BE_SET_IN_MLS_BUILDER_MESSAGE)
        }
        this.publicKey = publicKey
    }

    fun identityToken(identityToken: String) = apply {
        this.identityToken = identityToken
    }

    fun coroutineScope(coroutineScope: CoroutineScope) = apply {
        this.coroutineScope = coroutineScope
    }

    /**
     * init public key if not present
     */
    protected fun initPublicKeyIfNeeded() {
        // grab public key from Manifest if not set manually,
        if (publicKey.isEmpty()) {
            mlsTvFragment.requireActivity().applicationContext.let {
                val app = it?.packageManager?.getApplicationInfo(
                    it.packageName,
                    PackageManager.GET_META_DATA
                )
                publicKey = app?.metaData?.getString("tv.mycujoo.MLS_PUBLIC_KEY") ?: ""
            }
        }
    }

    fun setLogLevel(logLevel: LogLevel) = apply { this.logLevel = logLevel }

    // Headless is a client without UI elements in it.
    @OptIn(DelicateCoroutinesApi::class)
    fun build(context: Context): HeadlessMLS {
        initPublicKeyIfNeeded()
        if (publicKey.isEmpty()) {
            throw IllegalArgumentException(C.PUBLIC_KEY_MUST_BE_SET_IN_MLS_BUILDER_MESSAGE)
        }

        val coroutineScope = coroutineScope
        val scope = if (coroutineScope == null) {
            val job = SupervisorJob()
            CoroutineScope(newSingleThreadContext(BuildConfig.LIBRARY_PACKAGE_NAME) + job)
        } else {
            coroutineScope
        }

        DaggerHeadlessMLSComponent
            .builder()
            .bindContext(context)
            .bindCoroutinesScope(scope)
            .build()
            .inject(this)

        prefManager.persist(C.IDENTITY_TOKEN_PREF_KEY, identityToken)
        prefManager.persist(C.PUBLIC_KEY_PREF_KEY, publicKey)

        return headlessMLS
    }
}

@Singleton
@Component(
    modules = [
        StorageModule::class,
        NetworkModuleBinds::class,
        NetworkModule::class,
        CoreModule::class
    ]
)
interface HeadlessMLSComponent {
    @Component.Builder
    interface Builder {
        @BindsInstance
        fun bindContext(context: Context): Builder

        @BindsInstance
        fun bindCoroutinesScope(coroutineScope: CoroutineScope): Builder

        fun build(): HeadlessMLSComponent
    }

    fun inject(builder: HeadlessMLSBuilder)
}