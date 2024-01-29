package net.viktors.gameoflifefx

import com.almasb.fxgl.app.GameApplication
import com.almasb.fxgl.app.GameSettings
import com.almasb.fxgl.core.concurrent.Async
import com.almasb.fxgl.dsl.FXGL
import com.almasb.fxgl.dsl.getGameTimer
import com.almasb.fxgl.entity.Entity
import com.almasb.fxgl.time.TimerAction
import javafx.geometry.Point2D
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.util.Duration
import net.viktors.gameoflifefx.automaton.GameOfLifeAutomaton

class GameOfLifeApp : GameApplication() {
    private val cellViewSize = 10.0
    private val stepInterval = Duration.seconds(2.0)

    private val gameState: GameOfLifeAutomaton = GameOfLifeAutomaton(100, 100)
    private val cellEntities: MutableMap<Point2D, Entity> = mutableMapOf()

    private lateinit var periodicUpdateAction: TimerAction

    override fun initSettings(settings: GameSettings) {
        settings.width = gameState.columns * cellViewSize.toInt()
        settings.height = gameState.rows * cellViewSize.toInt()
        settings.title = "Game of life"
    }

    override fun initGame() {
        gameState.withCellData {
            val entity = FXGL.entityBuilder()
                .at(it.column * cellViewSize, it.row * cellViewSize)
                .view(Rectangle(cellViewSize, cellViewSize, Color.WHITE))
                .buildAndAttach()

            cellEntities[stateCoordinates(it)] = entity
        }

        periodicUpdateAction = getGameTimer().runAtInterval({
            Async.startAsync { gameState.advance() }
        }, stepInterval)
    }

    private fun getColor(state: GameOfLifeAutomaton.CellState): Color = when (state) {
        GameOfLifeAutomaton.CellState.ALIVE -> Color.GREEN
        GameOfLifeAutomaton.CellState.DEAD -> Color.BLACK
    }

    override fun onUpdate(tpf: Double) {
        gameState.withCellData {
            getViewRect(it).fill = getColor(it.state)
        }
    }

    private fun stateCoordinates(cellData: GameOfLifeAutomaton.CellData) =
        Point2D(cellData.row.toDouble(), cellData.column.toDouble())

    private fun getViewRect(cellData: GameOfLifeAutomaton.CellData): Rectangle =
        cellEntities[stateCoordinates(cellData)]?.viewComponent?.children?.get(0) as Rectangle
}

fun main(args: Array<String>) {
    GameApplication.launch(GameOfLifeApp::class.java, args)
}
