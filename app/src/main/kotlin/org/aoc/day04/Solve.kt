package org.aoc.day04

import java.nio.file.Paths
import kotlin.io.path.readText
import kotlin.math.max
import kotlin.math.min

typealias Pattern = Array<Array<Char>>

fun solve() {
    // part1("day04/sample.txt")
    // part1("day04/input.txt")
    // part2("day04/sample.txt")
    part2("day04/input.txt")
}

fun readDataFile(path: String): String {
    // The starting path is `app`
    return Paths.get("").toAbsolutePath().getParent().resolve("data").resolve(path).readText()
}

fun part1(path: String) {
    val grid = readDataFile(path).trim().lowercase()

    require(grid.isNotEmpty())
    grid.split("\n").let {
        // The grid must be squared
        require(it.size == it[0].length)
    }

    val candidates = getCandidateLines(grid)
    val result = candidates.map(::countLineXmas).sum()
    println(result)
}

fun part2(path: String) {
    val grid = readDataFile(path).trim().lowercase()

    require(grid.isNotEmpty())
    grid.split("\n").let {
        // The grid must be squared
        require(it.size == it[0].length)
    }

    // NOTE: The '.' is a special character that matches any character
    val patterns =
            arrayOf(
                    arrayOf(
                            arrayOf('m', '.', 'm'),
                            arrayOf('.', 'a', '.'),
                            arrayOf('s', '.', 's'),
                    ),
                    arrayOf(
                            arrayOf('s', '.', 'm'),
                            arrayOf('.', 'a', '.'),
                            arrayOf('s', '.', 'm'),
                    ),
                    arrayOf(
                            arrayOf('s', '.', 's'),
                            arrayOf('.', 'a', '.'),
                            arrayOf('m', '.', 'm'),
                    ),
                    arrayOf(
                            arrayOf('m', '.', 's'),
                            arrayOf('.', 'a', '.'),
                            arrayOf('m', '.', 's'),
                    ),
            )

    val matches = scan2DPatterns(grid, patterns)
    println(matches)
}

fun getCandidateLines(grid: String): List<String> {
    val lines = grid.split("\n")
    val gridSize = lines[0].length

    // The candidates are
    //  - Horizontal
    val horizontal = lines

    //  - Vertical
    val vertical = (0 until gridSize).map { col -> lines.map { it[col] }.joinToString("") }

    //  - Diagonal (right)
    val diagRight = diagonalRight(lines)

    //  - Diagonal (left)
    val diagLeft = diagonalLeft(lines)

    //  - All in reverse
    val candidates = horizontal + vertical + diagRight + diagLeft

    val reversedCandidates = candidates.map { it.reversed().toString() }

    return candidates + reversedCandidates
}

fun countLineXmas(line: String): Int {
    return max(line.split("xmas").size - 1, 0)
}

fun diagonalRight(lines: List<String>): List<String> {
    val gridSize = lines[0].length
    // To get all the diagonals "to the right", we need to generate a diagonal
    // starting at every element from the left edge and top edges of the grid
    val traceDiagonal = { startX: Int, startY: Int ->
        val diagLength = min(gridSize - startX, gridSize - startY)
        (0 until diagLength).map { lines[startY + it][startX + it] }.joinToString("")
    }

    val leftEdgeCandidates =
            (0 until gridSize).map { traceDiagonal(0, it) }.filter { it.length > 3 }
    // NOTE(alvaro): We start at one since we already processed the top/left corner
    val topEdgeCandidates = (1 until gridSize).map { traceDiagonal(it, 0) }.filter { it.length > 3 }
    return leftEdgeCandidates + topEdgeCandidates
}

fun diagonalLeft(lines: List<String>): List<String> {
    val gridSize = lines[0].length
    // To get all the diagonals "to the left", we need to generate a diagonal
    // starting at every element from the top edge and right edges of the grid
    val traceDiagonal = { startX: Int, startY: Int ->
        val diagLength = min(startX + 1, gridSize - startY)
        (0 until diagLength).map { lines[startY + it][startX - it] }.joinToString("")
    }

    val topEdgeCandidates = (0 until gridSize).map { traceDiagonal(it, 0) }.filter { it.length > 3 }
    // NOTE(alvaro): We start at one since we already processed the top/right corner
    val rightEdgeCandidates =
            (1 until gridSize).map { traceDiagonal(gridSize - 1, it) }.filter { it.length > 3 }
    return topEdgeCandidates + rightEdgeCandidates
}

fun scan2DPatterns(grid: String, patterns: Array<Pattern>): Int {
    // There must be some pattern, which must be a squared array
    require(patterns.size > 0)
    val patternSize = patterns[0].size
    require(patterns.all { it.size == patternSize })
    require(patterns.all { pattern -> pattern.all { it.size == patternSize } })

    // Go through each position of the grid, and check if it matches either of the patterns
    val lines = grid.split("\n")
    val gridSize = lines[0].length

    var count = 0
    for (y in 0 until gridSize - patternSize + 1) {
        val dataLines = lines.subList(y, y + patternSize)
        for (x in 0 until gridSize - patternSize + 1) {
            val data = dataLines.map { it.subSequence(x, x + patternSize) }
            if (matchesPatterns(data, patterns)) {
                count += 1
            }
        }
    }
    return count
}

fun matchesPatterns(data: List<CharSequence>, patterns: Array<Pattern>): Boolean {
    return patterns.any { pattern ->
        // Check that all lines match
        data.zip(pattern).all { (dataLine, patternLine) ->
            dataLine.zip(String(patternLine.toCharArray())).all { (left, right) ->
                (right == '.') || (left == right)
            }
        }
    }
}
