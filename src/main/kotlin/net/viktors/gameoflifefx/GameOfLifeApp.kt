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
        private val stepInterval = Duration.seconds(2.0)

        private val INPUT_FILE_CHARSET = Charsets.UTF_16LE
    }


    private lateinit var gameState: SteppingCellularAutomaton<CellState>
    private val colorMapper: ColorMapper<CellState> = GameOfLifeColorMapper()
    private val cellEntities: MutableMap<Point2D, Entity> = mutableMapOf()

    private lateinit var periodicUpdateAction: TimerAction
    private var shouldAdvanceAutomaton: AtomicBoolean = AtomicBoolean(false)

    private val playPauseAction = object : UserAction(name = "Play/Pause") {
        override fun onActionBegin() {
            shouldAdvanceAutomaton.flip()
        }
    }

    override fun initSettings(settings: GameSettings) {
        settings.width = GRID_SIZE * CELL_VIEW_SIZE.toInt()
        settings.height = GRID_SIZE * CELL_VIEW_SIZE.toInt()
        settings.title = "Game of life"
    }


    override fun initGame() {
        loadStateFromFile("simple_sample2.csv")

        gameState.withCellData {
            val entity = FXGL.entityBuilder()
                .at(it.column * CELL_VIEW_SIZE, it.row * CELL_VIEW_SIZE)
                .view(Rectangle(CELL_VIEW_SIZE, CELL_VIEW_SIZE, Color.WHITE))
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
