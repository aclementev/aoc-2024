package org.aoc.day11

import java.nio.file.Paths
import kotlin.io.path.readText

fun solve() {
    // part1("day11/sample.txt")
    part1("day11/input.txt")
    // part2("day11/sample.txt")
    part2("day11/input.txt")
}

fun readDataFile(path: String): String {
    // The starting path is `app`
    return Paths.get("").toAbsolutePath().getParent().resolve("data").resolve(path).readText()
}

fun part1(path: String) {
    val stones = readDataFile(path).trim().split(" ").map { it.toLong() }
    val result = applyRepeatedly(25, stones.asSequence(), ::blink)
    println(result.count())
}

fun part2(path: String) {
    val stones = readDataFile(path).trim().split(" ").map { it.toLong() }

    // Let's check if we can apply 75 times the function to a single number
    val result = countBlinkCompressed(75, stones.asSequence())
    println(result)
}

fun <T> applyRepeatedly(times: Long, input: T, transform: (T) -> T): T {
    return (1..times).fold(input) { acc, _ -> transform(acc) }
}

fun blink(stones: Sequence<Long>): Sequence<Long> {
    return stones.flatMap(::blinkElement)
}

fun countBlinkCompressed(times: Int, stones: Sequence<Long>): Long {
    var compressed = stones.groupingBy { it }.eachCount().mapValues { it.value.toLong() }
    for (i in 1..times) {
        // Compute the blink result for each compressed elements
        val stoneSeqs = compressed.map { (stone, count) -> Pair(count, blinkElement(stone)) }
        // Compress and merge the new generated sequences into a single compressed result
        val newCountedStones =
                stoneSeqs.flatMap { (count, stoneSeq) ->
                    stoneSeq.map { newStone -> Pair(count, newStone) }
                }
        compressed =
                newCountedStones.groupBy(
                                keySelector = { (_, stone) -> stone },
                                valueTransform = { (count: Long, _: Long) -> count }
                        )
                        .mapValues { it.value.sum() }
    }

    // Return the total count of all the stones
    return compressed.values.map { it }.sum()
}

fun blinkElement(value: Long): Sequence<Long> {
    return when {
        value == 0L -> sequenceOf(1)
        value.toString().length % 2 == 0 -> {
            val digits = value.toString()
            val halfpoint = digits.length / 2
            val left = digits.substring(0 until halfpoint).toLong()
            val right = digits.substring(halfpoint until digits.length).toLong()
            sequenceOf(left, right)
        }
        else -> sequenceOf(value * 2024)
    }
}
