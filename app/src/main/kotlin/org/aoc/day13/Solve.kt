package org.aoc.day13

import java.nio.file.Paths
import kotlin.io.path.readText

data class Prize(val x: Long, val y: Long)

data class Button(val x: Long, val y: Long)

data class Claw(val buttonA: Button, val buttonB: Button, val prize: Prize)

data class Strategy(val aPresses: Long, val bPresses: Long)

fun solve() {
    // part1("day13/sample.txt")
    part1("day13/input.txt")
    // part2("day13/sample.txt")
    part2("day13/input.txt")
}

fun readDataFile(path: String): String {
    // The starting path is `app`
    return Paths.get("").toAbsolutePath().getParent().resolve("data").resolve(path).readText()
}

fun part1(path: String) {
    val input = readDataFile(path).trim()
    val claws = parseDescriptions(input)

    val strategies = claws.map { bruteForce(it, maxA = 100, maxB = 100) }
    // Compute the prices
    val tokens =
            strategies
                    .map {
                        if (it == null) {
                            0
                        } else {
                            it.aPresses * 3 + it.bPresses
                        }
                    }
                    .sum()
    println(tokens)
}

fun part2(path: String) {
    val input = readDataFile(path).trim()
    val CONSTANT = 10000000000000L
    val claws =
            parseDescriptions(input).map {
                // Increase the prizes by the constant
                val newPrize = it.prize.copy(x = it.prize.x + CONSTANT, y = it.prize.y + CONSTANT)
                it.copy(prize = newPrize)
            }

    // Figure out the way to win the prizes
    val strategies = claws.map(::solveIntegralEquation)

    // Compute the prices
    val tokens = strategies.map { it?.let { it.aPresses * 3 + it.bPresses } ?: 0 }
    val tokenCount = sumLong(tokens)
    println(tokenCount)
}

fun parseDescriptions(input: String): List<Claw> {
    val buttonRegex = """Button (?:A|B): X\+(\d+), Y\+(\d+)\s*""".toRegex()
    val prizeRegex = """Prize: X=(\d+), Y=(\d+)\s*""".toRegex()

    // Parse the claw description list
    val descriptions = input.split("\n\n")
    val claws =
            descriptions.map { block ->
                val lines = block.trim().split("\n")
                check(lines.count() == 3)

                // Parse Button A
                val aLine = lines[0]
                check(aLine.startsWith("Button A:"))
                val matchA = buttonRegex.matchEntire(aLine)!!
                val buttonA = Button(matchA.groupValues[1].toLong(), matchA.groupValues[2].toLong())

                // Parse Button B
                val bLine = lines[1]
                check(bLine.startsWith("Button B:"))
                val matchB = buttonRegex.matchEntire(bLine)!!
                val buttonB = Button(matchB.groupValues[1].toLong(), matchB.groupValues[2].toLong())

                // Parse Prize
                val priceLine = lines[2]
                check(priceLine.startsWith("Prize:"))
                val matchPrize = prizeRegex.matchEntire(priceLine)!!
                val prize =
                        Prize(
                                matchPrize.groupValues[1].toLong(),
                                matchPrize.groupValues[2].toLong()
                        )

                Claw(buttonA, buttonB, prize)
            }
    return claws
}

fun bruteForce(claw: Claw, maxA: Long, maxB: Long): Strategy? {
    val prize = claw.prize
    val buttonA = claw.buttonA
    val buttonB = claw.buttonB

    val strategies = mutableListOf<Strategy>()
    for (a in 0..maxA) {
        for (b in 0..maxB) {
            if ((a * buttonA.x + b * buttonB.x == prize.x) &&
                            (a * buttonA.y + b * buttonB.y == prize.y)
            ) {
                strategies.add(Strategy(a, b))
            }
        }
    }
    return strategies.minByOrNull { it.aPresses * 3 + it.bPresses }
}

fun solveIntegralEquation(claw: Claw): Strategy? {
    // This returns the (integral) solution to the equation
    val prize = claw.prize
    val buttonA = claw.buttonA
    val buttonB = claw.buttonB

    // We solve the equations:
    //          A_x * a + B_x * b = P_x
    //          A_y * a + B_y * b = P_y
    //
    // Using the matrix approach to linear equation solution, we get to

    // det(M) = A_x * B_y - B_x * A_y --> If this is 0, the system has no unique solution (or has no
    // solution)
    val det = buttonA.x * buttonB.y - buttonB.x * buttonA.y
    check(det != 0L) { "Found zero determinant ($det) for: $buttonA $buttonB" }

    // a = (B_y * P_x - B_x * P_y) / det
    val aPress = (buttonB.y * prize.x - buttonB.x * prize.y) / det
    if ((buttonB.y * prize.x - buttonB.x * prize.y) % det != 0L) {
        // This does not have an integral solution
        // println("No integral solutions due to A $claw")
        return null
    }

    // b = (A_x * P_y - A_y * P_x) / det
    val bPress = (buttonA.x * prize.y - buttonA.y * prize.x) / det
    if ((buttonA.x * prize.y - buttonA.y * prize.x) % det != 0L) {
        // This does not have an integral solution
        // println("No integral solutions due to B $claw")
        return null
    }

    return Strategy(aPress.toLong(), bPress.toLong())
}

fun sumLong(list: List<Long>): Long {
    var result = 0L
    for (element in list) {
        result += element
    }
    return result
}
