package tv.mycujoo.mcls.api

class MLSTestBuilder : MLSBuilder() {
    override fun build(): MLS {

        internalBuilder = InternalTestBuilder(activity!!)
        internalBuilder.initialize()

        val mls = MLS(this)
        mls.initializeComponent(internalBuilder)
        return mls
    }


}