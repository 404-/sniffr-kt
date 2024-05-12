package `fun`.notfound.sniffr.utils

import io.vavr.Tuple2
import io.vavr.collection.Seq

operator fun <A, B> Tuple2<A, B>.component1(): A = _1
operator fun <A, B> Tuple2<A, B>.component2(): B = _2

@Suppress("UNCHECKED_CAST")
infix fun <E, C: Seq<E>> E?.cons(tail: C): C =
    if (this == null) tail else tail.prepend(this) as C


