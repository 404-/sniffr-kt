package `fun`.notfound.sniffr.impls.drf

import `fun`.notfound.sniffr.Sniffr
import `fun`.notfound.sniffr.Testkit
import io.kotest.core.spec.style.FreeSpec
import `fun`.notfound.sniffr.langs.gson.GsonLang

class DrfSniffrSpec : FreeSpec({
    include(Testkit.standard(Sniffr(GsonLang, strict = true)))
    include(Testkit.lenient(Sniffr(GsonLang, strict = false)))
})
