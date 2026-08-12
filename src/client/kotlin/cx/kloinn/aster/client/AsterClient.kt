package cx.kloinn.aster.client

import com.mojang.logging.LogUtils
import cx.kloinn.aster.command.CommandManager
import cx.kloinn.aster.module.ModuleManager
import cx.kloinn.aster.utils.LoggerWrapper

class AsterClient {
    companion object { lateinit var SINGLETON: AsterClient }

    val logger = LoggerWrapper(LogUtils.getLogger())

    val inputManager = InputManager(this)
    val moduleManager = ModuleManager(this)
    val commandManager = CommandManager()
    val configManager = ConfigManager()

    val gameHelpers = GameHelpers()

    fun init() {
        SINGLETON = this

        logger.info("Init called")

        this.moduleManager.init()
        this.configManager.init()
    }

    fun inputTick() {
        this.inputManager.tick()
    }

    fun tick() {
        this.gameHelpers.tick()

        val modules = this.moduleManager.modules

        for (module in modules) {
            if (!module.enabled) continue

            module.onTick()
        }
    }
}