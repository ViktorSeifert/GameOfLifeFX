package net.viktors.gameoflifefx.automaton

import kotlin.random.Random


abstract class SteppingCellularAutomaton<T : Enum<T>>(val rows: Int, val columns: Int) {
    data class CellData<T>(val row: Int, val column: Int, val state: T)

    abstract fun withCellData(action: (data: CellData<T>) -> Unit)

    abstract fun advance()
}

class GameOfLifeAutomaton(rows: Int, columns: Int) :
    SteppingCellularAutomaton<GameOfLifeAutomaton.CellState>(rows, columns) {
    private var grid: Array<Array<CellState>> = randomGrid(rows, columns)

    private fun randomGrid(rows: Int, columns: Int) = Array(rows) {
        Array(columns) { if (Random.nextBoolean()) CellState.ALIVE else CellState.DEAD }
    }

    enum class CellState {
        ALIVE, DEAD
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