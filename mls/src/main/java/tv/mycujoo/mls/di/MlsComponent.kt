package tv.mycujoo.mls.di

import dagger.Component
import javax.inject.Singleton

@Component(modules = [NetworkModule::class])
@Singleton
interface MlsComponent {

}