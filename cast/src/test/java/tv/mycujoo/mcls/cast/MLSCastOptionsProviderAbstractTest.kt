package tv.mycujoo.mcls.cast

import org.junit.Before

class MLSCastOptionsProviderAbstractTest {

    private lateinit var castOptionsProvider: EmptyMLSCastOptionsProvider

    @Before
    fun setUp() {
        castOptionsProvider = EmptyMLSCastOptionsProvider()
    }
}