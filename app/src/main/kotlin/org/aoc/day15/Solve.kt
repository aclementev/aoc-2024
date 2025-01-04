package org.aoc.day15

import kotlin.collections.ArrayDeque
import org.aoc.util.Direction4
import org.aoc.util.Vector2
import org.aoc.util.positionIsValid
import org.aoc.util.readDataFile

enum class Cell {
    WALL,
    BOX,
    EMPTY
}

enum class ExpandedCell {
    WALL,
    BOX,
    EXTRA_BOX, // Represents the right side of another cell, which can be of either type
    EMPTY,
}

typealias CellMap<T> = Map<Vector2, T>

data class Grid(val robotPos: Vector2, val cells: CellMap<Cell>, val width: Int, val height: Int) {

    fun at(pos: Vector2): Cell {
        require(positionIsValid(pos, width = width, height = height))
        return cells.getOrDefault(pos, Cell.EMPTY)
    }

    fun applyMove(direction: Direction4): Grid {
        // Since we don't allow the robot to walk into a wall, the next position
        // must be valid
        val nextPos = robotPos + direction.vector()
        val value = at(nextPos)
        return when (value) {
            Cell.WALL -> copy() // The robot does not move
            Cell.EMPTY -> copy(robotPos = nextPos) // The robot moves in that direction
            Cell.BOX -> {
                // We push the boxes directly in contact with this
                val boxesToMove =
                        generateSequence(nextPos) { it + direction.vector() }.takeWhile {
                            at(it) == Cell.BOX
                        }

                // Since we represent the positions as a set, there cannot be
                // two objects in the same cell, so we need to move the boxes
                // from furthest to closest
                val newCells = cells.toMutableMap()
                for (boxPos in boxesToMove.toList().asReversed()) {
                    val targetPos = boxPos + direction.vector()
                    val targetValue = newCells.getOrDefault(targetPos, Cell.EMPTY)
                    // Verify that the position the box is moving to is available
                    when (targetValue) {
                        Cell.WALL -> {
                            // This means that the line of boxes is blocked and we cannot move
                            return copy()
                        }
                        Cell.BOX ->
                                error(
                                        "Unreachable"
                                ) // The box at the target position should've been moved before
                        Cell.EMPTY -> {
                            // There's an empty spot to move the boxes
                            val boxValue = newCells.getOrDefault(boxPos, Cell.WALL)
                            check(boxValue == Cell.BOX)
                            newCells.remove(boxPos)
                            newCells.set(targetPos, Cell.BOX)
                        }
                    }
                }

                copy(robotPos = nextPos, cells = newCells.toMap())
            }
        }
    }

    fun pretty(): String {
        return (0 until height)
                .map { y ->
                    (0 until width)
                            .map { x ->
                                val pos = Vector2(x, y)
                                if (pos == robotPos) {
                                    '@'
                                } else {
                                    when (cells.get(pos)) {
                                        Cell.WALL -> '#'
                                        Cell.BOX -> 'O'
                                        null -> '.'
                                        else -> '?'
                                    }
                                }
                            }
                            .joinToString("")
                }
                .joinToString("\n")
    }

    fun applyMoves(moves: List<Direction4>): Grid {
        return moves.fold(this) { lastGrid, move -> lastGrid.applyMove(move) }
    }

    fun expand(): ExpandedGrid {
        val expandedCells =
                cells
                        .flatMap { (pos, value) ->
                            val newPos = pos + pos.copy(y = 0)
                            when (value) {
                                Cell.WALL ->
                                        sequenceOf(
                                                newPos to ExpandedCell.WALL,
                                                newPos + Direction4.EAST.vector() to
                                                        ExpandedCell.WALL
                                        )
                                Cell.BOX ->
                                        sequenceOf(
                                                newPos to ExpandedCell.BOX,
                                                newPos + Direction4.EAST.vector() to
                                                        ExpandedCell.EXTRA_BOX
                                        )
                                else ->
                                        error(
                                                "There should not be any other type of value during expansion: $pos -> $value"
                                        )
                            }
                        }
                        .toMap()

        return ExpandedGrid(
                robotPos = robotPos + robotPos.copy(y = 0), // Only expand on X
                cells = expandedCells,
                width = width * 2,
                height = height
        )
    }
}

data class ExpandedGrid(
        val robotPos: Vector2,
        val cells: CellMap<ExpandedCell>,
        val width: Int,
        val height: Int
) {

    fun at(pos: Vector2): ExpandedCell {
        require(positionIsValid(pos, width = width, height = height))
        return cells.getOrDefault(pos, ExpandedCell.EMPTY)
    }

    fun atSnapped(pos: Vector2): ExpandedCell {
        return at(pos).let {
            if (it == ExpandedCell.EXTRA_BOX) {
                val leftVal = cells.get(pos + Direction4.WEST.vector())
                check(leftVal == ExpandedCell.BOX)
                leftVal
            } else {
                it
            }
        }
    }

    fun applyMove(direction: Direction4): ExpandedGrid {
        val nextPos = robotPos + direction.vector()
        // Since we don't allow the robot to walk into a wall, the next position
        // must be valid
        val value = at(nextPos)
        return when (value) {
            ExpandedCell.WALL -> copy() // The robot does not move
            ExpandedCell.EMPTY -> copy(robotPos = nextPos) // The robot moves in that direction
            ExpandedCell.BOX, ExpandedCell.EXTRA_BOX -> {
                // Generate a list of all the positions of boxes that are affected
                // by the move
                val boxesToMove = sequence {
                    val yielded = mutableSetOf<Vector2>()
                    val candidates = ArrayDeque<Vector2>(listOf(nextPos))
                    while (candidates.isNotEmpty()) {
                        val candPos = candidates.removeFirst()
                        val candValue = at(candPos)
                        if ((candValue == ExpandedCell.BOX) || (candValue == ExpandedCell.EXTRA_BOX)
                        ) {
                            // This is a cell that we would need to move
                            if (!yielded.contains(candPos)) {
                                yield(candPos)
                                yielded.add(candPos)
                            }

                            // Add the next position to the candidates
                            candidates.addLast(candPos + direction.vector())

                            // NOTE(alvaro): If we are moving right / left, we don't need to
                            // explicitly consider the extra size of the boxes, they are
                            // automatically handled by mvoing straight in the direction
                            // If we are moving up or down we should check if any part
                            // of this box will push another box that may be shifted, i.e. not
                            // straight up/down
                            if ((direction == Direction4.NORTH) || (direction == Direction4.SOUTH)
                            ) {
                                // Yield the extra element
                                val otherDirection =
                                        if (candValue == ExpandedCell.BOX) {
                                            Direction4.EAST
                                        } else {
                                            Direction4.WEST
                                        }
                                val otherPos = candPos + otherDirection.vector()
                                val otherValue = at(otherPos)
                                check(
                                        (otherValue == ExpandedCell.BOX) ||
                                                ((otherValue == ExpandedCell.EXTRA_BOX))
                                )

                                if (!yielded.contains(otherPos)) {
                                    yield(otherPos)
                                    yielded.add(otherPos)
                                }

                                // Add as candidate the positions affected by the movement of the
                                // other
                                candidates.addLast(otherPos + direction.vector())
                            }
                        }
                    }
                }

                // Since we represent the positions as a set, there cannot be
                // two objects in the same cell, so we need to move the boxes
                // from furthest to closest
                val newCells = cells.toMutableMap()
                for (boxPos in boxesToMove.toList().asReversed()) {
                    val targetPos = boxPos + direction.vector()
                    val targetValue = newCells.getOrDefault(targetPos, ExpandedCell.EMPTY)
                    when (targetValue) {
                        ExpandedCell.WALL -> {
                            // This means that the line of boxes is blocked and we cannot move
                            return copy()
                        }
                        ExpandedCell.BOX, ExpandedCell.EXTRA_BOX ->
                                error(
                                        "Unreachable"
                                ) // The box at the target position should've been moved before
                        ExpandedCell.EMPTY -> {
                            // There's an empty spot to move the boxes
                            val boxValue = newCells.get(boxPos)
                            check(
                                    (boxValue == ExpandedCell.BOX) ||
                                            (boxValue == ExpandedCell.EXTRA_BOX)
                            )
                            newCells.remove(boxPos)
                            newCells.set(targetPos, boxValue!!)
                        }
                    }
                }
                copy(robotPos = nextPos, cells = newCells.toMap())
            }
        }
    }

    fun applyMoves(moves: List<Direction4>): ExpandedGrid {
        return moves.fold(this) { lastGrid, move -> lastGrid.applyMove(move) }
    }

    fun pretty(): String {
        return (0 until height)
                .map { y ->
                    (0 until width)
                            .map { x ->
                                val pos = Vector2(x, y)
                                if (pos == robotPos) {
                                    '@'
                                } else {
                                    when (cells.getOrDefault(pos, ExpandedCell.EMPTY)) {
                                        ExpandedCell.WALL -> '#'
                                        ExpandedCell.BOX -> '['
                                        ExpandedCell.EXTRA_BOX -> {
                                            val leftPos = pos + Direction4.WEST.vector()
                                            val leftValue = atSnapped(leftPos)
                                            check(leftValue == ExpandedCell.BOX)
                                            ']'
                                        }
                                        ExpandedCell.EMPTY -> '.'
                                    }
                                }
                            }
                            .joinToString("")
                }
                .joinToString("\n")
    }
}

fun solve() {
    // part1("day15/sample.txt")
    // part1("day15/sample2.txt")
    part1("day15/input.txt")
    // part2("day15/sample.txt")
    // part2("day15/sample2.txt")
    part2("day15/input.txt")
}

fun part1(path: String) {
    val inputStr = readDataFile(path)
    val (grid, moves) = parseInput(inputStr)
    val finalGrid = grid.applyMoves(moves)
    // prinln(finalGrid.pretty())
    // Compute the GPS distance
    val gps = finalGrid.cells.filter { it.value == Cell.BOX }.keys.sumOf { it.y * 100L + it.x }
    println(gps)
}

fun part2(path: String) {
    val inputStr = readDataFile(path)
    val (grid, moves) = parseInput(inputStr)
    val expandedGrid = grid.expand()
    val finalGrid = expandedGrid.applyMoves(moves)
    // println(finalGrid.pretty())
    // Compute the GPS distance (a box is measured by the distance from the top/left corner, so we
    // only care about the BOX cell)
    val gps =
            finalGrid.cells.filter { it.value == ExpandedCell.BOX }.keys.sumOf {
                it.y * 100L + it.x
            }
    println(gps)
}

fun parseInput(input: String): Pair<Grid, List<Direction4>> {
    val (mapStr, movesStr) = input.split("\n\n")
    require(mapStr.split("\n").count() > 0)

    // Parse the map
    val inputLines = mapStr.split("\n").map { it.trim() }
    val height = inputLines.count()
    val width = inputLines[0].count()

    val initialPosition = findInitialPosition(inputLines)
    val cells =
            mapPositions(inputLines) { position, value ->
                        when (value) {
                            '#' -> position to Cell.WALL
                            'O' -> position to Cell.BOX
                            else -> null
                        }
                    }
                    .filterNotNull()
                    .toMap()
    val grid = Grid(initialPosition, cells, width = width, height = height)

    // Parse the moves
    val moves =
            movesStr.replace("\n", "").map {
                when (it) {
                    '^' -> Direction4.NORTH
                    '>' -> Direction4.EAST
                    'v' -> Direction4.SOUTH
                    '<' -> Direction4.WEST
                    else -> error("invalid input value: $it")
                }
            }
    return Pair(grid, moves)
}

fun <T> mapPositions(lines: List<String>, f: (Vector2, Char) -> T): List<T> {
    // Go through all the positions in the map and return a single list with T
    return lines.withIndex().flatMap { (y, line) ->
        line.withIndex().map { (x, value) -> f(Vector2(x, y), value) }
    }
}

fun findInitialPosition(inputMap: List<String>): Vector2 {
    val (y, x, _) =
            inputMap.withIndex()
                    .flatMap { (y, line) ->
                        line.withIndex().map { (x, value) -> Triple(y, x, value) }
                    }
                    .filter { it.third == '@' }
                    .first()
    return Vector2(y, x)
}

fun moveToStr(move: Direction4): String =
        when (move) {
            Direction4.NORTH -> "^"
            Direction4.EAST -> ">"
            Direction4.SOUTH -> "v"
            Direction4.WEST -> "<"
        }

fun expandLines(lines: List<String>): List<String> {
    return lines.map {
        it.replace("#", "##").replace("O", "[]").replace(".", "..").replace("@", "@.")
    }
}
