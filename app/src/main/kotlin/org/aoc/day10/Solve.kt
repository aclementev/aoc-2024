package org.aoc.day10

import java.nio.file.Paths
import kotlin.collections.ArrayDeque
import kotlin.io.path.readText

data class Vector2(val x: Int, val y: Int) {
    operator fun plus(other: Vector2): Vector2 = Vector2(x + other.x, y + other.y)
}

data class Cell(val pos: Vector2, val value: Int)

enum class Direction {
    NORTH,
    EAST,
    SOUTH,
    WEST
}

data class Candidate(val from: Cell, val to: Cell, val path: List<Cell>)

// sealed class ExploreDecision {
//     object Invalid : ExploreDecision()
//     data class Continue(val candidate: Candidate) : ExploreDecision()
//     data class Done(val candidate: Candidate) : ExploreDecision()
// }
enum class ExploreDecision {
    INVALID,
    CONTINUE,
    DONE
}

fun solve() {
    // part1("day10/sample.txt")
    // part1("day10/sample2.txt")
    part1("day10/input.txt")
    // part2("day10/sample.txt")
    // part2("day10/sample2.txt")
    part2("day10/input.txt")
}

fun readDataFile(path: String): String {
    // The starting path is `app`
    return Paths.get("").toAbsolutePath().getParent().resolve("data").resolve(path).readText()
}

fun part1(path: String) {
    val grid = readDataFile(path).trim().split("\n")

    // Find the trailheads
    val trailheads = findTrailheads(grid)

    // Explore the paths from the trailheads
    val validPaths =
            exploreValidPaths(grid, trailheads) { from: Cell, to: Cell ->
                // Logic to check if the path is valid
                when (to.value - from.value) {
                    1 ->
                            if (to.value == 9) {
                                ExploreDecision.DONE
                            } else {
                                ExploreDecision.CONTINUE
                            }
                    else -> ExploreDecision.INVALID
                }
            }

    // Count the number of unique positions reached
    val trailHeadScores =
            validPaths.groupBy { it.first().pos }.mapValues { (_, paths) ->
                paths.map { it.last() }.toSet().count()
            }
    val totalScore = trailHeadScores.values.sumOf { it }
    println(totalScore)
}

fun part2(path: String) {
    val grid = readDataFile(path).trim().split("\n")

    // Find the trailheads
    val trailheads = findTrailheads(grid)

    // Explore the paths from the trailheads
    val validPaths =
            exploreValidPaths(grid, trailheads) { from: Cell, to: Cell ->
                // Logic to check if the path is valid
                when (to.value - from.value) {
                    1 ->
                            if (to.value == 9) {
                                ExploreDecision.DONE
                            } else {
                                ExploreDecision.CONTINUE
                            }
                    else -> ExploreDecision.INVALID
                }
            }

    // Count the number of unique positions reached
    val trailHeadRatings =
            validPaths.groupBy { it.first().pos }.mapValues { (_, paths) -> paths.count() }
    val totalRating = trailHeadRatings.values.sumOf { it }
    println(totalRating)
}

fun findTrailheads(grid: List<String>): List<Cell> {
    return grid.withIndex().flatMap { (y, line) ->
        line.withIndex()
                .map { (x, value) ->
                    when (value) {
                        '0' -> Cell(Vector2(x, y), value.digitToInt())
                        else -> null
                    }
                }
                .filterNotNull()
    }
}

fun exploreValidPaths(
        grid: List<String>,
        startingPositions: List<Cell>,
        decider: (Cell, Cell) -> ExploreDecision,
): List<List<Cell>> {
    require(grid.size > 0)
    require(grid[0].length > 0)

    val height = grid.size
    val width = grid[0].length

    val move = { origin: Cell, direction: Direction ->
        val dest =
                when (direction) {
                    Direction.NORTH -> origin.pos + Vector2(0, -1)
                    Direction.EAST -> origin.pos + Vector2(1, 0)
                    Direction.SOUTH -> origin.pos + Vector2(0, 1)
                    Direction.WEST -> origin.pos + Vector2(-1, 0)
                }
        if ((dest.x < 0) || (dest.x >= width) || (dest.y < 0) || (dest.y >= height)) {
            null
        } else {
            Cell(dest, grid[dest.y][dest.x].digitToInt())
        }
    }

    // Implements a depth-first search for the valid paths
    val candidates = ArrayDeque<Candidate>()
    startingPositions.flatMapTo(candidates) { from ->
        Direction.values().map { move(from, it) }.filterNotNull().map { to ->
            Candidate(from, to, listOf(from, to))
        }
    }

    val result = mutableListOf<List<Cell>>()
    while (candidates.isNotEmpty()) {
        val cand = candidates.removeFirst()
        when (decider(cand.from, cand.to)) {
            ExploreDecision.INVALID -> {
                // Nothing to do, continue with next candidate
            }
            ExploreDecision.CONTINUE -> {
                // Generate new candidates to continue exploration
                val newFrom = cand.to
                Direction.values()
                        .map { move(newFrom, it) }
                        .filterNotNull()
                        .map { newTo -> Candidate(newFrom, newTo, cand.path + newTo) }
                        .reversed()
                        .forEach { candidates.addFirst(it) }
            }
            ExploreDecision.DONE -> {
                // Add the result to the result array and continue with next candidate
                result.add(cand.path)
            }
        }
    }

    return result.toList()
}
