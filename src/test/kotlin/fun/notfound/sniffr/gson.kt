package `fun`.notfound.sniffr

import com.google.gson.Gson
import com.google.gson.JsonElement

val gson = Gson()

fun parse(text: String): JsonElement =
    gson.fromJson(text, JsonElement::class.java)
