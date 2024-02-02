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

package net.viktors.gameoflifefx

import javafx.scene.paint.Color
import net.viktors.gameoflifefx.automaton.GameOfLifeAutomaton

abstract class ColorMapper<T: Enum<T>> {
    abstract fun viewColor(value: T): Color
}

class GameOfLifeColorMapper: ColorMapper<GameOfLifeAutomaton.CellState>() {
    override fun viewColor(value: GameOfLifeAutomaton.CellState): Color = when (value) {
        GameOfLifeAutomaton.CellState.ALIVE -> Color.GREEN
        GameOfLifeAutomaton.CellState.DEAD -> Color.LIGHTGREY
    }
}
