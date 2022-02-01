package tv.mycujoo.domain.repository

import retrofit2.HttpException
import timber.log.Timber
import tv.mycujoo.mcls.network.NoConnectionInterceptor
import java.io.IOException

abstract class AbstractRepository {
    suspend fun <T> safeApiCall(apiCall: suspend () -> T): tv.mycujoo.domain.entity.Result<Exception, T> {

        return try {
            tv.mycujoo.domain.entity.Result.Success(apiCall.invoke())
        } catch (throwable: Throwable) {
            when (throwable) {
                is HttpException -> {
                    val code = throwable.code()
                    Timber.e(
                        "safeApiCall: $code \n\n\n" +
                                "${throwable.response()?.body()} \n" +
                                "${throwable.response()?.headers()} \n" +
                                "${throwable.response()?.raw()} \n"
                    )
                    return tv.mycujoo.domain.entity.Result.GenericError(
                        code,
                        throwable.message()
                    )

                }
                is IOException -> {
                    // Network Error
                    tv.mycujoo.domain.entity.Result.NetworkError(throwable)
                }
                is NoConnectionInterceptor.NoConnectivityException -> {
                    tv.mycujoo.domain.entity.Result.NetworkError(throwable)
                }
                else -> {
                    tv.mycujoo.domain.entity.Result.NetworkError(Exception(throwable))
                }
            }
        }
    }
}