package `fun`.notfound.sniffr

import `fun`.notfound.sniffr.Path.Companion.render

sealed interface Delta {
    val path: Path

    companion object
}

data class Insert(override val path: Path) : Delta {
    override fun toString(): String =
        "Insert(${render(path.segments)})"

    companion object
}

data class Replace(override val path: Path) : Delta {
    override fun toString(): String =
        "Replace(${render(path.segments)})"

    companion object
}

data class Move(
    val parent: Path,
    val origin: Path.Segment,
    val target: Path.Segment,
) : Delta {
    override val path: Path = parent + origin

    override fun toString(): String =
        "Move(${render(parent.segments)}:${render(origin)}->${render(target)})"

    companion object
}

data class Delete(override val path: Path) : Delta {
    override fun toString(): String =
        "Delete(${render(path.segments)})"

    companion object
}
