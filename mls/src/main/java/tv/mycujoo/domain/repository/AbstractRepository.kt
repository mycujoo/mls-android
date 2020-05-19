package tv.mycujoo.domain.repository

import retrofit2.HttpException
import java.io.IOException

abstract class AbstractRepository {
    suspend fun <T> safeApiCall(apiCall: suspend () -> T): tv.mycujoo.domain.entity.Result<Exception, T> {

        return try {
            tv.mycujoo.domain.entity.Result.Success(apiCall.invoke())
        } catch (throwable: Throwable) {
            when (throwable) {
                is IOException -> tv.mycujoo.domain.entity.Result.NetworkError(throwable)
                is HttpException -> {
                    val code = throwable.code()

                    return tv.mycujoo.domain.entity.Result.GenericError(
                        code,
                        throwable.message()
                    )

                }
                else -> {
                    tv.mycujoo.domain.entity.Result.NetworkError(Exception(throwable))
                }
            }
        }
    }
}