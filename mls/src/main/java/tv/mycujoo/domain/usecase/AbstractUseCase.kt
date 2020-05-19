package tv.mycujoo.domain.usecase

abstract class AbstractUseCase<out T> {

    abstract suspend fun build(): T

    suspend fun execute(): T = build()
}