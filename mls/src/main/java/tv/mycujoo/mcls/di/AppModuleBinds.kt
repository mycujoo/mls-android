package tv.mycujoo.mcls.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import tv.mycujoo.data.repository.EventsRepository
import tv.mycujoo.domain.repository.IEventsRepository
import tv.mycujoo.mcls.core.AnnotationFactory
import tv.mycujoo.mcls.core.AnnotationListener
import tv.mycujoo.mcls.core.IAnnotationFactory
import tv.mycujoo.mcls.core.IAnnotationListener
import tv.mycujoo.mcls.helper.DownloaderClient
import tv.mycujoo.mcls.helper.IDownloaderClient
import tv.mycujoo.mcls.helper.IOverlayFactory
import tv.mycujoo.mcls.helper.OverlayFactory
import tv.mycujoo.mcls.manager.IVariableKeeper
import tv.mycujoo.mcls.manager.VariableKeeper
import tv.mycujoo.mcls.manager.ViewHandler
import tv.mycujoo.mcls.manager.contracts.IViewHandler
import tv.mycujoo.mcls.mediator.AnnotationMediator
import tv.mycujoo.mcls.mediator.IAnnotationMediator
import tv.mycujoo.mcls.network.socket.IReactorSocket
import tv.mycujoo.mcls.network.socket.ReactorSocket
import tv.mycujoo.mcls.player.IPlayer
import tv.mycujoo.mcls.player.Player
import tv.mycujoo.mcls.tv.player.TvAnnotationListener
import tv.mycujoo.mcls.tv.player.TvAnnotationMediator
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
interface AppModuleBinds {

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


    @Binds
    @Singleton
    fun bindDownloadClient(downloadClient: DownloaderClient): IDownloaderClient

    @Binds
    @Singleton
    fun bindOverlayFactory(overlayFactory: OverlayFactory): IOverlayFactory

    @Singleton
    @Binds
    fun bindAnnotationMediator(annotationMediator: AnnotationMediator): IAnnotationMediator

    @Singleton
    @Binds
    @TV
    fun bindTvAnnotationMediator(annotationMediator: TvAnnotationMediator): IAnnotationMediator

    @Binds
    @Singleton
    fun bindViewHandler(viewHandler: ViewHandler): IViewHandler

    @Binds
    @Singleton
    fun bindIVariableKeeper(variableKeeper: VariableKeeper): IVariableKeeper

    @Binds
    @Singleton
    fun bindReactorSocket(ReactorSocket: ReactorSocket): IReactorSocket

    @Binds
    @Singleton
    fun bindEventsRepository(EventsRepository: EventsRepository): IEventsRepository

    @Binds
    @Singleton
    fun bindPlayer(player: Player): IPlayer
}