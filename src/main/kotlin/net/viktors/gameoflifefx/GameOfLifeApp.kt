package net.viktors.gameoflifefx

import com.almasb.fxgl.app.GameApplication
import com.almasb.fxgl.app.GameSettings
import com.almasb.fxgl.core.concurrent.Async
import com.almasb.fxgl.dsl.FXGL
import com.almasb.fxgl.dsl.getGameTimer
import com.almasb.fxgl.dsl.getInput
import com.almasb.fxgl.entity.Entity
import com.almasb.fxgl.input.UserAction
import com.almasb.fxgl.time.TimerAction
import javafx.geometry.Point2D
import javafx.scene.input.KeyCode
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.util.Duration
import net.viktors.gameoflifefx.automaton.GameOfLifeAutomaton
import net.viktors.gameoflifefx.automaton.SteppingCellularAutomaton
import java.util.concurrent.atomic.AtomicBoolean

// For now leave this as aliases, later we can make the application more generic
typealias CellState = GameOfLifeAutomaton.CellState
typealias CellData = SteppingCellularAutomaton.CellData<CellState>

class GameOfLifeApp : GameApplication() {
    private val cellViewSize = 10.0
    private val stepInterval = Duration.seconds(2.0)

    private val gameState: SteppingCellularAutomaton<CellState> = GameOfLifeAutomaton(100, 100)
    private val colorMapper: ColorMapper<CellState> = GameOfLifeColorMapper()
    private val cellEntities: MutableMap<Point2D, Entity> = mutableMapOf()

    private lateinit var periodicUpdateAction: TimerAction
    private var shouldAdvanceAutomaton: AtomicBoolean = AtomicBoolean(false)

    private val playPauseAction = object: UserAction(name = "Play/Pause") {
        override fun onActionBegin() {
            shouldAdvanceAutomaton.flip()
        }
    }

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
            Async.startAsync {
                if (shouldAdvanceAutomaton.get()) {
                    gameState.advance()
                }
            }
        }, stepInterval)

        getInput().addAction(playPauseAction, KeyCode.SPACE)
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
