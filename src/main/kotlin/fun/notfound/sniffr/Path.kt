package `fun`.notfound.sniffr

import io.vavr.collection.Seq
import io.vavr.collection.List

@JvmInline value class Path(val segments: Seq<Segment>) {
    sealed interface Segment {
        @JvmInline value class Field(val name: String) : Segment
        @JvmInline value class Index(val value: Int) : Segment
    }

    operator fun plus(field: String): Path =
        plus(Segment.Field(field))

    operator fun plus(index: Int): Path =
        plus(Segment.Index(index))

    operator fun plus(segment: Segment): Path =
        Path(segment cons segments)

    override fun toString(): String =
        "Path(${render(segments)})"

    companion object {
        val root = Path(List.empty())

        fun render(segments: Seq<Segment>): String =
            segments.reverse().joinToString(prefix = "$", separator = "", transform = Companion::render)

        fun render(segment: Segment): String =
            when (segment) {
                is Segment.Field -> ".${segment.name}"
                is Segment.Index -> "[${segment.value}]"
            }
    }
}
