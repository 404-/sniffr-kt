package `fun`.notfound.sniffr.impls.common.lang

import `fun`.notfound.sniffr.JsonLang
import `fun`.notfound.sniffr.JsonLang.*
import `fun`.notfound.sniffr.Path.Segment.Field
import `fun`.notfound.sniffr.Path.Segment.Index
import io.vavr.collection.LinkedHashMap
import io.vavr.collection.Map
import io.vavr.collection.Queue
import io.vavr.collection.Traversable

class Nodes<E, L, O, A>(private val lang: JsonLang<E, L, O, A>) {
    infix fun Lit<L, O, A>.eq(that: Lit<L, O, A>) =
        lang.eq(lhs = this.v, rhs = that.v)

    fun Obj<L, O, A>.normalize(): Map<Field, E> =
        fold(LinkedHashMap.empty(), Map<Field, E>::put)

    fun Obj<L, O, A>.decompose(): Traversable<Pair<Field, E>> =
        fold(Queue.empty()) { children, key, child -> children.append(key to child) }

    fun <R> Obj<L, O, A>.fold(zero: R, grow: (R, Field, E) -> R): R {
        tailrec fun step(children: Iterator<kotlin.collections.Map.Entry<String, E>>, res: R): R =
            if (children.hasNext()) {
                val (field: String, child: E) = children.next()
                step(children, grow(res, Field(field), child))
            }
            else
                res

        return step(lang.childrenOf(v).iterator(), zero)
    }

    fun Arr<L, O, A>.normalize(): Map<Index, E> =
        fold(LinkedHashMap.empty(), Map<Index, E>::put)

    fun Arr<L, O, A>.decompose(): Traversable<Pair<Index, E>> =
        fold(Queue.empty()) { children, key, child -> children.append(key to child) }

    fun <R> Arr<L, O, A>.fold(zero: R, grow: (R, Index, E) -> R): R {
        val elements = lang.elementsOf(v)

        tailrec fun step(i: Int, res: R): R =
            if (i < elements.size)
                step(i + 1, grow(res, Index(i), elements[i]))
            else
                res

        return step(0, zero)
    }
}
