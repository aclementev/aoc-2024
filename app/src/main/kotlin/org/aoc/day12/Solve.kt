package org.aoc.day12

import java.nio.file.Paths
import kotlin.io.path.readText

data class Vector2(val x: Int, val y: Int) {
    operator fun plus(other: Vector2): Vector2 = Vector2(x + other.x, y + other.y)
    operator fun minus(other: Vector2): Vector2 = Vector2(x - other.x, y - other.y)
}

enum class Direction {
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

data class Region(val type: Char, val cells: List<Vector2>) {
    fun area(): Int = cells.count()
    fun perimeter(grid: List<String>): Int {
        // Go through all the positions in the region and
        return cells.sumOf {
            var cellContribution = 0
            forEachNeighbor(grid, it) { _, neighborValue ->
                if (neighborValue != type) {
                    // This is an edge, so we add one to the perimeter
                    cellContribution += 1
                }
            }
            cellContribution
        }
    }

    fun sides(grid: List<String>): Int {
        // We count the number of sides by counting the number of corners
        return cells.sumOf { pos ->
            var neighbors = mutableListOf<Pair<Vector2, Boolean>>()
            forEachNeighbor(grid, pos) { neighborPos, neighborValue ->
                neighbors.add(Pair(neighborPos, neighborValue != type))
            }

            // We dupliate the first element at the end so that we can iterate
            // over it as well
            neighbors.add(neighbors.first())

            // We identify corners by having consecutive directions be at the edge at the same time
            // (outside corner)
            // or in the case of both being not an edge, the diagonal is different (inside corner)
            neighbors
                    .zipWithNext() { (curPos, curIsEdge), (nextPos, nextIsEdge) ->
                        if (curIsEdge && nextIsEdge) {
                            1
                        } else if ((!curIsEdge) && (!nextIsEdge)) {
                            // Check the cell in the diagonal in the direction of
                            // this corner
                            val curDirectionVec = curPos - pos
                            val nextDirectionVec = nextPos - pos
                            val diagonalVec = curDirectionVec + nextDirectionVec
                            val diagonalPos = pos + diagonalVec
                            // NOTE: The diagonal MUST be valid, since we already
                            // checked if both directions are not edges
                            val diagonalValue = grid[diagonalPos.y][diagonalPos.x]
                            if (diagonalValue != type) {
                                1
                            } else {
                                0
                            }
                        } else {
                            0
                        }
                    }
                    .sum()
        }
    }
}

fun solve() {
    // part1("day12/sample.txt")
    // part1("day12/sample3.txt")
    part1("day12/input.txt")
    // part2("day12/sample.txt")
    // part2("day12/sample5.txt")
    part2("day12/input.txt")
}

fun readDataFile(path: String): String {
    // The starting path is `app`
    return Paths.get("").toAbsolutePath().getParent().resolve("data").resolve(path).readText()
}

fun part1(path: String) {
    val grid = readDataFile(path).trim().split("\n")

    // Find the region
    val regions = findRegions(grid)

    // Print the result
    val price = regions.sumOf { it.area() * it.perimeter(grid) }
    println(price)
}

fun part2(path: String) {
    val grid = readDataFile(path).trim().split("\n")

    // Find the region
    val regions = findRegions(grid)

    // Print the result
    val price = regions.sumOf { it.area() * it.sides(grid) }
    println(price)
}

fun findRegions(grid: List<String>): List<Region> {
    // Go through the cells in the grid and check the contiguous regions
    val height = grid.count()
    val width = grid[0].length
    val visited = mutableSetOf<Vector2>()
    val regions = mutableListOf<Region>()

    for (y in 0 until height) {
        val line = grid[y]
        for (x in 0 until width) {
            val pos = Vector2(x, y)
            if (visited.contains(pos)) {
                continue
            }

            val value = line[x]
            val regionCells = exploreRegion(pos, grid)

            // Create a region
            val region = Region(type = value, cells = regionCells)
            regions.add(region)

            // Add the regionCells to the list of already visited cells
            visited.addAll(regionCells)
        }
    }

    return regions.toList()
}

fun exploreRegion(position: Vector2, grid: List<String>): List<Vector2> {
    val regionValue = grid[position.y][position.x]
    val candidates = mutableListOf<Vector2>(position)
    val visited = mutableSetOf<Vector2>()
    val result = mutableListOf<Vector2>()

    // Breadth first search
    while (candidates.isNotEmpty()) {
        val cand = candidates.removeLast()

        // Add this candidate to the region list
        result.add(cand)
        visited.add(cand)

        // Explore all the neighbors
        forEachNeighbor(grid, cand) { neighborPos, neighborValue ->
            if ((neighborValue != null) &&
                            (neighborValue == regionValue) &&
                            (!visited.contains(neighborPos))
            ) {
                candidates.add(neighborPos)
                visited.add(neighborPos)
            }
        }
    }

    return result
}

fun forEachNeighbor(grid: List<String>, position: Vector2, f: (Vector2, Char?) -> Unit) {
    Direction.values().forEach {
        val neighborPos = position + it.vector()

        // Check if the position is valid and it has not been yet added to the candidate list
        val neighborValue = grid.getOrNull(neighborPos.y)?.getOrNull(neighborPos.x)
        // Apply the function
        f(neighborPos, neighborValue)
    }
}
