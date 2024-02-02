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

import com.almasb.fxgl.app.GameApplication
import com.almasb.fxgl.app.GameSettings
import com.almasb.fxgl.core.concurrent.Async
import com.almasb.fxgl.dsl.FXGL
import com.almasb.fxgl.dsl.getGameTimer
import com.almasb.fxgl.dsl.getInput
import com.almasb.fxgl.entity.Entity
import com.almasb.fxgl.input.UserAction
import javafx.geometry.Point2D
import javafx.scene.input.KeyCode
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.util.Duration
import net.viktors.gameoflifefx.automaton.GameOfLifeAutomaton
import net.viktors.gameoflifefx.automaton.SteppingCellularAutomaton
import java.io.InputStream
import java.io.InputStreamReader
import java.util.concurrent.atomic.AtomicBoolean

// For now leave this as aliases, later we can make the application more generic
typealias CellState = GameOfLifeAutomaton.CellState
typealias CellData = SteppingCellularAutomaton.CellData<CellState>

class GameOfLifeApp : GameApplication() {
    companion object {
        private const val CELL_VIEW_SIZE = 10.0
        private const val GRID_SIZE = 100
        private val stepInterval = Duration.seconds(1.5)

        private val INPUT_FILE_CHARSET = Charsets.UTF_16LE
    }


    private lateinit var gameState: SteppingCellularAutomaton<CellState>
    private val colorMapper: ColorMapper<CellState> = GameOfLifeColorMapper()
    private val cellEntities: MutableMap<Point2D, Entity> = mutableMapOf()

    private val shouldAdvanceAutomaton: AtomicBoolean = AtomicBoolean(false)
    private val shouldRandomizeAutomaton: AtomicBoolean = AtomicBoolean(false)

    private val playPauseAction = object : UserAction(name = "Play/Pause") {
        override fun onActionBegin() {
            shouldAdvanceAutomaton.flip()
        }
    }

    private val randomizeAction = object : UserAction(name = "Randomize") {
        override fun onActionBegin() {
            shouldRandomizeAutomaton.set(true)
        }
    }

    override fun initSettings(settings: GameSettings) {
        settings.width = GRID_SIZE * CELL_VIEW_SIZE.toInt()
        settings.height = GRID_SIZE * CELL_VIEW_SIZE.toInt()
        settings.title = "Game of life"
    }


    override fun initGame() {
        loadStateFromFile("example_patterns1.csv")

        gameState.withCellData {
            val entity = FXGL.entityBuilder()
                .at(it.column * CELL_VIEW_SIZE, it.row * CELL_VIEW_SIZE)
                .view(Rectangle(CELL_VIEW_SIZE, CELL_VIEW_SIZE, Color.WHITE))
                .buildAndAttach()

            cellEntities[stateCoordinates(it)] = entity
        }

        getGameTimer().runAtInterval(::onAutomatonStepTick, stepInterval)

        getInput().addAction(playPauseAction, KeyCode.SPACE)
        getInput().addAction(randomizeAction, KeyCode.R)
    }

    private fun onAutomatonStepTick() = Async.startAsync {
        if (shouldRandomizeAutomaton.getAndSet(false)) {
            gameState.randomize()
        } else if (shouldAdvanceAutomaton.get()) {
            gameState.advance()
        }
    }


    // leave this with the parameter, so later we can implement loading while app is running
    @Suppress("SameParameterValue")
    private fun loadStateFromFile(fileName: String) {
        val wasRunning = shouldAdvanceAutomaton.getAndSet(false)

        gameState = javaClass.getResourceAsStream(fileName).use { reader ->
            GameOfLifeAutomaton.fromInputStream(
                GRID_SIZE, GRID_SIZE, InputStreamReader(reader ?: InputStream.nullInputStream(), INPUT_FILE_CHARSET)
            )
        }

        shouldAdvanceAutomaton.set(wasRunning)
    }

    override fun onUpdate(tpf: Double) {
        gameState.withCellData {
            getViewRect(it).fill = colorMapper.viewColor(it.state)
        }
    }

    private fun stateCoordinates(cellData: CellData) = Point2D(cellData.row.toDouble(), cellData.column.toDouble())

    private fun getViewRect(cellData: CellData): Rectangle =
        cellEntities[stateCoordinates(cellData)]?.viewComponent?.children?.get(0) as Rectangle

    private fun AtomicBoolean.flip() {
        var temp: Boolean
        do {
            temp = this.get()
        } while (!this.compareAndSet(temp, !temp))
    }
}

fun main(args: Array<String>) {
    GameApplication.launch(GameOfLifeApp::class.java, args)
}
