package tv.mycujoo

import tv.mycujoo.mcls.api.MLS
import tv.mycujoo.mcls.api.MLSBuilder
import tv.mycujoo.mcls.enum.C
import javax.inject.Inject

class MLSTestBuilder @Inject constructor(
    private val mls: MLS
) : MLSBuilder() {

    override fun build(): MLS {
        initPublicKeyIfNeeded()
        if (publicKey.isEmpty()) {
            throw IllegalArgumentException(C.PUBLIC_KEY_MUST_BE_SET_IN_MLS_BUILDER_MESSAGE)
        }

        initYouboraPlugin()

        mls.initializeComponent(this)

        return mls
    }
}