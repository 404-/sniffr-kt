package `fun`.notfound.sniffr.impls.drf

import `fun`.notfound.sniffr.*
import `fun`.notfound.sniffr.JsonLang.*
import `fun`.notfound.sniffr.Path.Segment.Field
import `fun`.notfound.sniffr.Path.Segment.Index
import `fun`.notfound.sniffr.impls.common.lang.Nodes
import `fun`.notfound.sniffr.utils.cons
import io.vavr.collection.*
import io.vavr.collection.List
import io.vavr.collection.Map
import kotlin.DeepRecursiveFunction as Drf

typealias Progress = Seq<Delta>
typealias Recurse<E> = Drf<DrfSniffr.Ctx<E>, Progress>

operator fun <E, L, O, A> Sniffr.Companion.invoke(
    lang: JsonLang<E, L, O, A>,
    strict: Boolean = true
): Sniffr<E> =
    DrfSniffr(lang, strict = strict)

class DrfSniffr<E, L, O, A>(
    private val lang: JsonLang<E, L, O, A>,
    private val strategies: Strategies<E>
) : Sniffr<E> {
    constructor(lang: JsonLang<E, L, O, A>, strict: Boolean = true) : this(
        lang = lang,
        strategies = Strategies<E>(strict)
    )

    class Strategies<E>(
        val objects: Strategy<Field, E>,
        val arrays: Strategy<Index, E>
    ) {
        constructor(strict: Boolean = true) : this(
            objects = Strategy.strict(Obj),
            arrays = if (strict) Strategy.strict(Arr) else Strategy.lenient(Arr)
        )

        companion object
    }

    interface Strategy<K: Path.Segment, E> {
        class Params<K: Path.Segment, E>(
            val path: Path,
            val lhs: Traversable<Pair<K, E>>,
            val rhs: Map<K, E>,
            val progress: Progress,
            val recurse: Recurse<E>
        ) { companion object }

        val diff: Drf<Params<K, E>, Progress>

        companion object
    }

    data class Ctx<E>(val path: Path, val lhs: E, val rhs: E, val progress: Progress) { companion object }

    override fun sniff(lhs: E, rhs: E): Seq<Delta> =
        sniff(Ctx(Path.root, lhs, rhs, List.empty())).reverse()

    private val sniff: Recurse<E> = Drf { ctx ->
        val (path, progress) = with(ctx) { path to progress }

        val lhs = lang.classify(ctx.lhs)
        val rhs = lang.classify(ctx.rhs)

        when {
            lhs is Lit && rhs is Lit -> leaf(path, lhs, rhs, progress)
            lhs is Obj && rhs is Obj -> down.callRecursive(Ctx(path, lhs, rhs, progress))
            lhs is Arr && rhs is Arr -> into.callRecursive(Ctx(path, lhs, rhs, progress))
            else                     -> Replace(path) cons progress
        }
    }

    private val nodes = Nodes(lang)

    private fun leaf(path: Path, lhs: Lit<L, O, A>, rhs: Lit<L, O, A>, progress: Progress): Progress {
        val delta = with(nodes) {
            if (lhs eq rhs) null else Replace(path)
        }
        return delta cons progress
    }

    private val down: Recurse<Obj<L, O, A>> = Drf { (path, lhs, rhs, progress) ->
        strategies.objects.diff.callRecursive(with(nodes) {
            Strategy.Params(
                path, lhs.decompose(), rhs.normalize(), progress, sniff
            )
        })
    }

    private val into: Recurse<Arr<L, O, A>> = Drf { (path, lhs, rhs, progress) ->
        strategies.arrays.diff.callRecursive(with(nodes) {
            Strategy.Params(
                path, lhs.decompose(), rhs.normalize(), progress, sniff
            )
        })
    }

    companion object
}
