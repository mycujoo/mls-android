package tv.mycujoo.mcls.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import tv.mycujoo.mcls.helper.DownloaderClient
import tv.mycujoo.mcls.helper.IDownloaderClient
import tv.mycujoo.mcls.helper.IOverlayFactory
import tv.mycujoo.mcls.helper.OverlayFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface HelperModule {

    @Binds
    @Singleton
    fun bindDownloadClient(downloadClient: DownloaderClient): IDownloaderClient

    @Binds
    @Singleton
    fun bindOverlayFactory(overlayFactory: OverlayFactory): IOverlayFactory
}