package tv.mycujoo.domain.repository

import tv.mycujoo.domain.entity.Result

interface AnnotationsRepository {
    suspend fun getSVG(): Result<Exception, ByteArray>
}