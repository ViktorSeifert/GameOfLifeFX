package net.viktors.gameoflifefx.automaton

import kotlin.random.Random

class GameOfLifeAutomaton(val rows: Int, val columns: Int) {
    private var grid: Array<Array<CellState>> = randomGrid(rows, columns)

    private fun randomGrid(rows: Int, columns: Int) = Array(rows) {
        Array(columns) { if (Random.nextBoolean()) CellState.ALIVE else CellState.DEAD }
    }

    enum class CellState {
        ALIVE, DEAD
    }

    data class CellData(val row: Int, val column: Int, val state: CellState)

    fun withCellData(action: (data: CellData) -> Unit) {
        grid.indices.forEach { row ->
            grid[row].indices.forEach { column ->
                action(CellData(row, column, grid[row][column]))
            }
        }
    }

    fun advance() {
        grid = randomGrid(rows, columns)
    }
}