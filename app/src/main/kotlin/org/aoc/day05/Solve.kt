package org.aoc.day05

import java.nio.file.Paths
import kotlin.io.path.readText

data class Ordering(val left: Int, val right: Int)

fun solve() {
    // part1("day05/sample.txt")
    // part1("day05/input.txt")
    // part2("day05/sample.txt")
    part2("day05/input.txt")
}

fun readDataFile(path: String): String {
    // The starting path is `app`
    return Paths.get("").toAbsolutePath().getParent().resolve("data").resolve(path).readText()
}

fun part1(path: String) {
    val (orderingStr, updateStr) = readDataFile(path).trim().split("\n\n")

    // Parse ordering
    val orderings =
            orderingStr.split("\n").map { it.split("|") }.map {
                Ordering(it[0].toInt(), it[1].toInt())
            }

    // Parse updates
    val updates = updateStr.split("\n").map { it.split(",").map { it.toInt() } }

    // Check which of the updates are ordered
    val ordered = updates.filter { isOrdered(it, orderings) }

    // Find the middle values
    val result = ordered.map { it[it.size / 2] }.sum()
    println(result)
}

fun part2(path: String) {
    val (orderingStr, updateStr) = readDataFile(path).trim().split("\n\n")

    // Parse ordering
    val orderings =
            orderingStr.split("\n").map { it.split("|") }.map {
                Ordering(it[0].toInt(), it[1].toInt())
            }

    // Parse updates
    val updates = updateStr.split("\n").map { it.split(",").map { it.toInt() } }

    // Check which of the updates are ordered
    val unordered = updates.filter { !isOrdered(it, orderings) }.map { fixOrdering(it, orderings) }

    // Find the middle values
    val result = unordered.map { it[it.size / 2] }.sum()
    println(result)
}

fun isOrdered(update: List<Int>, rules: List<Ordering>): Boolean {
    // Go through every element of the list, and check every rule
    // NOTE: This is O(n^2)
    val updateSet = update.toSet()
    return update.withIndex().all { idxValue ->
        val page = idxValue.value
        val position = idxValue.index
        rules.all { ordering ->
            if ((ordering.left != page) && (ordering.right != page)) {
                // This rule does not apply
                true
            } else {
                // Actually check the rule
                val mustBeBefore = ordering.left == page
                val other =
                        if (ordering.left == page) {
                            ordering.right
                        } else {
                            ordering.left
                        }
                val otherExists = updateSet.contains(other)
                if (!otherExists) {
                    // The other element does not exist in the update, so we can skip it
                    true
                } else {
                    // The other element exists, so check the rule
                    val otherPosition = update.indexOf(other)
                    val result =
                            (mustBeBefore && otherPosition > position) ||
                                    (!mustBeBefore && otherPosition < position)
                    result
                }
            }
        }
    }
}

fun fixOrdering(update: List<Int>, rules: List<Ordering>): List<Int> {

    val ruleComparator =
            Comparator<Int> { left: Int, right: Int ->
                // Check the ordering rules to see if left < right
                rules
                        .find {
                            (it.left == left && it.right == right) ||
                                    (it.left == right && it.right == left)
                        }
                        ?.let {
                            if (it.left == left) {
                                1
                            } else {
                                -1
                            }
                        }
                        ?: 0
            }

    return update.sortedWith(ruleComparator)
}
