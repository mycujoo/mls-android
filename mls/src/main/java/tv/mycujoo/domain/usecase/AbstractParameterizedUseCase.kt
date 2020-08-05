package tv.mycujoo.domain.usecase

abstract class AbstractParameterizedUseCase<in P, out T> {

    abstract suspend fun build(param: P): T

    suspend fun execute(param: P): T = build(param)
}