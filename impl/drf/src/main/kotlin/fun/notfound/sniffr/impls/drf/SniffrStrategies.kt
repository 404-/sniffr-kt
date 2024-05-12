package `fun`.notfound.sniffr.impls.drf

import `fun`.notfound.sniffr.*
import `fun`.notfound.sniffr.JsonLang.Arr
import `fun`.notfound.sniffr.JsonLang.Obj
import `fun`.notfound.sniffr.Path.Segment.Field
import `fun`.notfound.sniffr.Path.Segment.Index
import `fun`.notfound.sniffr.utils.cons
import `fun`.notfound.sniffr.utils.component1
import `fun`.notfound.sniffr.utils.component2
import io.vavr.Tuple2
import io.vavr.collection.List
import io.vavr.collection.Map
import io.vavr.collection.Seq
import kotlin.DeepRecursiveFunction as Drf
import kotlin.DeepRecursiveScope as Drs

fun <E> DrfSniffr.Strategy.Companion.strict(@Suppress("UNUSED_PARAMETER") obj: Obj.Companion): DrfSniffr.Strategy<Field, E> = Strict()
fun <E> DrfSniffr.Strategy.Companion.lenient(@Suppress("UNUSED_PARAMETER") obj: Obj.Companion): DrfSniffr.Strategy<Field, E> = Lenient()

fun <E> DrfSniffr.Strategy.Companion.strict(@Suppress("UNUSED_PARAMETER") arr: Arr.Companion): DrfSniffr.Strategy<Index, E> = Strict()
fun <E> DrfSniffr.Strategy.Companion.lenient(@Suppress("UNUSED_PARAMETER") arr: Arr.Companion): DrfSniffr.Strategy<Index, E> = Lenient()

private class Strict<K: Path.Segment, E> : DrfSniffr.Strategy<K, E> {
    override val diff = Drf { params: DrfSniffr.Strategy.Params<K, E> ->
        val (remainder, progress) = fold<Pair<K, E>, Pair<Map<K, E>, Progress>>(Drf { (state, input) ->
            val (candidates: Map<K, E>, curr: Progress) = state
            val (key: K, ideal: E) = input
            val next = diff(Strict, ideal, key, params.path, candidates, curr, params.recurse)

            candidates.remove(key) to next
        }).callRecursive(params.lhs to (params.rhs to params.progress))

        Insert.fold(params.path, progress, remainder)
    }

    companion object
}

private class Lenient<K: Path.Segment, E> : DrfSniffr.Strategy<K, E> {
    override val diff = Drf { params: DrfSniffr.Strategy.Params<K, E> ->
        val candidates = params.rhs

        val (keys, progress) = fold<Pair<K, E>, Pair<Seq<K>, Progress>>(Drf { (state, input) ->
            val (keys: Seq<K>, curr: Progress) = state
            val (key: K, ideal: E) = input

            val next = diff(ideal, origin = key, params.path, candidates, progress = curr, params.recurse)
            (key cons keys) to next
        }).callRecursive(params.lhs to (List.empty<K>() to params.progress))

        Insert.fold(params.path, progress, candidates.removeAll(keys))
    }

    private suspend fun <K: Path.Segment, E> Drs<*, *>.diff(
        ideal: E,
        origin: K,
        path: Path,
        candidates: Map<K, E>,
        progress: Progress,
        recurse: Recurse<E>
    ): Progress {

        val deltas = diff(Strict, ideal, key = origin, path, candidates, List.empty(), recurse)

        return if (deltas.isEmpty) progress else {
            /** skip diffing [origin] because the result is already available as [deltas] */
            val matched = search(ideal, path, candidates.remove(origin), recurse)

            when (matched) {
                null -> progress.prependAll(deltas)
                else -> Move(path, origin = origin, target = matched) cons progress
            }
        }
    }

    private suspend fun <K: Path.Segment, E> Drs<*, *>.search(
        ideal: E,
        path: Path,
        candidates: Map<K, E>,
        recurse: Recurse<E>
    ): K? =
        first<Tuple2<K, E>>(Drf { (key, candidate) ->
            val deltas = recurse.callRecursive(DrfSniffr.Ctx(
                path + key, ideal, candidate, List.empty()
            ))
            /** no differences signals a match between the current [candidate] and the [ideal] */
            deltas.isEmpty
        }).callRecursive(candidates)?.let { (matched: K, _: E) -> matched }

    companion object
}

private suspend fun <K: Path.Segment, E> Drs<*, *>.diff(
    @Suppress("UNUSED_PARAMETER") t: Strict.Companion,
    ideal: E,
    key: K,
    path: Path,
    candidates: Map<K, E>,
    progress: Progress,
    recurse: Recurse<E>
): Progress =
    candidates[key]
        .orNull
        ?.let { candidate ->
            recurse.callRecursive(DrfSniffr.Ctx(path + key, ideal, candidate, progress))
        }
        ?: (Delete(path + key) cons progress)

private fun <K: Path.Segment, E> Insert.Companion.fold(
    path: Path,
    zero: Progress,
    source: Map<K, E>
): Progress =
    source.foldLeft(zero) { progress, (key, _) ->
        val delta = Insert(path + key)
        delta cons progress
    }
