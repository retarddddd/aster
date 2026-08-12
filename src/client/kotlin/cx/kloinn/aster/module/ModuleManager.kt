package cx.kloinn.aster.module

import cx.kloinn.aster.client.AsterClient
import cx.kloinn.aster.module.impl.other.TestModule
import cx.kloinn.aster.module.impl.visual.ModuleList
import com.mojang.blaze3d.platform.InputConstants
import cx.kloinn.aster.module.impl.combat.AutoTotem
import cx.kloinn.aster.module.impl.combat.Criticals
import cx.kloinn.aster.module.impl.combat.KillAura
import cx.kloinn.aster.module.impl.combat.Reach
import cx.kloinn.aster.module.impl.movement.AutoSprint
import cx.kloinn.aster.module.impl.movement.DamageBoost
import cx.kloinn.aster.module.impl.movement.FastStop
import cx.kloinn.aster.module.impl.movement.Flight
import cx.kloinn.aster.module.impl.movement.Speed
import cx.kloinn.aster.module.impl.movement.Velocity
import cx.kloinn.aster.module.impl.other.AutoConfig
import cx.kloinn.aster.module.impl.world.ChestStealer
import cx.kloinn.aster.module.impl.world.ChestStorer
import cx.kloinn.aster.module.impl.other.FlagDetector
import cx.kloinn.aster.module.impl.other.PacketLogger
import cx.kloinn.aster.module.impl.other.Timer
import cx.kloinn.aster.module.impl.visual.ChestUI
import cx.kloinn.aster.module.impl.visual.ClickGUI
import cx.kloinn.aster.module.impl.visual.ESP
import cx.kloinn.aster.module.impl.visual.HideScoreboard
import cx.kloinn.aster.module.impl.visual.LevelInfo
import cx.kloinn.aster.module.impl.world.InvManager
import cx.kloinn.aster.module.impl.world.Scaffold
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionContext
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.ChatScreen
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.screens.inventory.AnvilScreen
import net.minecraft.client.input.KeyEvent
import net.minecraft.client.input.MouseButtonInfo

class ModuleManager(val clientRef: AsterClient) {
    enum class Category {
        Movement,
        Combat,
        Visual,
        Other,
        World;

        // I could technically do Category.entries here, but I want to sort them in a specific way
        // for the click gui
        companion object {
            val ALL = listOf(
                Combat,
                Movement,
                Visual,
                World,
                Other
            )
        }
    }

    val modules: List<Module> = listOf(
        TestModule(),
        ModuleList(),
        AutoSprint(),
        AutoConfig(),
        FastStop(),
        ClickGUI(),
        PacketLogger(),
        FlagDetector(),
        Velocity(),
        Flight(),
        Speed(),
        Timer(),
        LevelInfo(),
        DamageBoost(),
        HideScoreboard(),
        ESP(),
        KillAura(),
        Criticals(),
        Scaffold(),
        Reach(),
        AutoTotem(),
        ChestStealer(),
        ChestStorer(),
        ChestUI(),
        InvManager()
    )

    fun init() {
        for (module in modules) {
            clientRef.logger.info("Initializing module ${module.name}")

            module.onInit()

            // If a module is supposed to be enabled by default, enable it.
            if (module.enabled) {
                module.onEnable()
            }
        }
    }

    fun getModule(moduleName: String, ignoreCase: Boolean = false): Module? {
        return modules.find {
            if (ignoreCase) {
                it.name.lowercase() == moduleName.lowercase()
            } else {
                it.name == moduleName
            }
        }
    }

    fun getModule(moduleName: String): Module? {
        return this.getModule(moduleName, false)
    }

    fun enableModule(moduleName: String) {
        val module = getModule(moduleName)!!

        if (module.enabled) {
            return
        }

        module.enabled = true
        module.onEnable()
    }

    fun disableModule(moduleName: String) {
        val module = getModule(moduleName)!!

        if (!module.enabled) {
            return
        }

        module.enabled = false
        module.onDisable()
    }

    fun toggleModule(moduleName: String) {
        val module = getModule(moduleName)!!
        module.enabled = !module.enabled

        if (module.enabled) {
            module.onEnable()
        } else {
            module.onDisable()
        }
    }

    fun onKeyInput(action: Int, event: KeyEvent): Boolean {
        val client = Minecraft.getInstance()
        val screen = client.gui.screen()

        if (screen is ChatScreen || screen is AnvilScreen) {
            return false
        }

        var needToCancel = false

        if (action == InputConstants.PRESS) {
            for (module in modules) {
                if (module.keybinds.any { it == event.key() }) {
                    module.enabled = !module.enabled

                    if (module.enabled) {
                        module.onEnable()
                    } else {
                        module.onDisable()
                    }

                    needToCancel = true
                }
            }
        }

        for (module in modules) {
            if (ClickGUI.isExpanded(getModule(module.name)!!)) {
                for (setting in module.getSettings()) {
                    setting.onKeyInput(action, event)
                }
            }

            if (module.enabled) {
                if (module.onKeyInput(action, event)) {
                    needToCancel = true
                }
            }
        }

        return needToCancel
    }

    fun onMouseButtonInput(action: Int, event: MouseButtonInfo) {
        for (module in modules) {
            if (module.enabled) {
                module.onMouseButtonInput(action, event)
            }
        }
    }

    fun onCursorInput(x: Int, y: Int) {
        for (module in modules) {
            if (module.enabled) {
                module.onCursorInput(x, y)
            }
        }
    }

    fun onLevelEndExtraction(context: LevelExtractionContext) {
        for (module in modules) {
            if (module.enabled) {
                module.onLevelEndExtraction(context)
            }
        }
    }

    fun onScreenAfterInit(client: Minecraft, screen: Screen, width: Int, height: Int) {
        for (module in modules) {
            if (module.enabled) {
                module.onScreenAfterInit(client, screen, width, height)
            }
        }
    }
}