package tv.mycujoo.mcls.api

import org.mockito.Mockito.mock
import tv.mycujoo.domain.repository.EventsRepository
import tv.mycujoo.mcls.di.RepositoryModule
import tv.mycujoo.mcls.network.MlsApi

class TestRepositoryModule() : RepositoryModule() {

    var mockEventsRepository: EventsRepository = mock(EventsRepository::class.java)

    override fun provideEventsRepository(api: MlsApi): EventsRepository {
        return mockEventsRepository
    }

}