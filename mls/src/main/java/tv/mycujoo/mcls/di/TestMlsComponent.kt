package tv.mycujoo.mcls.di

import dagger.Component
import javax.inject.Singleton

/**
 * MLS Test component for injecting fakes while testing
 */
@Component(modules = [AppModule::class, NetworkModule::class, RepositoryModule::class])
@Singleton
interface TestMlsComponent : MlsComponent {
}