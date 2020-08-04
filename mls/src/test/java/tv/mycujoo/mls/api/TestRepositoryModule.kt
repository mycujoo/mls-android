package tv.mycujoo.mls.api

import org.mockito.Mockito.mock
import tv.mycujoo.domain.repository.EventsRepository
import tv.mycujoo.mls.di.RepositoryModule
import tv.mycujoo.mls.network.MlsApi

class TestRepositoryModule() : RepositoryModule() {

    var mockEventsRepository: EventsRepository = mock(EventsRepository::class.java)

    override fun provideEventsRepository(api: MlsApi): EventsRepository {
        return mockEventsRepository
    }

}