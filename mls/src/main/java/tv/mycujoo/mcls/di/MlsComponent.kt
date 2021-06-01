package tv.mycujoo.mcls.di

import dagger.Component
import tv.mycujoo.mcls.api.MLS
import tv.mycujoo.mcls.api.MLSBuilder
import tv.mycujoo.mcls.core.InternalBuilder
import tv.mycujoo.mcls.tv.api.MLSTvInternalBuilder
import javax.inject.Singleton

/**
 * MLS dependency graph component
 */
@Component(modules = [AppModule::class, NetworkModule::class, RepositoryModule::class])
@Singleton
interface MlsComponent {
    fun inject(MLS: MLS)
    fun inject(MLS: MLSBuilder)
    fun inject(internalBuilder: InternalBuilder)
    fun inject(mlsTvInternalBuilder: MLSTvInternalBuilder)

}