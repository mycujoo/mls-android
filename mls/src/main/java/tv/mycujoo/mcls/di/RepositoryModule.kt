package tv.mycujoo.mcls.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import tv.mycujoo.domain.repository.EventsRepository
import tv.mycujoo.mcls.network.MlsApi
import javax.inject.Singleton

/**
 * Provides Repositories to be used with DI.
 * EventsRepository is provided by this module
 * @see EventsRepository
 */
@Module
@InstallIn(SingletonComponent::class)
open class RepositoryModule {

    @Provides
    @Singleton
    open fun provideEventsRepository(api: MlsApi): EventsRepository {
        return tv.mycujoo.data.repository.EventsRepository(api)
    }
}