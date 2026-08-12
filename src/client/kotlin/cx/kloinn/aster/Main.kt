package cx.kloinn.aster

import cx.kloinn.aster.client.AsterClient
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionEvents
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents

class Main : ClientModInitializer {
    override fun onInitializeClient() {
        ClientLifecycleEvents.CLIENT_STARTED.register {
            val client = AsterClient()

            client.logger.info("About to initialize client")
            client.init()

            LevelExtractionEvents.END_EXTRACTION.register { context ->
                client.moduleManager.onLevelEndExtraction(context)
            }

            ScreenEvents.AFTER_INIT.register { minecraft, screen, width, height ->
                client.moduleManager.onScreenAfterInit(minecraft, screen, width, height)
            }
        }
    }
}
