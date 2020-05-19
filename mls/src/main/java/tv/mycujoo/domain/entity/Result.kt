package tv.mycujoo.domain.entity

sealed class Result<out E, out V> {

    data class Success<out V>(val value: V) : Result<Nothing, V>()
    data class NetworkError<out E>(val error: E) : Result<E, Nothing>()
    data class GenericError(val errorCode: Int, val errorMessage: String) : Result<Nothing, Nothing>()

}