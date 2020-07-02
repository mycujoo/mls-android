package tv.mycujoo.domain.usecase

import tv.mycujoo.domain.repository.AnnotationsRepository

class GetSVGUseCase(private val annotationsRepository: AnnotationsRepository) :
    AbstractUseCase<tv.mycujoo.domain.entity.Result<Exception, ByteArray>>() {
    override suspend fun build(): tv.mycujoo.domain.entity.Result<Exception, ByteArray> {
        return annotationsRepository.getSVG()
    }
}