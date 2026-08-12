package cx.kloinn.aster.command

import cx.kloinn.aster.command.impl.BindModule
import cx.kloinn.aster.command.impl.Config
import cx.kloinn.aster.command.impl.Help
import cx.kloinn.aster.command.impl.SetSetting
import cx.kloinn.aster.command.impl.ToggleModule
import cx.kloinn.aster.utils.ChatUtils
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ServerboundChatPacket

class CommandManager {
    val commands: List<Command> = listOf(
        ToggleModule(),
        BindModule(),
        SetSetting(),
        Help(),
        Config()
    )

    fun onPacket(packet: Packet<*>): Boolean {
        if (packet is ServerboundChatPacket) {
            val message = packet.message

            if (!message.startsWith(".")) return false

            var found = false

            for (command in commands) {
                for (name in command.names) {
                    val label = ".${name}"

                    if (message.startsWith(label)) {
                        val args = message
                            .split(" ")
                            .drop(1)

                        val parsedArgs = parseArgs(args)

                        try {
                            command.runCommand(parsedArgs.toList())
                        } catch (e: Exception) {
                            ChatUtils.sendErrorMessage("Error executing command. See console.")
                            e.printStackTrace()
                        }

                        found = true
                        break
                    }
                }
            }

            if (!found) {
                ChatUtils.sendErrorMessage("Unknown command. Type '.help' for a list of available commands.")
            }

            return true
        }

        return false
    }

    private fun parseArgs(args: List<String>): Array<String> {
        var newArgs: Array<String> = emptyArray()

        var quoteArg: Array<String> = emptyArray()
        var inQuotes = false

        // This parser fucking sucks, but I just want to be finally done with this piece of shit client
        for (arg in args) {
            if (arg.startsWith("\"")) {
                inQuotes = true
            }

            if (inQuotes) {
                quoteArg += arg
            } else {
                newArgs += arg
            }

            if (arg.endsWith("\"")) {
                if (quoteArg.size > 0) {
                    newArgs += quoteArg.joinToString(" ").replace("\"", "")
                    quoteArg = emptyArray()
                }

                inQuotes = false
            }
        }

        return newArgs
    }
}