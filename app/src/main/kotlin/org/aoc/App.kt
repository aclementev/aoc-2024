/*
 * This source file was generated by the Gradle 'init' task
 */
package org.aoc

import kotlin.system.exitProcess
import org.aoc.day01.solve as solveDay01

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        System.err.println("error: you must provide a day to run")
        exitProcess(1)
    }

    val day = args[0].toInt()

    when (day) {
        1 -> solveDay01()
        else -> error("day ${day} not implemented")
    }
    println(args[0])
}
