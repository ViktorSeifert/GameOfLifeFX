package net.viktors.gameoflifefx

import com.almasb.fxgl.app.GameApplication
import com.almasb.fxgl.app.GameSettings

class GameOfLifeApp : GameApplication() {
    override fun initSettings(settings: GameSettings) {
        settings.width = 800
        settings.height = 600
        settings.title = "BasicGameSample"
    }
}

fun main(args: Array<String>) {
    GameApplication.launch(GameOfLifeApp::class.java, args)
}
