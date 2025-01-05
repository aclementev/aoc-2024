package org.aoc.day16

import java.util.PriorityQueue
import org.aoc.util.Direction4
import org.aoc.util.Vector2
import org.aoc.util.readDataLines

val HUGE_COST = 1_000_000L

enum class Rotation {
    CLOCKWISE,
    COUNTER_CLOCKWISE;

    fun rotate(direction: Direction4): Direction4 {
        val N = Direction4.values().count()
        val nextIdx =
                when (this) {
                    CLOCKWISE -> (direction.ordinal + 1).mod(N)
                    COUNTER_CLOCKWISE -> (direction.ordinal - 1).mod(N)
                }
        return Direction4.values()[nextIdx]
    }

    companion object Factory {
        fun fromDirections(start: Direction4, end: Direction4): Rotation {
            // Since there's a modulo operation, we need to handle the edge cases
            // explicitly before using the ordinal
            return when (start to end) {
                Pair(Direction4.NORTH, Direction4.WEST) -> COUNTER_CLOCKWISE
                Pair(Direction4.WEST, Direction4.NORTH) -> CLOCKWISE
                else -> {
                    if (end.ordinal - start.ordinal > 0) {
                        CLOCKWISE
                    } else {
                        COUNTER_CLOCKWISE
                    }
                }
            }
        }
    }
}

sealed class Step() {
    data class Forward(val from: Vector2, val to: Vector2, val facing: Direction4) : Step()
    data class Rotate(val from: Vector2, val facing: Direction4, val rotation: Rotation) : Step()

    fun cost(): Long =
            when (this) {
                is Forward -> 1L
                is Rotate -> 1000L
            }
}

data class State(val position: Vector2, val facing: Direction4) {
    fun forward(): State = copy(position = position + facing.vector())
    fun rotate(rotation: Rotation): State = copy(facing = rotation.rotate(facing))
}

fun solve() {
    // part1("day16/sample.txt")
    // part1("day16/sample2.txt")
    part1("day16/input.txt")
    // part2("day16/sample.txt")
    // part2("day16/sample2.txt")
    part2("day16/input.txt")
}

fun part1(path: String) {
    val grid = readDataLines(path)
    val (start, end) = findStartEnd(grid)
    val (cost, _shortestPath) = aStar(start, end, grid)
    println(cost)
}

fun part2(path: String) {
    val grid = readDataLines(path)
    val (start, end) = findStartEnd(grid)
    // NOTE(alvaro): Not the most efficient, but gets the job done (~15s)
    val shortestPaths = findAllShortestPaths(start, end, grid)
    val uniquePositions =
            shortestPaths.flatMap { shortPath -> shortPath.map { it.position } }.toSet()
    println(uniquePositions.count() + 1) // The paths do not include the end state
}

fun findStartEnd(lines: List<String>): Pair<Vector2, Vector2> {
    var start: Vector2? = null
    var end: Vector2? = null

    for ((y, line) in lines.withIndex()) {
        for ((x, value) in line.withIndex()) {
            if (value == 'S') {
                start = Vector2(x, y)
            } else if (value == 'E') {
                end = Vector2(x, y)
            }
        }
    }

    return start!! to end!!
}

fun aStar(start: Vector2, end: Vector2, grid: List<String>): Pair<Long, List<Step>> {

    // Stores where a node came from in the current best path from start
    val cameFromMap = mutableMapOf<State, State>()

    // Stores the current known "goal score" for the best path to the start
    val scoreFromStartMap = mutableMapOf<State, Long>()
    val startingState = State(start, Direction4.EAST)
    scoreFromStartMap.set(startingState, 0L)

    // The A* heuristic function
    fun heuristic(state: State): Double = (end - state.position).length()
    // Represents our current "best guess" of the cost to get from this state to the goal
    fun fscore(state: State): Double =
            scoreFromStartMap.getOrDefault(state, HUGE_COST) + heuristic(state)

    fun reconstructPath(state: State): List<Step> {

        val steps = mutableListOf<Step>()

        var current = state
        while (current != startingState) {
            val cameFrom = cameFromMap.get(current)!!
            check(cameFrom != current)

            // Figure out which action was taken to get from states A -> B
            val step =
                    if (cameFrom.position == current.position) {
                        check(cameFrom.facing != current.facing)
                        // This must be a rotation, which can be either clockwise or
                        // counterclockwise
                        val rotation = Rotation.fromDirections(cameFrom.facing, current.facing)
                        Step.Rotate(
                                from = cameFrom.position,
                                facing = cameFrom.facing,
                                rotation = rotation,
                        )
                    } else {
                        check(cameFrom.position != current.position)
                        // This must be a forward step
                        val direction = Direction4.fromVector(current.position - cameFrom.position)
                        Step.Forward(
                                from = cameFrom.position,
                                to = current.position,
                                facing = direction
                        )
                    }
            steps.add(step)
            current = cameFrom
        }

        return steps.reversed()
    }

    val candidates = PriorityQueue<State> { a, b -> ((fscore(a) - fscore(b)) * 1000).toInt() }
    candidates.add(startingState)

    while (candidates.isNotEmpty()) {
        // Pick the candidate with the lowest tentative score
        val cand = candidates.poll()
        val candScore = scoreFromStartMap.get(cand)!!

        if (cand.position == end) {
            return candScore to reconstructPath(cand)
        }

        // Explore the actions: Forward, Rotate Clockwise, Rotate Counterclockwise
        val tentativeNewActions =
                listOf(
                        // Forward
                        cand.forward() to 1,
                        // Rotations
                        cand.rotate(Rotation.CLOCKWISE) to 1000,
                        cand.rotate(Rotation.COUNTER_CLOCKWISE) to 1000,
                )

        for ((newState, actionCost) in tentativeNewActions) {
            // Make sure this action is valid
            if (grid[newState.position.y][newState.position.x] == '#') {
                continue
            }
            val newStateScore = candScore + actionCost
            if (newStateScore < scoreFromStartMap.getOrDefault(newState, HUGE_COST)) {
                // This is the new optimal path to this state
                scoreFromStartMap.set(newState, newStateScore)
                cameFromMap.set(newState, cand)
                // Update the score of this neighbor in the priority queue
                if (candidates.contains(newState)) {
                    candidates.remove(newState)
                }
                candidates.add(newState)
            }
        }
    }

    error("No path found!")
}

fun findAllShortestPaths(start: Vector2, end: Vector2, grid: List<String>): List<List<State>> {

    // Depth First Search with Pruning
    data class Candidate(val state: State, val tentativeScore: Long, val from: Candidate? = null) {
        fun states(): List<State> {
            val result = mutableListOf<State>()
            var cand: Candidate? = this
            while (cand != null) {
                result.add(cand.state)
                cand = cand.from
            }
            return result.reversed()
        }
    }

    val bestScore = mutableMapOf<State, Long>()
    val bestPaths = mutableMapOf<State, List<Candidate>>()

    val initialState = State(start, Direction4.EAST)
    // Setup the inital condition
    bestScore.set(initialState, 0L)

    // Will be used as a stack
    val candidates = mutableListOf<Candidate>()
    candidates.addFirst(Candidate(initialState, 0L))
    while (candidates.isNotEmpty()) {

        // Explore this candidate
        val cand = candidates.removeLast()

        if (cand.state.position == end) {
            // We are done exploring this path, continue to find other possible
            // paths
            continue
        }

        // Prune this candidate if we already explored a better option
        val candScore = bestScore.get(cand.state)!!
        if (cand.tentativeScore > candScore) {
            continue
        }
        check(cand.tentativeScore == candScore)

        // Explore the actions: Forward, Rotate Clockwise, Rotate Counterclockwise
        val tentativeNewActions =
                listOf(
                        // Forward
                        cand.state.forward() to 1,
                        // Rotations
                        cand.state.rotate(Rotation.CLOCKWISE) to 1000,
                        cand.state.rotate(Rotation.COUNTER_CLOCKWISE) to 1000,
                )

        // NOTE(alvaro): The order that we want to explore them is from last to first,
        // since these will be added to a stack, hence the reversed() call
        for ((newState, actionCost) in tentativeNewActions.reversed()) {
            // Make sure this action is valid
            if (grid[newState.position.y][newState.position.x] == '#') {
                continue
            }

            val newStateScore = candScore + actionCost
            val bestNewStateScore = bestScore.getOrDefault(newState, HUGE_COST)

            if (newStateScore < bestNewStateScore) {
                // This is the new optimal path to this state
                bestScore.set(newState, newStateScore)

                // Reset the list of best paths
                bestPaths.set(newState, listOf(cand))

                // Add this action to the list of candidates
                candidates.add(Candidate(newState, newStateScore, from = cand))
            } else if (newStateScore == bestNewStateScore) {

                // Add this action to the list of optimum options
                val stateBestPaths = bestPaths.get(newState)!!
                bestPaths.set(newState, stateBestPaths + cand)

                // Add this action to the list of candidates
                candidates.add(Candidate(newState, newStateScore, from = cand))
            }
        }
    }

    // Find any paths that finish in any State at the end
    val endBestPaths =
            Direction4.values().flatMap { bestPaths.getOrElse(State(end, it)) { emptyList() } }

    val actualMinScore = endBestPaths.minOf { bestScore.get(it.state)!! }
    val actualBestPaths = endBestPaths.filter { bestScore.get(it.state)!! == actualMinScore }

    return actualBestPaths.map { it.states() }
}
