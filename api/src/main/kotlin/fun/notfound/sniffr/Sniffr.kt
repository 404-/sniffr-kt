package `fun`.notfound.sniffr

import io.vavr.collection.Seq

interface Sniffr<E> {
    fun sniff(lhs: E, rhs: E): Seq<Delta>

    companion object
}
