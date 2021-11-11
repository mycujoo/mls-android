package tv.mycujoo.mcls.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import tv.mycujoo.mcls.helper.DownloaderClient
import tv.mycujoo.mcls.helper.IDownloaderClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface HelperModule {

    @Binds
    @Singleton
    fun bindDownloadClient(downloadClient: DownloaderClient): IDownloaderClient
}