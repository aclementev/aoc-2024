package org.aoc.util

import java.nio.file.Paths
import kotlin.io.path.readText
import kotlin.math.sqrt

enum class Direction4 {
    NORTH,
    EAST,
    SOUTH,
    WEST;

    fun vector(): Vector2 =
            when (this) {
                NORTH -> Vector2(0, -1)
                EAST -> Vector2(1, 0)
                SOUTH -> Vector2(0, 1)
                WEST -> Vector2(-1, 0)
            }

    companion object Factory {
        fun fromVector(vector: Vector2): Direction4 =
                when (vector) {
                    Vector2(0, -1) -> Direction4.NORTH
                    Vector2(1, 0) -> Direction4.EAST
                    Vector2(0, 1) -> Direction4.SOUTH
                    Vector2(-1, 0) -> Direction4.WEST
                    else -> error("Cannot create a Direction4 from vector: $vector")
                }
    }
}

enum class Direction8 {
    NORTH,
    NORTHEAST,
    EAST,
    SOUTHEAST,
    SOUTH,
    SOUTHWEST,
    WEST,
    NORTHWEST;

    fun vector(): Vector2 =
            when (this) {
                NORTH -> Vector2(0, -1)
                NORTHEAST -> Vector2(1, -1)
                EAST -> Vector2(1, 0)
                SOUTHEAST -> Vector2(1, 1)
                SOUTH -> Vector2(0, 1)
                SOUTHWEST -> Vector2(-1, 1)
                WEST -> Vector2(-1, 0)
                NORTHWEST -> Vector2(-1, -1)
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
    operator fun div(other: Vector2): Vector2 = Vector2(x / other.x, y / other.y)
    operator fun div(other: Int): Vector2 = Vector2(x / other, y / other)
    operator fun rem(other: Vector2): Vector2 = Vector2(x % other.x, y % other.y)
    operator fun rem(other: Int): Vector2 = Vector2(x % other, y % other)

    fun mod(other: Vector2): Vector2 = Vector2(x.mod(other.x), y.mod(other.y))
    fun mod(other: Int): Vector2 = Vector2(x.mod(other), y.mod(other))

    fun length(): Double = sqrt(x.toDouble() * x.toDouble() + y.toDouble() * y.toDouble())
}

fun positionIsValid(position: Vector2, width: Int, height: Int): Boolean {
    return ((position.x >= 0) && (position.x < width) && (position.y >= 0) && (position.y < height))
}

fun readDataFile(path: String): String {
    // The starting path is `app`
    return Paths.get("")
            .toAbsolutePath()
            .getParent()
            .resolve("data")
            .resolve(path)
            .readText()
            .trim()
}

fun readDataLines(path: String): List<String> {
    return readDataFile(path).split("\n").map { it.trim() }
}
