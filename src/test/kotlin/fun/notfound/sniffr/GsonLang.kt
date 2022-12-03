package `fun`.notfound.sniffr

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.google.gson.*
import `fun`.notfound.sniffr.JsonLang.*

object GsonLang : JsonLang<JsonElement, Either<JsonNull, JsonPrimitive>, JsonObject, JsonArray> {
    override fun classify(el: JsonElement): Node<Either<JsonNull, JsonPrimitive>, JsonObject, JsonArray> =
        when(el) {
            is JsonNull -> Lit(el.left())
            is JsonPrimitive -> Lit(el.right())
            is JsonObject -> Obj(el)
            is JsonArray -> Arr(el)
            else -> throw UnsupportedOperationException("Unsupported type: ${el::class.java}")
        }

    override fun eq(lhs: Either<JsonNull, JsonPrimitive>, rhs: Either<JsonNull, JsonPrimitive>): Boolean =
        when(lhs) {
            is Either.Left -> when(rhs) {
                is Either.Left -> true
                is Either.Right -> false
            }
            is Either.Right -> when(rhs) {
                is Either.Left -> false
                is Either.Right -> lhs.value == rhs.value
            }
        }

    override fun childrenOf(el: JsonObject): Map<String, JsonElement> = el.asMap()
    override fun fieldsOf(el: JsonObject): Set<String> = el.keySet()
    override fun get(el: JsonObject, field: String): JsonElement? = el.get(field)

    override fun elementsOf(el: JsonArray): List<JsonElement> = el.asList()
    override fun get(el: JsonArray, index: Int): JsonElement? =
        index.takeIf { it < el.size() }?.let(el::get)
}
