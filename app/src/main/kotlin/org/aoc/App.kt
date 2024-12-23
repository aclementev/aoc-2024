/*
 * This source file was generated by the Gradle 'init' task
 */
package org.aoc

import kotlin.system.exitProcess
import org.aoc.day01.solve as solveDay01
import org.aoc.day02.solve as solveDay02
import org.aoc.day03.solve as solveDay03
import org.aoc.day04.solve as solveDay04
import org.aoc.day05.solve as solveDay05

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        System.err.println("error: you must provide a day to run")
        exitProcess(1)
    }

    val day = args[0].toInt()

    when (day) {
        1 -> solveDay01()
        2 -> solveDay02()
        3 -> solveDay03()
        4 -> solveDay04()
        5 -> solveDay05()
        else -> error("day ${day} not implemented")
    }
}
