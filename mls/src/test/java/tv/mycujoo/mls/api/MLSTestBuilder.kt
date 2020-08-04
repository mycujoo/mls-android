package tv.mycujoo.mls.api

class MLSTestBuilder : MLSBuilder() {
    override fun build(): MLS {

        internalBuilder = InternalTestBuilder(activity!!)
        internalBuilder.initialize()

        val mls = MLS(this)
        mls.initialize(internalBuilder)
        return mls
    }


}