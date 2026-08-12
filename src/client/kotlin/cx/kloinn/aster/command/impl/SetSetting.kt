package cx.kloinn.aster.command.impl

import cx.kloinn.aster.client.AsterClient
import cx.kloinn.aster.command.Command
import cx.kloinn.aster.module.setting.impl.BoolSetting
import cx.kloinn.aster.module.setting.impl.EnumSetting
import cx.kloinn.aster.module.setting.impl.FloatSetting
import cx.kloinn.aster.utils.ChatUtils
import cx.kloinn.aster.utils.Utils

class SetSetting : Command() {
    override val names: List<String> = listOf("set")
    override val description: String = "Changes settings of modules"
    override val helpMessage: String = "<module name> <setting name> <value>"

    override fun runCommand(args: List<String>) {
        if (args.size != 3) {
            ChatUtils.sendErrorMessage("Usage: .set ${this.helpMessage}")
            return
        }

        val moduleName = args[0]
        val settingName = args[1]
        val value = args[2]

        val moduleManager = AsterClient.SINGLETON.moduleManager
        val module = moduleManager.getModule(moduleName, true)

        if (module == null) {
            ChatUtils.sendErrorMessage("Module '${moduleName}' doesn't exist")
            return
        }

        val setting = module.getSettings().find { it.settingName.lowercase() == settingName.lowercase() }

        if (setting == null) {
            ChatUtils.sendErrorMessage("Setting '${moduleName}' doesn't exist")
            return
        }

        if (setting is FloatSetting) {
            val floatValue = value
                .replace("f", "")
                .replace(",", ".")
                .toFloatOrNull()

            if (floatValue == null) {
                ChatUtils.sendErrorMessage("$value is not a valid float")
                return
            }

            setting.value = floatValue
        }

        if (setting is BoolSetting) {
            val boolValue = when (value) {
                "true", "t", "1", "y", "yes" -> true
                "false", "f", "0", "n", "no" -> false
                else -> {
                    ChatUtils.sendErrorMessage("$value is not a valid bool")
                    return
                }
            }

            setting.value = boolValue
        }

        if (setting is EnumSetting<*>) {
            val enumValue = setting.values.find { it.name.lowercase() == value.lowercase() }

            if (enumValue == null) {
                ChatUtils.sendErrorMessage("$value is not a valid enum value. Valid values: ${setting.values.joinToString(", ")}")
                return
            }

            (setting as EnumSetting<Enum<*>>).selectedValue = enumValue
        }

        ChatUtils.sendSuccessMessage("Setting successfully changed")
    }
}