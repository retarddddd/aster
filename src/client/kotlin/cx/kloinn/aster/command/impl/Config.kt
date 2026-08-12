package cx.kloinn.aster.command.impl

import cx.kloinn.aster.client.AsterClient
import cx.kloinn.aster.command.Command
import cx.kloinn.aster.utils.ChatUtils

class Config : Command() {
    override val names: List<String> = listOf("c", "config")
    override val description: String = "Config management command"
    override val helpMessage: String = "([l]oad/[s]ave/[l]i[s]t) [config name]"

    override fun runCommand(args: List<String>) {
        if (args.size < 1) {
            ChatUtils.sendErrorMessage("Usage: .config ${this.helpMessage}")
            return
        }

        val subCommand = args[0]

        val configManager = AsterClient.SINGLETON.configManager

        when (subCommand) {
            "l", "load" -> {
                if (args.size < 2) {
                    ChatUtils.sendErrorMessage("Usage: .config load [config name]")
                    return
                }

                val configName = args[1]

                try {
                    ChatUtils.sendSuccessMessage("Loaded config: $configName")
                    configManager.loadConfig(configName)
                } catch (_: Exception) {
                    ChatUtils.sendErrorMessage("Failed to load config: $configName")
                }
            }
            "s", "save" -> {
                if (args.size < 2) {
                    ChatUtils.sendErrorMessage("Usage: .config save [config name]")
                    return
                }

                val configName = args[1]

                try {
                    ChatUtils.sendSuccessMessage("Saved config: $configName")
                    configManager.saveConfig(configName)
                } catch (_: Exception) {
                    ChatUtils.sendErrorMessage("Failed to save config: $configName")
                }
            }
            "ls", "list" -> {
                ChatUtils.sendMessage("Configs: ${configManager.listConfigs().joinToString(", ")}")
            }
            else -> {
                ChatUtils.sendErrorMessage("Invalid subcommand! Usage .config ${this.helpMessage}")
            }
        }
    }
}