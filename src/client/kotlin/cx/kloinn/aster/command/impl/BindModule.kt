package cx.kloinn.aster.command.impl

import cx.kloinn.aster.client.AsterClient
import cx.kloinn.aster.command.Command
import cx.kloinn.aster.utils.ChatUtils
import cx.kloinn.aster.utils.Utils

class BindModule : Command() {
    override val names: List<String> = listOf("b", "bind")
    override val description: String = "Binds a module to a specific key"
    override val helpMessage: String = "<module name> <key>"

    override fun runCommand(args: List<String>) {
        if (args.size != 2) {
            ChatUtils.sendErrorMessage("Usage: .bind ${this.helpMessage}")
            return
        }

        val moduleName = args[0]
        val key = args[1]

        val moduleManager = AsterClient.SINGLETON.moduleManager
        val module = moduleManager.getModule(moduleName, true)

        if (module == null) {
            ChatUtils.sendErrorMessage("Module '${moduleName}' doesn't exist")
            return
        }

        val glfwKey = try {
            Utils.charToGlfwKey(key)
        } catch (e: Exception) {
            ChatUtils.sendErrorMessage("$e")
            return
        }

        if (module.keybinds.contains(glfwKey)) {
            ChatUtils.sendSuccessMessage("Unbound ${module.name} from key ${key.uppercase()}")
            module.keybinds.remove(glfwKey)
        } else {
            ChatUtils.sendSuccessMessage("Bound ${module.name} to key ${key.uppercase()}")
            module.keybinds.add(glfwKey)
        }
    }
}