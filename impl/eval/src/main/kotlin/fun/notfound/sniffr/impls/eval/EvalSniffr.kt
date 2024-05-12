package `fun`.notfound.sniffr.impls.eval

import arrow.eval.Eval
import `fun`.notfound.sniffr.*
import `fun`.notfound.sniffr.JsonLang.*
import `fun`.notfound.sniffr.impls.common.lang.Nodes
import `fun`.notfound.sniffr.utils.cons
import io.vavr.collection.*
import io.vavr.collection.List
import io.vavr.collection.Map

typealias Progress = Seq<Delta>
typealias Recurse<E> = (path: Path, lhs: E, rhs: E, progress: Progress) -> Eval<Progress>

operator fun <E, L, O, A> Sniffr.Companion.invoke(
    lang: JsonLang<E, L, O, A>,
    strict: Boolean = true
): Sniffr<E> =
    EvalSniffr(lang, strict = strict)

class EvalSniffr<E, L, O, A>(
    private val lang: JsonLang<E, L, O, A>,
    private val strategies: Strategies
) : Sniffr<E> {
    constructor(lang: JsonLang<E, L, O, A>, strict: Boolean = true) : this(
        lang = lang,
        strategies = Strategies(strict = strict)
    )

    class Strategies(
        val objects: Strategy,
        val arrays: Strategy
    ) {
        constructor(strict: Boolean = true) : this(
            objects = Strategy.STRICT,
            arrays = if (strict) Strategy.STRICT else Strategy.LENIENT
        )

        companion object
    }

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

    override fun sniff(lhs: E, rhs: E): Seq<Delta> =
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

    private val nodes = Nodes(lang)

    private fun leaf(path: Path, lhs: Lit<L, O, A>, rhs: Lit<L, O, A>, progress: Progress): Eval<Progress> {
        val delta = with(nodes) {
            if (lhs eq rhs) null else Replace(path)
        }
        return Eval.now(delta cons progress)
    }

    private fun down(path: Path, lhs: Obj<L, O, A>, rhs: Obj<L, O, A>, progress: Progress): Eval<Progress> =
        with(nodes) {
            strategies.objects.diff(path, lhs.decompose(), rhs.normalize(), progress, ::sniff)
        }

    private fun into(path: Path, lhs: Arr<L, O, A>, rhs: Arr<L, O, A>, progress: Progress): Eval<Progress> =
        with(nodes) {
            strategies.arrays.diff(path, lhs.decompose(), rhs.normalize(), progress, ::sniff)
        }

    companion object
}
