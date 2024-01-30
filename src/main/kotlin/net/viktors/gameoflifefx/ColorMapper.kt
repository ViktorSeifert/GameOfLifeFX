package net.viktors.gameoflifefx

import javafx.scene.paint.Color
import net.viktors.gameoflifefx.automaton.GameOfLifeAutomaton

abstract class ColorMapper<T: Enum<T>> {
    abstract fun viewColor(value: T): Color
}

class GameOfLifeColorMapper: ColorMapper<GameOfLifeAutomaton.CellState>() {
    override fun viewColor(value: GameOfLifeAutomaton.CellState) = when (value) {
        GameOfLifeAutomaton.CellState.ALIVE -> Color.GREEN
        GameOfLifeAutomaton.CellState.DEAD -> Color.BLACK
    }
}
