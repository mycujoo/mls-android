package tv.mycujoo.mls.di

import dagger.Component
import javax.inject.Singleton

@Component(modules = [AppModule::class, NetworkModule::class, RepositoryModule::class])
@Singleton
interface TestMlsComponent : MlsComponent {
}