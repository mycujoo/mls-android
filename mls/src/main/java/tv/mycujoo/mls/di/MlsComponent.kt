package tv.mycujoo.mls.di

import dagger.Component
import tv.mycujoo.mls.api.MLS
import javax.inject.Singleton

@Component(modules = [AppModule::class, NetworkModule::class, RepositoryModule::class])
@Singleton
interface MlsComponent {
    fun inject(MLS: MLS)

}