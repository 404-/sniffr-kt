package `fun`.notfound.sniffr

import arrow.core.Eval
import io.vavr.collection.List
import io.vavr.collection.Map
import io.vavr.collection.Traversable

val Sniffr.Strategy.Companion.STRICT: Sniffr.Strategy get() = Strict
val Sniffr.Strategy.Companion.LENIENT: Sniffr.Strategy get() = Lenient

private object Strict : Sniffr.Strategy {
    override fun <K: Path.Segment, E> diff(
        path: Path,
        lhs: Traversable<Pair<K, E>>,
        rhs: Map<K, E>,
        progress: Progress,
        recurse: Recurse<E>
    ): Eval<Progress> =
        Eval
            .fold(lhs, rhs to progress) { (rhs, progress), (key, el) ->
                diff(path, key, el, rhs, progress, recurse).map { progress ->
                    rhs.remove(key) to progress
                }
            }
            .map { (remainder, progress) ->
                Insert.fold(path, progress, remainder)
            }

    inline fun <K: Path.Segment, E> diff(
        path: Path,
        key: K,
        ideal: E,
        candidates: Map<K, E>,
        progress: Progress,
        crossinline recurse: Recurse<E>
    ): Eval<Progress> =
        Eval
            .get(key, candidates) { candidate ->
                recurse(path + key, ideal, candidate, progress)
            }
            ?: Eval.now(Delete(path + key) cons progress)
}

private object Lenient : Sniffr.Strategy {
    override fun <K: Path.Segment, E> diff(
        path: Path,
        lhs: Traversable<Pair<K, E>>,
        rhs: Map<K, E>,
        progress: Progress,
        recurse: Recurse<E>
    ): Eval<Progress> =
        Eval
            .fold(lhs, List.empty<K>() to progress) { (keys, progress), (key, el) ->
                diff(path, key, el, rhs, progress, recurse).map { progress ->
                    (key cons keys) to progress
                }
            }
            .map { (keys, progress) ->
                Insert.fold(path, progress, rhs.removeAll(keys))
            }

    private inline fun <K: Path.Segment, E> diff(
        path: Path,
        origin: K,
        ideal: E,
        candidates: Map<K, E>,
        progress: Progress,
        crossinline recurse: Recurse<E>
    ): Eval<Progress> =
        Strict.diff(path, origin, ideal, candidates, List.empty(), recurse).flatMap { differences ->
            val same = differences.isEmpty
            if (same)
                Eval.now(progress)
            else
                /** skip diffing [origin] because the result is already available as [differences] */
                search(path, skip = origin, ideal, candidates, equality(recurse)).map { matched ->
                    when (matched) {
                        null -> progress.prependAll(differences)
                        else -> Move(path, origin = origin, target = matched) cons progress
                    }
                }
        }

    private inline fun <K: Path.Segment, E> search(
        path: Path,
        skip: K,
        ideal: E,
        candidates: Map<K, E>,
        crossinline same: (Path, E, E) -> Eval<Boolean>
    ): Eval<K?> =
        Eval.find(candidates) { key, candidate ->
            if (key == skip)
                Eval.now(false)
            else
                same(path + key, ideal, candidate)
        }

    /** convert [diff] into a simple equality checker */
    private inline fun <E> equality(crossinline diff: Recurse<E>): (Path, E, E) -> Eval<Boolean> =
        { path, lhs, rhs ->
            diff(path, lhs, rhs, List.empty()).map { it.isEmpty }
        }
}

private fun <K: Path.Segment, E> Insert.Companion.fold(
    path: Path,
    zero: Progress,
    source: Map<K, E>
): Progress =
    source.foldLeft(zero) { progress, (key, _) ->
        val delta = Insert(path + key)
        delta cons progress
    }
