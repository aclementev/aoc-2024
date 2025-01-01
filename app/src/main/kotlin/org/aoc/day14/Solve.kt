package org.aoc.day14

import kotlin.math.abs
import kotlin.math.sqrt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.aoc.util.Vector2
import org.aoc.util.readDataLines

data class Robot(val position: Vector2, val velocity: Vector2)

fun solve() {
    // part1("day14/sample.txt")
    part1("day14/input.txt")
    // part2("day14/sample.txt")
    part2("day14/input.txt")
}

fun part1(path: String) {
    val (width, height) =
            if (path.contains("sample")) {
                Pair(11, 7)
            } else {
                Pair(101, 103)
            }
    val lines = readDataLines(path)
    val robots = parseInput(lines)
    val positions =
            robots.map { robotAtTime(100, it, width = width, height = height) }.map { it.position }
    printPositions(positions, width = width, height = height)
    println(safetyFactor(positions, width = width, height = height))
}

fun part2(path: String) {
    data class ComputationResult(
            val seconds: Int,
            val spatialAutocorrelation: Double,
            val grid: List<List<Int>>
    )

    val (width, height) =
            if (path.contains("sample")) {
                Pair(11, 7)
            } else {
                Pair(101, 103)
            }
    val lines = readDataLines(path)
    val robots = parseInput(lines)

    runBlocking {
        val interestingChannel = Channel<ComputationResult>()
        // Launch a live reporting coroutine
        launch {
            // Read from the results
            for (result in interestingChannel) {
                println(
                        "Got intersting result! ${result.seconds} -> ${result.spatialAutocorrelation}"
                )
                println(result.seconds)
                printBinaryGrid(result.grid)
            }
        }

        // Spawn the tasks
        val tasks =
                List(10_000) { seconds ->
                    async(Dispatchers.Default) {
                        val positions =
                                robots
                                        .map {
                                            robotAtTime(seconds, it, width = width, height = height)
                                        }
                                        .map { it.position }

                        val binaryGrid = getBinaryGrid(positions, width = width, height = height)
                        val spatialAutocorrelation =
                                moransI(binaryGrid, width = width, height = height)

                        val result = ComputationResult(seconds, spatialAutocorrelation, binaryGrid)
                        if (abs(spatialAutocorrelation) > 0.15) {
                            // Notify that we may have found something
                            interestingChannel.send(result)
                        }
                        result
                    }
                }

        // Await all producer jobs
        val results = tasks.awaitAll()
        interestingChannel.close()

        // Print the final result
        results.sortedBy { -abs(it.spatialAutocorrelation) }.take(10).reversed().forEach {
            println("${it.seconds} -> ${it.spatialAutocorrelation}}")
            printBinaryGrid(it.grid)
            println()
        }
        // NOTE(alvaro): The final result is ~6400 seconds, so we need to parallelize
        // or make this faster
    }
}

fun parseInput(lines: List<String>): List<Robot> {
    return lines.map { line ->
        val (posStr, velStr) = line.split(" ")

        // Parse position
        check(posStr.startsWith("p="))
        val (x, y) = posStr.substring(2).split(",").map { it.toInt() }

        // Parse velocity
        check(velStr.startsWith("v="))
        val (vx, vy) = velStr.substring(2).split(",").map { it.toInt() }

        Robot(position = Vector2(x, y), velocity = Vector2(vx, vy))
    }
}

fun printPositions(positions: List<Vector2>, width: Int, height: Int) {
    val posCounts = positions.groupingBy { it }.eachCount()
    for (y in 0 until height) {
        for (x in 0 until width) {
            val char = posCounts.get(Vector2(x, y))?.let { it.toString() } ?: "."
            print(char)
        }
        print("\n")
    }
}

fun printBinaryGrid(binaryGrid: List<List<Int>>) {
    binaryGrid.forEach { row ->
        val line =
                row
                        .map {
                            when (it) {
                                1 -> '#'
                                0 -> '.'
                                else -> '?'
                            }
                        }
                        .joinToString("")
        println(line)
    }
}

fun robotAtTime(second: Int, robot: Robot, width: Int, height: Int): Robot {
    // This uses the simple physics equations to simulate
    val newPos = (robot.position + robot.velocity * second).mod(Vector2(width, height))
    return robot.copy(position = newPos)
}

fun safetyFactor(positions: List<Vector2>, width: Int, height: Int): Long {
    // Separate the positions in quadrants
    var topLeft = 0L
    var topRight = 0L
    var botLeft = 0L
    var botRight = 0L

    positions.forEach {
        // Check the quadrant that this belongs to
        when {
            (it.x < width / 2) && (it.y < height / 2) -> topLeft += 1
            (it.x > width / 2) && (it.y < height / 2) -> topRight += 1
            (it.x < width / 2) && (it.y > height / 2) -> botLeft += 1
            (it.x > width / 2) && (it.y > height / 2) -> botRight += 1
        }
    }

    println("$topLeft $topRight $botLeft $botRight")

    return topLeft * topRight * botLeft * botRight
}

fun moransI(binaryGrid: List<List<Int>>, width: Int, height: Int): Double {
    // Compute Moran's I statistic for spatial autocorrelation as a heuristic for
    // the points being distributed in some kind of non-random pattern

    val N = width * height
    val sqrt2 = sqrt(2.0)
    val eps = 1e-3

    // The total weight sum
    var W = 0.0
    var moransNum = 0.0
    var moransDen = 0.0

    val mean = binaryGrid.flatten().sumOf { it.toLong() }.toDouble() / N

    for (i in 0 until N) {
        // This refers to how i relates to j
        val iPos = Vector2(i % width, i / width)

        // Once per row position, we compute the denominator
        val meanDiff = (binaryGrid[iPos.y][iPos.x] - mean)
        moransDen += meanDiff * meanDiff

        for (j in 0 until N) {
            // This is a 1 if the cells are neighbors (including diagonals), otherwise it's a 0
            val jPos = Vector2(j % width, j / width)

            // 2 positions are neighbors if their distance is <sqrt(2)
            val diff = (jPos - iPos)
            val dist = diff.length()

            val weight =
                    if ((i != j) && (dist < (sqrt2 + eps))) {
                        // These are neighbors
                        1
                    } else {
                        // These are not neighbors
                        0
                    }

            moransNum +=
                    weight *
                            (binaryGrid[iPos.y][iPos.x] - mean) *
                            (binaryGrid[jPos.y][jPos.x] - mean)
            W += weight
        }
    }

    return N.toDouble() / W.toDouble() * moransNum / moransDen
}

fun getBinaryGrid(positions: List<Vector2>, width: Int, height: Int): List<List<Int>> {
    // Prepare a grid with 1s in the cells that are present
    val binaryGrid = MutableList(height) { MutableList(width) { 0 } }
    for (position in positions) {
        binaryGrid[position.y][position.x] = 1
    }

    return binaryGrid
}
