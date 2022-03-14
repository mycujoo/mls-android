package tv.mycujoo.mcls.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class CountingIdlingResourceViewIdentifierManager

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class PublicBaseUrl

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class ApiBaseUrl

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class PublicApi

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class MLSAPI

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class TV

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class ReactorUrl

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class ConcurrencySocketUrl
