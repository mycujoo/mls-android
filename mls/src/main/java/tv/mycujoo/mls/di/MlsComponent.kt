package tv.mycujoo.mls.di

import dagger.Component
import tv.mycujoo.mls.api.MyCujooLiveService
import javax.inject.Singleton

@Component(modules = [AppModule::class, NetworkModule::class, RepositoryModule::class])
@Singleton
interface MlsComponent {
    fun inject(myCujooLiveService: MyCujooLiveService)

}