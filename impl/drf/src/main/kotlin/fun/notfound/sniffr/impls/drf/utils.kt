package `fun`.notfound.sniffr.impls.drf

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import io.vavr.collection.Traversable
import kotlin.DeepRecursiveFunction as Drf

internal fun <E, R> fold(
    plus: Drf<Pair<R, E>, R>
): Drf<Pair<Traversable<E>, R>, R> = Drf { (elements, curr) ->

    when (elements.isEmpty) {
        true  -> curr
        false -> {
            val head = elements.head()
            val tail = elements.tail()
            val next = plus.callRecursive(curr to head)
            callRecursive(tail to next)
        }
    }
}

internal fun <E> first(
    test: Drf<E, Boolean>
): Drf<Traversable<E>, E?> = Drf { els: Traversable<E> ->

    val result = visit<E, Unit, E>(Drf { (_, el) ->
        val matched = test.callRecursive(el)
        if (matched) el.left() else Unit.right()
    }).callRecursive(els to Unit)

    result.fold({ it }, { null })
}

internal fun <E, R, L> visit(
    plus: Drf<Pair<R, E>, Either<L, R>>
): Drf<Pair<Traversable<E>, R>, Either<L, R>> = Drf { (els: Traversable<E>, curr: R) ->

    if (els.isEmpty)
        curr.right()
    else
        when (val next = plus.callRecursive(curr to els.head())) {
            is Either.Left  -> next
            is Either.Right -> callRecursive(els.tail() to next.value)
        }
}
