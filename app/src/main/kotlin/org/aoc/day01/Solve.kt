package org.aoc.day01

import java.nio.file.Paths
import kotlin.io.path.readText
import kotlin.math.abs

val sample: String = """3   4
4   3
2   5
1   3
3   9
3   3
"""

fun solve() {
    // part1("day01/sample.txt")
    part1("day01/input.txt")
    // part2("day01/sample.txt")
    part2("day01/input.txt")
}

fun readDataFile(path: String): String {
    // The starting path is `app`
    return Paths.get("").toAbsolutePath().getParent().resolve("data").resolve(path).readText()
}

fun part1(path: String) {
    val lines = readDataFile(path).trim().split("\n")
    val lefts = mutableListOf<Int>()
    val rights = mutableListOf<Int>()
    for (line in lines) {
        val (leftStr, rightStr) = line.split("\\s+".toRegex(), limit = 2)
        val left = leftStr.toInt()
        val right = rightStr.toInt()

        lefts.add(left)
        rights.add(right)
    }

    lefts.sort()
    rights.sort()

    val distances = lefts.zip(rights).sumOf { (left, right) -> abs(left - right) }
    println(distances)
}

fun part2(path: String) {
    val lines = readDataFile(path).trim().split("\n")
    val lefts = mutableListOf<Int>()
    val rights = mutableListOf<Int>()
    for (line in lines) {
        val (leftStr, rightStr) = line.split("\\s+".toRegex(), limit = 2)
        val left = leftStr.toInt()
        val right = rightStr.toInt()

        lefts.add(left)
        rights.add(right)
    }

    // Create a frequency map for the number of occurrences on the right
    val freqs = rights.groupingBy { it }.eachCount()
    val score = lefts.sumOf { it * (freqs.get(it) ?: 0) }
    println(score)
}
