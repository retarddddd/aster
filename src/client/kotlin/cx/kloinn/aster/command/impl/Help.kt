package cx.kloinn.aster.command.impl

import cx.kloinn.aster.client.AsterClient
import cx.kloinn.aster.command.Command
import cx.kloinn.aster.utils.ChatUtils

class Help : Command() {
    override val names: List<String> = listOf("help")
    override val description: String = "Shows a list of available commands"
    override val helpMessage: String? = null

    override fun runCommand(args: List<String>) {
        ChatUtils.sendMessage("Available commands:")

        val commandManager = AsterClient.SINGLETON.commandManager

        for (cmd in commandManager.commands) {
            for (name in cmd.names) {
                val helpMessage = if (cmd.helpMessage != null) {
                    " ${cmd.helpMessage}"
                } else {
                    ""
                }

                ChatUtils.sendMessage(" .$name$helpMessage - ${cmd.description}")
            }
        }
    }
}