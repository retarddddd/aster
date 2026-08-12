package cx.kloinn.aster.module.impl.visual

import cx.kloinn.aster.client.AsterClient
import cx.kloinn.aster.module.Module
import cx.kloinn.aster.module.ModuleManager
import cx.kloinn.aster.module.setting.Setting
import net.fabricmc.fabric.api.client.screen.v1.Screens
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.screens.inventory.ContainerScreen
import net.minecraft.client.input.KeyEvent
import net.minecraft.network.chat.Component
import org.lwjgl.glfw.GLFW

class ChestUI : Module() {
    override val name = "Chest UI"
    override val keybinds: ArrayList<Int> = arrayListOf()
    override var enabled = false
    override val category = ModuleManager.Category.Visual
    override val settings: ArrayList<Setting> = arrayListOf()

    private var wasChestStorerEnabled = false
    private var wasChestStealerEnabled = false

    override fun onScreenAfterInit(client: Minecraft, screen: Screen, width: Int, height: Int) {
        if (screen is ContainerScreen) {
            val stealButton =
                Button.builder(
                    Component.literal("Steal"),
                    {
                        wasChestStealerEnabled = true

                        AsterClient.SINGLETON.moduleManager.disableModule("Chest Storer")
                        AsterClient.SINGLETON.moduleManager.toggleModule("Chest Stealer")
                    }
                )
                    .bounds(10, 10, 80, 20)
                    .build()

            stealButton.setOverrideRenderHighlightedSprite { stealButton.isHovered }

            Screens.getWidgets(screen).add(stealButton)

            val storeButton =
                Button.builder(
                    Component.literal("Store"),
                    {
                        wasChestStorerEnabled = true

                        AsterClient.SINGLETON.moduleManager.disableModule("Chest Stealer")
                        AsterClient.SINGLETON.moduleManager.toggleModule("Chest Storer")
                    }
                )
                    .bounds(10, 20 + 15 /* height of steal button + 15 pixel padding */, 80, 20)
                    .build()


            storeButton.setOverrideRenderHighlightedSprite { storeButton.isHovered }

            Screens.getWidgets(screen).add(storeButton)
        }
    }

    override fun onKeyInput(action: Int, event: KeyEvent): Boolean {
        if (event.key == GLFW.GLFW_KEY_ESCAPE) {
            if (wasChestStealerEnabled) {
                AsterClient.SINGLETON.moduleManager.disableModule("Chest Stealer")
                wasChestStealerEnabled = false
            }

            if (wasChestStorerEnabled) {
                AsterClient.SINGLETON.moduleManager.disableModule("Chest Storer")
                wasChestStorerEnabled = false
            }
        }

        return false
    }

    override fun onDisable() {
        wasChestStorerEnabled = false
        wasChestStealerEnabled = false
    }
}