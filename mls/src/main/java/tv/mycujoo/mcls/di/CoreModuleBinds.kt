package tv.mycujoo.mcls.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import tv.mycujoo.mcls.core.AnnotationFactory
import tv.mycujoo.mcls.core.AnnotationListener
import tv.mycujoo.mcls.core.IAnnotationFactory
import tv.mycujoo.mcls.core.IAnnotationListener
import tv.mycujoo.mcls.tv.player.TvAnnotationListener
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface CoreModuleBinds {

    @Binds
    @Singleton
    fun bindIAnnotationFactory(annotationFactory: AnnotationFactory): IAnnotationFactory

    @Binds
    @Singleton
    fun bindIAnnotationListener(annotationListener: AnnotationListener): IAnnotationListener

    @TV
    @Binds
    @Singleton
    fun bindTvAnnotationListener(annotationListener: TvAnnotationListener): IAnnotationListener
}