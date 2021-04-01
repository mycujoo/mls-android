package tv.mycujoo.mcls.model

import java.io.Serializable

data class MutablePair<A, B>(
    public var first: A,
    public var second: B
) : Serializable {

    /**
     * Returns string representation of the [Pair] including its [first] and [second] values.
     */
    public override fun toString(): String = "($first, $second)"
}
