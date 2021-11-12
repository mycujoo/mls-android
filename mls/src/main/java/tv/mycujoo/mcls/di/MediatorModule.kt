package tv.mycujoo.mcls.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import tv.mycujoo.mcls.mediator.AnnotationMediator
import tv.mycujoo.mcls.mediator.IAnnotationMediator
import tv.mycujoo.mcls.tv.player.TvAnnotationMediator
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface MediatorModule {

    @Singleton
    @Binds
    fun bindAnnotationMediator(annotationMediator: AnnotationMediator): IAnnotationMediator

    @Singleton
    @Binds
    @TV
    fun bindTvAnnotationMediator(annotationMediator: TvAnnotationMediator): IAnnotationMediator
}
