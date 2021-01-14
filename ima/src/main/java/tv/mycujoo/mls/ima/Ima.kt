package tv.mycujoo.mls.ima

class Ima(private val adUnit: String) : IIma {

    override fun getAdUnit(): String {
        return adUnit
    }
}