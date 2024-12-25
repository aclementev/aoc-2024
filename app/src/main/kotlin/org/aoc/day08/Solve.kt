package org.aoc.day08

import java.nio.file.Paths
import kotlin.io.path.readText

data class Vector2(val x: Int, val y: Int) {
    operator fun plus(other: Vector2): Vector2 = Vector2(x + other.x, y + other.y)
    operator fun minus(other: Vector2): Vector2 = Vector2(x - other.x, y - other.y)
    operator fun times(other: Int): Vector2 = Vector2(x * other, y * other)
}

data class Antenna(val pos: Vector2, val value: Char)

// Takes two positions, the width and the height
typealias ResonanceStrategy = (Vector2, Vector2, Int, Int) -> List<Vector2>

fun solve() {
    // part1("day08/sample.txt")
    part1("day08/input.txt")
    // part2("day08/sample.txt")
    part2("day08/input.txt")
}

fun readDataFile(path: String): String {
    // The starting path is `app`
    return Paths.get("").toAbsolutePath().getParent().resolve("data").resolve(path).readText()
}

fun part1(path: String) {
    val lines = readDataFile(path).trim().split("\n")
    val height = lines.size
    val width = lines[0].length

    // Parse the locations of each of the antennas
    val antennas =
            lines.withIndex().flatMap { indexedLine ->
                indexedLine.value.withIndex().filter { it.value != '.' }.map {
                    Antenna(Vector2(it.index, indexedLine.index), it.value)
                }
            }
    val byValue = antennas.groupBy { it.value }

    // Compute the resonances for each group
    val resonances =
            byValue.values.map { an -> an.map { it.pos } }.flatMap {
                getResonances(it, width, height, ::getPairResonances)
            }

    // Deduplicate resonances
    val dedup = resonances.toSet().toList()
    println(dedup.size)
}

fun part2(path: String) {
    val lines = readDataFile(path).trim().split("\n")
    val height = lines.size
    val width = lines[0].length

    // Parse the locations of each of the antennas
    val antennas =
            lines.withIndex().flatMap { indexedLine ->
                indexedLine.value.withIndex().filter { it.value != '.' }.map {
                    Antenna(Vector2(it.index, indexedLine.index), it.value)
                }
            }
    val byValue = antennas.groupBy { it.value }

    // Compute the resonances for each group
    val resonances =
            byValue.values.map { an -> an.map { it.pos } }.flatMap {
                getResonances(it, width, height, ::getResonancesHarmonic)
            }

    // Deduplicate resonances
    val dedup = resonances.toSet().toList()
    println(dedup.size)
}

fun getResonances(
        positions: List<Vector2>,
        width: Int,
        height: Int,
        strategy: ResonanceStrategy,
): List<Vector2> {
    // Check every combination of positions
    return positions
            .flatMap { pos ->
                positions.filter { it != pos }.flatMap { pos2 ->
                    // Given a position, get the resonance
                    strategy(pos, pos2, width, height)
                }
            }
            .toSet()
            .toList()
}

fun getPairResonances(left: Vector2, right: Vector2, width: Int, height: Int): List<Vector2> {
    // We treat this as vectors, and the resonances are in the same direction
    // of the difference, and pointing "outside"
    val diff = left - right
    return listOf(left + diff, right - diff).filter {
        (it.x >= 0) && (it.x < width) && (it.y >= 0) && (it.y < height)
    }
}

fun getResonancesHarmonic(left: Vector2, right: Vector2, width: Int, height: Int): List<Vector2> {
    // We treat this as vectors, and the resonances are in the same direction
    // of the difference, and pointing "outside"
    val diff = left - right

    val inBounds = { pos: Vector2 ->
        (pos.x >= 0) && (pos.x < width) && (pos.y >= 0) && (pos.y < height)
    }

    var resonances = mutableListOf<Vector2>()
    var times = 0
    while (true) {
        val timesDiff = diff * times

        // Check the left direction
        val leftResonance = left + timesDiff
        val rightResonance = right - timesDiff

        if (inBounds(leftResonance)) {
            resonances.add(leftResonance)
        }
        if (inBounds(rightResonance)) {
            resonances.add(rightResonance)
        }

        if (!inBounds(leftResonance) && !inBounds(rightResonance)) {
            break
        }

        times += 1
    }
    return resonances
}
