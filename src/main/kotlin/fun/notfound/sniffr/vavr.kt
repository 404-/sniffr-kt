package `fun`.notfound.sniffr

import arrow.eval.Eval
import io.vavr.Tuple2
import io.vavr.collection.LinkedHashMap
import io.vavr.collection.Map
import io.vavr.collection.Seq
import io.vavr.collection.Traversable

internal operator fun <A, B> Tuple2<A, B>.component1(): A = _1
internal operator fun <A, B> Tuple2<A, B>.component2(): B = _2

internal infix fun <E, C: Seq<E>> E?.cons(tail: C): C =
    if (this == null) tail else tail.prepend(this) as C

internal inline fun <E, C: Traversable<E>, R> C.uncons(crossinline f: (E, C) -> R): R? =
    headOption()!!.orNull?.let { head -> f(head, tail() as C) }

internal inline fun <E, C: Traversable<E>, R> Eval.Companion.uncons(
    c: C,
    crossinline f: (E, C) -> Eval<R>
): Eval<R>? =
    c.uncons { head, tail ->
        defer {
            f(head, tail)
        }
    }

internal fun <E, C: Traversable<E>, R> Eval.Companion.fold(
    elements: C,
    zero: R,
    plus: (R, E, C) -> Eval<Pair<R, C>>
): Eval<R> =
    uncons(elements) { head, tail ->
        plus(zero, head, tail).flatMap { (result, tail) ->
            fold(tail, result, plus)
        }
    }
        ?: now(zero)

internal inline fun <E, R> Eval.Companion.fold(
    elements: Traversable<E>,
    zero: R,
    crossinline plus: (R, E) -> Eval<R>
): Eval<R> = fold(elements, zero) { result, head, tail ->
    plus(result, head).map { it to tail }
}

internal inline fun <K, V, R> Eval.Companion.get(
    key: K,
    map: Map<K, V>,
    crossinline f: (V) -> Eval<R>
): Eval<R>? =
    map[key].orNull?.let { v ->
        defer { f(v) }
    }

internal inline fun <K, V> Eval.Companion.find(
    source: Map<K, V>,
    crossinline test: (K, V) -> Eval<Boolean>
): Eval<K?> =
    fold(source, null as K?) { _, (k, v), candidates ->
        test(k, v).map { matched ->
            if (matched)
                k to LinkedHashMap.empty()
            else
                null to candidates
        }
    }
