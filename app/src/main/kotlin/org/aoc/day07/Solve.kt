package org.aoc.day07

import java.nio.file.Paths
import kotlin.io.path.readText

sealed class Expression {
    data class AsLong(val value: Long) : Expression()
    data class AsExpression(val left: Expression, val op: Operator, val right: Long) : Expression()

    fun eval(): Long =
            when (this) {
                is AsLong -> this.value
                is AsExpression -> this.op.eval(left.eval(), right)
            }
}

enum class Operator {
    PLUS,
    MULTIPLY,
    CONCAT;

    fun eval(left: Long, right: Long): Long =
            when (this) {
                PLUS -> left + right
                MULTIPLY -> left * right
                CONCAT -> (left.toString() + right.toString()).toLong()
            }
}

data class IncompleteEquation(val left: Long, val operands: List<Long>)

data class Equation(val left: Long, val right: Expression) {
    fun isValid(): Boolean = left == right.eval()
}

fun solve() {
    // part1("day07/sample.txt")
    part1("day07/input.txt")
    // part2("day07/sample.txt")
    part2("day07/input.txt")
}

fun readDataFile(path: String): String {
    // The starting path is `app`
    return Paths.get("").toAbsolutePath().getParent().resolve("data").resolve(path).readText()
}

fun part1(path: String) {
    // Parse the input
    val lines = readDataFile(path).trim().split("\n")
    val operations = lines.map(::parseIncompleteEquation)
    val validOperators = listOf(Operator.PLUS, Operator.MULTIPLY)
    val validOperations = operations.map { completeEquation(it, validOperators) }.filterNotNull()
    val result = validOperations.sumOf { it.left }
    println(result)
}

fun part2(path: String) {
    // Parse the input
    val lines = readDataFile(path).trim().split("\n")
    val operations = lines.map(::parseIncompleteEquation)
    val validOperators = listOf(Operator.PLUS, Operator.MULTIPLY, Operator.CONCAT)
    val validOperations = operations.map { completeEquation(it, validOperators) }.filterNotNull()
    val result = validOperations.sumOf { it.left }
    println(result)
}

fun parseIncompleteEquation(input: String): IncompleteEquation {
    val (left, right) = input.split(":")

    val operands = right.trim().split(" ").map { it.toLong() }
    return IncompleteEquation(left.toLong(), operands)
}

fun completeEquation(equation: IncompleteEquation, validOperators: List<Operator>): Equation? {
    require(equation.operands.size > 1) // We don't support unary ops

    // The `rest` contains the operands
    data class Candidate(val expr: Expression, val rest: List<Long>)

    // Initialize the queue of candidates
    val op1 = equation.operands[0]
    val op2 = equation.operands[1]
    val rest = equation.operands.subList(2, equation.operands.size)
    var candidates =
            ArrayDeque(
                    validOperators.map { operator ->
                        Candidate(
                                Expression.AsExpression(Expression.AsLong(op1), operator, op2),
                                rest
                        )
                    }
            )

    // Depth-first search
    while (candidates.isNotEmpty()) {
        val cand = candidates.removeFirst()

        if (cand.rest.isNotEmpty()) {
            // This candidate is not ready for validation, so generate new candidates
            // by exploring all valid operators

            val newOperand = cand.rest[0]
            val newRest = cand.rest.subList(1, cand.rest.size)

            validOperators.forEach { operator ->
                candidates.addFirst(
                        Candidate(Expression.AsExpression(cand.expr, operator, newOperand), newRest)
                )
            }
            continue
        }

        // The expression is complete, so we can evaluate it
        if (equation.left == cand.expr.eval()) {
            // This is a valid equation, so we can stop
            return Equation(equation.left, cand.expr)
        }
    }

    // We could not find a valid equation, so this is null
    return null
}
