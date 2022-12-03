package `fun`.notfound.sniffr

import arrow.core.Either
import com.google.gson.*
import io.kotest.core.spec.style.FreeSpec
import io.kotest.core.spec.style.scopes.FreeSpecTerminalScope
import io.kotest.inspectors.forAll
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSingleElement
import `fun`.notfound.sniffr.Path.Segment.Index

typealias GsonSniffr = Sniffr<JsonElement, Either<JsonNull, JsonPrimitive>, JsonObject, JsonArray>

class SniffrSpec : FreeSpec({
    "stack safety" - {
        val depth = 1024 * 16

        tailrec fun Path.unfold(limit: Int, level: Int = 0, f: (Int) -> Path.Segment): Path =
            if (level > limit) this else (this + f(level)).unfold(limit, level + 1, f)

        "nested objects" {
            val lhs = JsonObject().gen("madness", depth, "1")
            val rhs = JsonObject().gen("madness", depth, "2")

            val expected = (Path.root + "madness").unfold(depth, 1) { Path.Segment.Field(it.toString()) }

            sniff(lhs, rhs) shouldHaveSingleElement Replace(expected)
        }

        "nested arrays" {
            val lhs = JsonArray().gen(depth, "1")
            val rhs = JsonArray().gen(depth, "2")

            val expected = Path.root.unfold(depth) { Index(0) }

            sniff(lhs, rhs) shouldHaveSingleElement Replace(expected)
        }
    }

    "objects" - {
        "same" - {
            "shallow" {
                val root = parse("""{ "x": 1 }""")
                sniff(root, root).shouldBeEmpty()
            }

            "deep" {
                val root = parse("""{ "a": { "x": 1 } }""")
                sniff(root, root).shouldBeEmpty()
            }
        }

        "delete" - {
            "shallow" {
                enforce(
                    """{ "x": 1 }""",
                    """{}""",
                    Delete(Path.root + "x")
                )
            }

            "deep" {
                enforce(
                    """{ "a": { "x": 1 } }""",
                    """{ "a": {} }""",
                    Delete(Path.root + "a" + "x")
                )
            }

            "mixed" {
                enforce(
                    """{ "a": { "x": 1 }, "b": { "y": 9 } }""",
                    """{ "b": { "y": 9 }, "a": {} }""",
                    Delete(Path.root + "a" + "x")
                )
            }
        }

        "insert" - {
            "shallow" {
                enforce(
                    """{}""",
                    """{ "x": 1 }""",
                    Insert(Path.root + "x")
                )
            }

            "deep" {
                enforce(
                    """{ "a": {} }""",
                    """{ "a": { "x": 1 } }""",
                    Insert(Path.root + "a" + "x")
                )
            }

            "mixed" {
                enforce(
                    """{ "b": { "y": 9 }, "a": {} }""",
                    """{ "a": { "x": 1 }, "b": { "y": 9 } }""",
                    Insert(Path.root + "a" + "x")
                )
            }
        }

        "replace" - {
            "shallow" {
                enforce(
                    """{ "x": 1 }""",
                    """{ "x": 2 }""",
                    Replace(Path.root + "x")
                )
            }

            "deep" {
                enforce(
                    """{ "a": { "x": 1 } }""",
                    """{ "a": { "x": 2 } }""",
                    Replace(Path.root + "a" + "x")
                )
            }

            "mixed" {
                enforce(
                    """{ "a": { "x": 1 }, "b": { "y": 9 } }""",
                    """{ "b": { "y": 9 }, "a": { "x": 2 } }""",
                    Replace(Path.root + "a" + "x")
                )
            }
        }
    }

    "arrays" - {
        "same" - {
            "shallow" {
                val root = parse("""[1]""")
                sniff(root, root).shouldBeEmpty()
            }

            "deep" {
                val root = parse("""[[1]]""")
                sniff(root, root).shouldBeEmpty()
            }
        }

        "delete" - {
            "shallow" {
                enforce(
                    """[1]""",
                    """[ ]""",
                    Delete(Path.root + 0)
                )
            }

            "deep" {
                enforce(
                    """[[1]]""",
                    """[[ ]]""",
                    Delete(Path.root + 0 + 0)
                )
            }

            "multiple" {
                enforce(
                    """[[1],[2]]""",
                    """[[ ],[2]]""",
                    Delete(Path.root + 0 + 0)
                )
            }
        }

        "insert" - {
            "shallow" {
                enforce(
                    """[ ]""",
                    """[1]""",
                    Insert(Path.root + 0)
                )
            }

            "deep" {
                enforce(
                    """[[ ]]""",
                    """[[1]]""",
                    Insert(Path.root + 0 + 0)
                )
            }

            "multiple" {
                enforce(
                    """[[ ],[2]]""",
                    """[[1],[2]]""",
                    Insert(Path.root + 0 + 0)
                )
            }
        }

        "replace" - {
            "shallow" {
                enforce(
                    """[1]""",
                    """[2]""",
                    Replace(Path.root + 0)
                )
            }

            "deep" {
                enforce(
                    """[[1]]""",
                    """[[2]]""",
                    Replace(Path.root + 0 + 0)
                )
            }

            "multiple" {
                enforce(
                    """[[1],[2]]""",
                    """[[2],[2]]""",
                    Replace(Path.root + 0 + 0)
                )
            }
        }
    }

    "mixed" - {
        "misc" {
            enforce(
                """{
                  |  "a": {
                  |    "x": 1,
                  |    "y": 2,
                  |    "z": 3
                  |  },
                  |  "b": [
                  |    { "m": 4 },
                  |    { "n": 5 }
                  |  ]
                  |}
                """,
                """{
                  |  "a": {
                  |    "y": 91,
                  |    "z": 3,
                  |    "w": 92
                  |  },
                  |  "b": [
                  |    { "n": 5 }
                  |  ]
                  |}
                """,
                Delete(Path.root + "a" + "x"),
                Replace(Path.root + "a" + "y"),
                Insert(Path.root + "a" + "w"),
                Delete(Path.root + "b" + 0 + "m"),
                Insert(Path.root + "b" + 0 + "n"),
                Delete(Path.root + "b" + 1)
            )
        }

        "replace - type change" - {
            listOf(
                "Int -> String" to """{ "a": 1 }""" to """{ "a": "1" }""",
                "Int -> Object" to """{ "a": 1 }""" to """{ "a": { } }""",
                "Int -> Array"  to """{ "a": 1 }""" to """{ "a": [ ] }""",

                "Object -> Int"   to """{ "a": {} }""" to """{ "a": 1  }""",
                "Object -> Array" to """{ "a": {} }""" to """{ "a": [] }""",

                "Array -> Int"    to """{ "a": [] }""" to """{ "a": 1  }""",
                "Array -> Object" to """{ "a": [] }""" to """{ "a": {} }"""
            ).forAll { (x, rhs) ->
                val (name, lhs) = x
                name {
                    enforce(
                        lhs,
                        rhs,
                        Replace(Path.root + "a")
                    )
                }
            }
        }
    }

    "lenient" - {
        val sniffr = Sniffr(GsonLang, strict = false)

        "same" - {
            "shallow" {
                val root = parse("""[ 1 ]""")
                sniffr.sniff(root, root).shouldBeEmpty()
            }

            "deep" {
                val root = parse("""{ "a": [{ "x": 1 }] }""")
                sniffr.sniff(root, root).shouldBeEmpty()
            }
        }

        "misc" {
            enforce(
                """[
                  |  { "a": 1 },
                  |  { "b": 2 },
                  |  { "c": 3 },
                  |  { "d": 4 }
                  |]
                """,
                """[
                  |  { "a": 91 },
                  |  { "c": 3 },
                  |  { "b": 3 }
                  |]
                """,
                Replace(Path.root + 0 + "a"),
                Delete(Path.root + 1 + "b"),
                Insert(Path.root + 1 + "c"),
                Move(Path.root, Index(2), Index(1)),
                Delete(Path.root + 3),
                sniffr = sniffr
            )
        }
    }
}) {
    private companion object {
        val sniff = Sniffr(GsonLang)::sniff

        fun Sniffr<JsonElement, *, *, *>.sniff(lhs: String, rhs: String) =
            sniff(parse(lhs.trimMargin()), parse(rhs.trimMargin()))

        fun FreeSpecTerminalScope.enforce(
            lhs: String,
            rhs: String,
            head: Delta, vararg tail: Delta,
            sniffr: GsonSniffr = Sniffr(GsonLang)
        ) {
            val actual = sniffr.sniff(lhs, rhs)
            val expected = listOf(head) + listOf(*tail)

            actual shouldContainExactly expected
        }

        fun JsonObject.gen(field: String, limit: Int, x: String): JsonObject = also {
            val nested = gen(
                limit = limit,
                x = x,
                make = { JsonObject() },
                grow = { level, child -> add(level.toString(), child) },
                leaf = { level, value -> add(level.toString(), JsonPrimitive(value)) }
            )

            add(field, nested)
        }

        fun JsonArray.gen(limit: Int, x: String): JsonArray = also {
            val nested = gen(
                limit = limit,
                x = x,
                make = { JsonArray(1) },
                grow = { _, child -> add(child) },
                leaf = { _, value -> add(value) }
            )

            add(nested)
        }

        private fun <E> gen(limit: Int, x: String, make: () -> E, grow: E.(Int, E) -> Unit, leaf: E.(Int, String) -> Unit): E =
            gen({ level -> if (level >= limit) x else null }, make, grow, leaf)

        private fun <E> gen(term: (Int) -> String?, make: () -> E, grow: E.(Int, E) -> Unit, leaf: E.(Int, String) -> Unit): E =
            make().let { root ->
                gen(root, root, level = 1) { level, parent ->
                    when (val x = term(level)) {
                        null ->
                            make().also { child -> parent.grow(level, child) }
                        else -> {
                            parent.leaf(level, x)
                            null
                        }
                    }
                }
            }

        private tailrec fun <E> gen(root: E, curr: E, level: Int, grow: (Int, E) -> E?): E =
            when (val child = grow(level, curr)) {
                null -> root
                else -> gen(root, child, level + 1, grow)
            }
    }
}
