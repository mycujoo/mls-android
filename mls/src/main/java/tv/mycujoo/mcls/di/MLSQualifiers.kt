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
annotation class EventsApiBaseUrl

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class TimelineApiBaseUrl

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class PublicApi

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class MLSEventsApi

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class MLSTimelineApi

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class TV

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class ReactorUrl

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class ConcurrencySocketUrl

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class ExoPlayerOkHttp

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class ClientDeviceType

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class YouboraAccountCode
