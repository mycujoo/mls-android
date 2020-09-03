package tv.mycujoo.mls.di

import dagger.Component
import tv.mycujoo.mls.api.MLS
import tv.mycujoo.mls.api.MLSBuilder
import tv.mycujoo.mls.core.InternalBuilder
import tv.mycujoo.mls.tv.api.MLSTvInternalBuilder
import javax.inject.Singleton

@Component(modules = [AppModule::class, NetworkModule::class, RepositoryModule::class])
@Singleton
interface MlsComponent {
    fun inject(MLS: MLS)
    fun inject(MLS: MLSBuilder)
    fun inject(internalBuilder: InternalBuilder)
    fun inject(mlsTvInternalBuilder: MLSTvInternalBuilder)

}