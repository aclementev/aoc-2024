package org.aoc.day02

import java.nio.file.Paths
import kotlin.io.path.readText
import kotlin.math.abs

fun solve() {
    // part1("day02/sample.txt")
    part1("day02/input.txt")
    // part2("day02/sample.txt")
    part2("day02/input.txt")
}

fun readDataFile(path: String): String {
    // The starting path is `app`
    return Paths.get("").toAbsolutePath().getParent().resolve("data").resolve(path).readText()
}

fun isReportSafe(report: List<Int>): Boolean {
    assert(report.size > 1)
    // Peek to see what direction the array should be sorted at
    val isAscending = report[1] > report[0]

    return report.zipWithNext().all { (left, right) ->
        // Check if the list is sorted
        val isSorted = ((isAscending && (right > left)) || (!isAscending && (right < left)))
        //
        // Check if the difference is in the range
        val diff = abs(left - right)
        val isDifferenceInRange = (diff > 0) && (diff < 4)

        isSorted && isDifferenceInRange
    }
}

fun isReportSafeWithTolerance(report: List<Int>): Boolean {
    // Check if the report if safe unmodified
    if (isReportSafe(report)) {
        return true
    }

    // Go through the elements and check if removing one works
    for (i in report.indices) {
        val newReport = report.filterIndexed { idx, _ -> idx != i }
        if (isReportSafe(newReport)) {
            return true
        }
    }
    return false
}

fun part1(path: String) {
    val lines = readDataFile(path).trim().split("\n")
    val result = lines.map { it.split(" ").map { it.toInt() } }.filter(::isReportSafe).count()
    println(result)
}

fun part2(path: String) {
    val lines = readDataFile(path).trim().split("\n")
    val result =
            lines
                    .map { it.split(" ").map { it.toInt() } }
                    .filter(::isReportSafeWithTolerance)
                    .count()
    println(result)
}
