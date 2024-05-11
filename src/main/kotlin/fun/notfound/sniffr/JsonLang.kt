package `fun`.notfound.sniffr

interface JsonLang<E, L, O, A> {
    sealed interface Node<L, O, A> { companion object }
    data class Lit<L, O, A>(val v: L) : Node<L, O, A> { companion object }
    data class Obj<L, O, A>(val v: O) : Node<L, O, A> { companion object }
    data class Arr<L, O, A>(val v: A) : Node<L, O, A> { companion object }

    fun classify(el: E): Node<L, O, A>

    fun eq(lhs: L, rhs: L): Boolean

    fun childrenOf(el: O): Map<String, E>
    fun fieldsOf(el: O): Set<String>
    fun get(el: O, field: String): E?

    fun elementsOf(el: A): List<E>
    fun get(el: A, index: Int): E?

    companion object
}
