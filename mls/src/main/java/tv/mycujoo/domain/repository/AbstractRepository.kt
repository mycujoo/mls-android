package tv.mycujoo.domain.repository

import android.util.Log
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
                    Log.e(
                        TAG,
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
                else -> {
                    tv.mycujoo.domain.entity.Result.NetworkError(Exception(throwable))
                }
            }
        }
    }

    companion object {
        private const val TAG = "AbstractRepository"
    }
}