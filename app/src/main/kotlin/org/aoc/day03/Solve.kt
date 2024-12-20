package org.aoc.day03

import java.nio.file.Paths
import kotlin.io.path.readText

data class Multiply(val left: Int, val right: Int) {
    fun execute(): Int = left * right
}

fun solve() {
    // part1("day03/sample.txt")
    part1("day03/input.txt")
    // part2("day03/sample2.txt")
    part2("day03/input.txt")
}

fun readDataFile(path: String): String {
    // The starting path is `app`
    return Paths.get("").toAbsolutePath().getParent().resolve("data").resolve(path).readText()
}

fun scanMultiplies(line: String): List<Multiply> {
    // Goes through the line looking for valid multiply instructions
    val regex = Regex("""mul\((\d{1,3}),(\d{1,3})\)""")
    return regex.findAll(line)
            .map { Multiply(it.groupValues[1].toInt(), it.groupValues[2].toInt()) }
            .toList()
}

fun scanWithConditionals(input: String): List<Multiply> {
    var enabled = true
    var multiplies = mutableListOf<Multiply>()
    var rest: CharSequence = input
    val mulRegex = Regex("""mul\((\d{1,3}),(\d{1,3})\)""")

    while (rest.isNotEmpty()) {
        // Scan the rest of the input until we see the next operation
        when (rest[0]) {
            'm' -> {
                // This could be a `mul`, so we can try to parse it
                if (enabled) {
                    val match = mulRegex.matchAt(rest, 0)
                    if (match != null) {
                        multiplies.add(
                                Multiply(match.groupValues[1].toInt(), match.groupValues[2].toInt())
                        )
                        rest = rest.subSequence(match.value.length, rest.length)
                    } else {
                        rest = rest.subSequence(1, rest.length)
                    }
                } else {
                    rest = rest.subSequence(1, rest.length)
                }
            }
            'd' -> {
                // This could be a `do` or `dont`
                if (rest.startsWith("do()")) {
                    enabled = true
                    rest = rest.subSequence(4, rest.length)
                } else if (rest.startsWith("don't()")) {
                    enabled = false
                    rest = rest.subSequence(7, rest.length)
                } else {
                    rest = rest.subSequence(1, rest.length)
                }
            }
            else -> {
                // Skip to the next element
                rest = rest.subSequence(1, rest.length)
            }
        }
    }

    return multiplies.toList()
}

fun part1(path: String) {
    val input = readDataFile(path).trim()
    val results = scanMultiplies(input).map { it.execute() }.sum()
    println(results)
}

fun part2(path: String) {
    val input = readDataFile(path).trim()
    val results = scanWithConditionals(input).map { it.execute() }.sum()
    println(results)
}
