package `fun`.notfound.sniffr.utils

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.vavr.collection.Vector

class VavrUtilsSpec : FreeSpec({
    "tail vs drop" {
        with(Vector.empty<Int>()) {
            shouldThrow<UnsupportedOperationException> {
                tail()
            }

            drop(1).isEmpty.shouldBeTrue()
        }

        with(Vector.of(1)) {
            tail().isEmpty.shouldBeTrue()
            drop(1).isEmpty.shouldBeTrue()
        }
    }
})
