package tv.mycujoo

import tv.mycujoo.mcls.api.MLS
import tv.mycujoo.mcls.enum.C
import tv.mycujoo.mcls.tv.api.MLSTV
import tv.mycujoo.mcls.tv.api.MLSTvBuilder
import javax.inject.Inject

class MLSTVTestBuilder @Inject constructor(
    private val mlstv: MLSTV
) : MLSTvBuilder() {

    override fun build(): MLSTV {
        initPublicKeyIfNeeded()
        if (publicKey.isEmpty()) {
            throw IllegalArgumentException(C.PUBLIC_KEY_MUST_BE_SET_IN_MLS_BUILDER_MESSAGE)
        }

        initYouboraPlugin()

        mlstv.initialize(this, mlsTvFragment)

        return mlstv
    }
}