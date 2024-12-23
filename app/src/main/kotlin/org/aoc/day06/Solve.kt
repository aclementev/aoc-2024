package org.aoc.day06

import java.nio.file.Paths
import kotlin.io.path.readText

data class Vector(val x: Int, val y: Int) {
    operator fun plus(other: Vector): Vector {
        return Vector(x + other.x, y + other.y)
    }
}

enum class Direction {
    NORTH,
    EAST,
    SOUTH,
    WEST;

    fun rotate(): Direction =
            when (this) {
                NORTH -> EAST
                EAST -> SOUTH
                SOUTH -> WEST
                WEST -> NORTH
            }
}

class LoopException(message: String) : Exception(message)

fun solve() {
    // part1("day06/sample.txt")
    part1("day06/input.txt")
    // part2("day06/sample.txt")
    part2("day06/input.txt")
}

fun readDataFile(path: String): String {
    // The starting path is `app`
    return Paths.get("").toAbsolutePath().getParent().resolve("data").resolve(path).readText()
}

fun part1(path: String) {

    // Parse the map
    val lines = readDataFile(path).trim().split("\n")

    val result = countGuardPositions(lines)
    println(result)
}

fun part2(path: String) {

    // Parse the map
    val lines = readDataFile(path).trim().split("\n")

    // Figure out the path of the guard
    val initialPosition = getInitialPosition(lines)

    // It only makes sense to block positions that would be visited by the guard
    val initialGuardPath = simulateGuardPath(lines, initialPosition, Direction.NORTH)
    val uniqueGuardPositions = initialGuardPath.toSet()

    // Check every position to see if it would loop
    var blockPositions =
            uniqueGuardPositions.filter { it != initialPosition }.filter {
                val newGrid = blockCell(lines, it)
                try {
                    simulateGuardPath(newGrid, initialPosition, Direction.NORTH)
                    false
                } catch (e: LoopException) {
                    true
                }
            }

    println(blockPositions.size)
}

fun blockCell(grid: List<String>, position: Vector): List<String> {
    return grid.withIndex().map { line ->
        String(
                line.value
                        .withIndex()
                        .map { col ->
                            if ((col.index == position.x) && (line.index == position.y)) {
                                '#'
                            } else {
                                col.value
                            }
                        }
                        .toCharArray()
        )
    }
}

fun countGuardPositions(grid: List<String>): Int {
    // Simulate the guard stepping
    var position = getInitialPosition(grid)
    val guardPath = simulateGuardPath(grid, position, Direction.NORTH)
    val distinctPositions = guardPath.toSet()
    return distinctPositions.count()
}

fun simulateGuardPath(
        grid: List<String>,
        initialPosition: Vector,
        initialDirection: Direction,
): List<Vector> {
    val height = grid.size
    val width = grid[0].length

    var position = initialPosition
    var direction = initialDirection
    var guardPath = mutableListOf(position)
    // For loop detection: If the guard falls in the same position with the
    // same direction, he's in a loop (the path is deterministic)
    var visitedPosDir = mutableSetOf(Pair(position, direction))

    while (true) {
        // Find whatever is in front of the guard
        val facingPosition =
                when (direction) {
                    Direction.NORTH -> position + Vector(0, -1)
                    Direction.EAST -> position + Vector(1, 0)
                    Direction.SOUTH -> position + Vector(0, 1)
                    Direction.WEST -> position + Vector(-1, 0)
                }

        // If the position is an edge, just walk out
        if ((facingPosition.x < 0) ||
                        (facingPosition.x >= width) ||
                        (facingPosition.y < 0) ||
                        (facingPosition.y >= height)
        ) {
            // We are done
            break
        }

        // Check whatever is in front
        val facingObject = grid[facingPosition.y][facingPosition.x]

        if (facingObject == '#') {
            // NOTE(alvaro): We don't step, we let the next iteration handle it
            // since we need to repeat all the checks
            direction = direction.rotate()
        } else {
            // Check if we are in a loop
            if (visitedPosDir.contains(Pair(facingPosition, direction))) {
                throw LoopException("We are in a loop")
            }

            // Perform a Step
            position = facingPosition
            guardPath.add(position)
            visitedPosDir.add(Pair(position, direction))
        }
    }
    return guardPath
}

fun getInitialPosition(grid: List<String>): Vector {
    val lineIndex = grid.withIndex().find { it.value.contains('^') }!!.index
    val colIndex = grid[lineIndex].indexOf('^')
    return Vector(colIndex, lineIndex)
}
