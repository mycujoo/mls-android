package tv.mycujoo.mcls.di

import dagger.Module
import dagger.Provides
import tv.mycujoo.domain.repository.EventsRepository
import tv.mycujoo.mcls.network.MlsApi
import javax.inject.Singleton

@Module
open class RepositoryModule {

    @Provides
    @Singleton
    open fun provideEventsRepository(api: MlsApi): EventsRepository {
        return tv.mycujoo.data.repository.EventsRepository(api)
    }
}