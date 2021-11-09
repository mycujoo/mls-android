package tv.mycujoo.mcls.api

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import org.mockito.Mockito.mock
import tv.mycujoo.domain.repository.EventsRepository
import tv.mycujoo.mcls.di.RepositoryModule
import tv.mycujoo.mcls.network.MlsApi

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [RepositoryModule::class]
)
class TestRepositoryModule {

    var mockEventsRepository: EventsRepository = mock(EventsRepository::class.java)

    fun provideEventsRepository(api: MlsApi): EventsRepository {
        return mockEventsRepository
    }

}