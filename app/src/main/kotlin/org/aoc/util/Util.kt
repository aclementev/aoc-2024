package org.aoc.util

import java.nio.file.Paths
import kotlin.io.path.readText

enum class Direction4 {
    NORTH,
    EAST,
    SOUTH,
    WEST;

    fun vector(): Vector2 =
            when (this) {
                NORTH -> Vector2(-1, 0)
                EAST -> Vector2(0, 1)
                SOUTH -> Vector2(1, 0)
                WEST -> Vector2(0, -1)
            }
}

data class Vector2(val x: Int, val y: Int) {
    operator fun plus(other: Vector2): Vector2 = Vector2(x + other.x, y + other.y)
    operator fun plus(other: Int): Vector2 = Vector2(x + other, y + other)
    operator fun minus(other: Vector2): Vector2 = Vector2(x - other.x, y - other.y)
    operator fun minus(other: Int): Vector2 = Vector2(x - other, y - other)
    operator fun unaryMinus(): Vector2 = Vector2(-x, -y)
    operator fun times(other: Vector2): Vector2 = Vector2(x * other.x, y * other.y)
    operator fun times(other: Int): Vector2 = Vector2(x * other, y * other)
}

fun readDataFile(path: String): String {
    // The starting path is `app`
    return Paths.get("").toAbsolutePath().getParent().resolve("data").resolve(path).readText()
}

fun readDataLines(path: String): List<String> {
    return readDataFile(path).split("\n")
}
