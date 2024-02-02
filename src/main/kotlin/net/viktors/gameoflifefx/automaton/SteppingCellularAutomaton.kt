/*
Game Of Life JavaFXGL
Copyright (C) 2024  Viktor Seifert

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package net.viktors.gameoflifefx.automaton

import net.viktors.gameoflifefx.concurrent.Mutex
import org.apache.commons.csv.CSVFormat
import java.io.InputStreamReader
import kotlin.random.Random


abstract class SteppingCellularAutomaton<T : Enum<T>>(val rows: Int, val columns: Int) {
    data class CellData<T>(val row: Int, val column: Int, val state: T)

    abstract fun withCellData(action: (data: CellData<T>) -> Unit)

    abstract fun advance()

    abstract fun randomize()
}

class GameOfLifeAutomaton private constructor(
    rows: Int, columns: Int, grid: Array<Array<CellState>>
) : SteppingCellularAutomaton<GameOfLifeAutomaton.CellState>(rows, columns) {
    constructor(rows: Int, columns: Int) : this(rows, columns, randomGrid(rows, columns))

    private var currentGrid = grid
    private var nextGrid = randomGrid(rows, columns)
    private val gridMutex = Mutex()

    enum class CellState(private val csvRepresentation: Int) {
        ALIVE(1), DEAD(0);

        companion object {
            private const val DEFAULT_CSV_VALUE = 0
            val DEFAULT_VALUE = DEAD

            fun fromCSVString(input: String): CellState {
                val numberRepresentation = input.toIntOrNull() ?: DEFAULT_CSV_VALUE

                for (v in entries) {
                    if (v.csvRepresentation == numberRepresentation) {
                        return v
                    }
                }

                throw IllegalArgumentException("$this.javaClass.name has no value for the number $numberRepresentation")
            }
        }
    }

    override fun withCellData(action: (data: CellData<CellState>) -> Unit) {
        gridMutex.withLock {
            currentGrid.indices.forEach { row ->
                currentGrid[row].indices.forEach { column ->
                    action(CellData(row, column, currentGrid[row][column]))
                }
            }
        }
    }

    override fun advance() {
        nextGrid.indices.forEach { row ->
            nextGrid[row].indices.forEach { column ->
                val numberOfLiveNeighbors = numberOfLiveNeighbors(row, column)
                val alive = isAlive(row, column)
                val nextState = if (numberOfLiveNeighbors in 2..3 && alive || //
                    numberOfLiveNeighbors == 3
                ) CellState.ALIVE
                else CellState.DEAD
                nextGrid[row][column] = nextState
            }
        }

        gridMutex.withLock {
            val temp = currentGrid
            currentGrid = nextGrid
            nextGrid = temp
        }
    }

    private fun numberOfLiveNeighbors(row: Int, column: Int): Int {
        var result = 0

        // top
        if (isAlive(row - 1, column)) result += 1
        // top-right
        if (isAlive(row - 1, column + 1)) result += 1
        // right
        if (isAlive(row, column + 1)) result += 1
        // bottom-right
        if (isAlive(row + 1, column + 1)) result += 1
        // bottom
        if (isAlive(row + 1, column)) result += 1
        // bottom-left
        if (isAlive(row + 1, column - 1)) result += 1
        // left
        if (isAlive(row, column - 1)) result += 1
        // top-left
        if (isAlive(row - 1, column - 1)) result += 1

        return result
    }

    private fun isAlive(row: Int, column: Int): Boolean =
        row in 0..<rows && column in 0..<columns && currentGrid[row][column] == CellState.ALIVE


    override fun randomize() {
        val randomGrid = randomGrid(rows, columns)

        gridMutex.withLock { currentGrid = randomGrid }
    }

    companion object {
        private val csvFormat: CSVFormat = CSVFormat.DEFAULT.builder().setAllowMissingColumnNames(true).build()

        private fun randomGrid(rows: Int, columns: Int) = Array(rows) {
            Array(columns) { if (Random.nextBoolean()) CellState.ALIVE else CellState.DEAD }
        }

        fun fromInputStream(rows: Int, columns: Int, streamReader: InputStreamReader): GameOfLifeAutomaton {
            val result = mutableListOf<Array<CellState>>()

            for (record in csvFormat.parse(streamReader)) {
                val row = (0..<record.size()).map { CellState.fromCSVString(record[it]) }

                while (row.size != columns) {
                    row.addLast(CellState.DEFAULT_VALUE)
                }

                result.add(row.toTypedArray())
            }

            while (result.size != rows) {
                result.add(Array(columns) { CellState.DEFAULT_VALUE })
            }

            return GameOfLifeAutomaton(rows, columns, result.toTypedArray())
        }
    }
}