package `fun`.notfound.sniffr

import arrow.core.Eval
import arrow.core.foldLeft
import `fun`.notfound.sniffr.JsonLang.*
import `fun`.notfound.sniffr.Path.Segment.Field
import `fun`.notfound.sniffr.Path.Segment.Index
import io.vavr.collection.*
import io.vavr.collection.List
import io.vavr.collection.Map

typealias Progress = Seq<Delta>
typealias Recurse<E> = (path: Path, lhs: E, rhs: E, progress: Progress) -> Eval<Progress>

class Sniffr<E, L, O, A>(
    private val lang: JsonLang<E, L, O, A>,
    private val strategies: Strategies
) {
    constructor(lang: JsonLang<E, L, O, A>, strict: Boolean = true) : this(
        lang = lang,
        strategies = Strategies(
            objects = Strategy.STRICT,
            arrays = if (strict) Strategy.STRICT else Strategy.LENIENT
        )
    )

    class Strategies(val objects: Strategy, val arrays: Strategy)

    interface Strategy {
        fun <K: Path.Segment, E> diff(
            path: Path,
            lhs: Traversable<Pair<K, E>>,
            rhs: Map<K, E>,
            progress: Progress,
            recurse: Recurse<E>
        ): Eval<Progress>

        companion object
    }

    fun sniff(lhs: E, rhs: E): Seq<Delta> =
        sniff(Path.root, lhs, rhs, List.empty()).value().reverse()

    private fun sniff(path: Path, lhs: E, rhs: E, progress: Progress): Eval<Progress> =
        sniff(path, lang.classify(lhs), lang.classify(rhs), progress)

    private fun sniff(path: Path, lhs: Node<L, O, A>, rhs: Node<L, O, A>, progress: Progress): Eval<Progress> =
        when {
            lhs is Lit && rhs is Lit -> leaf(path, lhs, rhs, progress)
            lhs is Obj && rhs is Obj -> down(path, lhs, rhs, progress)
            lhs is Arr && rhs is Arr -> into(path, lhs, rhs, progress)
            else                     -> Eval.now(Replace(path) cons progress)
        }

    private fun leaf(path: Path, lhs: Lit<L, O, A>, rhs: Lit<L, O, A>, progress: Progress): Eval<Progress> {
        val delta = if (lhs eq rhs) null else Replace(path)
        return Eval.now(delta cons progress)
    }

    private fun down(path: Path, lhs: Obj<L, O, A>, rhs: Obj<L, O, A>, progress: Progress): Eval<Progress> =
        strategies.objects.diff(path, lhs.decompose(), rhs.normalize(), progress, ::sniff)

    private fun into(path: Path, lhs: Arr<L, O, A>, rhs: Arr<L, O, A>, progress: Progress): Eval<Progress> =
        strategies.arrays.diff(path, lhs.decompose(), rhs.normalize(), progress, ::sniff)



    private infix fun Lit<L, O, A>.eq(that: Lit<L, O, A>) =
        lang.eq(lhs = this.v, rhs = that.v)

    private fun Obj<L, O, A>.normalize(): Map<Field, E> =
        fold(LinkedHashMap.empty(), Map<Field, E>::put)

    private fun Arr<L, O, A>.normalize(): Map<Index, E> =
        fold(LinkedHashMap.empty(), Map<Index, E>::put)

    private fun Obj<L, O, A>.decompose(): Traversable<Pair<Field, E>> =
        fold(Queue.empty()) { children, key, child -> children.append(key to child) }

    private fun Arr<L, O, A>.decompose(): Traversable<Pair<Index, E>> =
        fold(Queue.empty()) { children, key, child -> children.append(key to child) }

    private fun <R> Arr<L, O, A>.fold(zero: R, grow: (R, Index, E) -> R): R {
        val elements = lang.elementsOf(v)

        tailrec fun step(i: Int, res: R): R =
            if (i < elements.size)
                step(i + 1, grow(res, Index(i), elements[i]))
            else
                res

        return step(0, zero)
    }

    private fun <R> Obj<L, O, A>.fold(zero: R, grow: (R, Field, E) -> R): R =
        lang.childrenOf(v).foldLeft(zero) { children, (field, child) ->
            val key = Field(field)
            grow(children, key, child)
        }

    companion object
}
