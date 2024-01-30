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

import org.apache.commons.csv.CSVFormat
import java.io.InputStreamReader
import kotlin.random.Random


abstract class SteppingCellularAutomaton<T : Enum<T>>(val rows: Int, val columns: Int) {
    data class CellData<T>(val row: Int, val column: Int, val state: T)

    abstract fun withCellData(action: (data: CellData<T>) -> Unit)

    abstract fun advance()
}

class GameOfLifeAutomaton(
    rows: Int, columns: Int, private var grid: Array<Array<CellState>> = randomGrid(rows, columns)
) : SteppingCellularAutomaton<GameOfLifeAutomaton.CellState>(rows, columns) {
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
        grid.indices.forEach { row ->
            grid[row].indices.forEach { column ->
                action(CellData(row, column, grid[row][column]))
            }
        }
    }

    override fun advance() {
        grid = randomGrid(rows, columns)
    }
}