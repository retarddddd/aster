package cx.kloinn.aster.command.impl

import cx.kloinn.aster.client.AsterClient
import cx.kloinn.aster.command.Command
import cx.kloinn.aster.utils.ChatUtils

class ToggleModule : Command() {
    override val names: List<String> = listOf("t", "toggle")
    override val description: String = "Toggles modules"
    override val helpMessage: String = "<module name>"

    override fun runCommand(args: List<String>) {
        if (args.size != 1) {
            ChatUtils.sendErrorMessage("Usage: .toggle ${this.helpMessage}")
            return
        }

        val moduleManager = AsterClient.SINGLETON.moduleManager

        val moduleName = args[0]
        val module = moduleManager.getModule(moduleName, true)

        if (module == null) {
            ChatUtils.sendErrorMessage("Module '${moduleName}' doesn't exist")
            return
        }

        moduleManager.toggleModule(module.name)

        val status = if (module.enabled) {
            "Disabled"
        } else {
            "Enabled"
        }
        ChatUtils.sendSuccessMessage("$status ${module.name}")
    }
}