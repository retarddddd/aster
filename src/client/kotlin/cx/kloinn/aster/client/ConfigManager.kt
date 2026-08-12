package cx.kloinn.aster.client

import cx.kloinn.aster.command.impl.Config
import cx.kloinn.aster.module.setting.impl.BoolSetting
import cx.kloinn.aster.module.setting.impl.EnumSetting
import cx.kloinn.aster.module.setting.impl.FloatSetting
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.float
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import net.fabricmc.loader.api.FabricLoader
import java.nio.file.Files
import java.nio.file.Path
import java.util.stream.Collectors
import kotlin.io.path.name
import kotlin.to

private val client = AsterClient.SINGLETON

class ConfigManager {
    private lateinit var clientDataPath: Path
    private lateinit var configsPath: Path

    fun saveConfig(configName: String) {
        val serializedModules: MutableList<JsonObject> = mutableListOf()

        for (module in client.moduleManager.modules) {
            val serializedSettings: MutableList<JsonObject> = mutableListOf()

            for (setting in module.getSettings()) {
                var obj: JsonObject? = null

                if (setting is BoolSetting) {
                    obj = JsonObject(
                        mapOf(
                            "name" to JsonPrimitive(setting.settingName),
                            "type" to JsonPrimitive("bool"),
                            "value" to JsonPrimitive(setting.value)
                        )
                    )
                }

                if (setting is FloatSetting) {
                    obj = JsonObject(
                        mapOf(
                            "name" to JsonPrimitive(setting.settingName),
                            "type" to JsonPrimitive("float"),
                            "value" to JsonPrimitive(setting.value)
                        )
                    )
                }

                if (setting is EnumSetting<*>) {
                    obj = JsonObject(
                        mapOf(
                            "name" to JsonPrimitive(setting.settingName),
                            "type" to JsonPrimitive("enum"),
                            "value" to JsonPrimitive(setting.selectedValue.toString())
                        )
                    )
                }

                serializedSettings.plusAssign(obj!!)
            }

            val moduleInfo = JsonObject(
                mapOf(
                    "moduleName" to JsonPrimitive(module.name),
                    "enabled" to JsonPrimitive(module.enabled),
                    "keybinds" to JsonArray(
                        module.keybinds.map { JsonPrimitive(it) }
                    ),
                    "settings" to JsonArray(serializedSettings)
                )
            )
            serializedModules.plusAssign(moduleInfo)
        }

        val serializedConfig = Json.encodeToString(serializedModules)

        Files.writeString(configsPath.resolve("$configName.json"), serializedConfig)
    }

    fun loadConfig(configName: String) {
        val client = AsterClient.SINGLETON
        val moduleManager = client.moduleManager

        val rawConfig = Files.readString(configsPath.resolve("$configName.json"))
        val modules = Json.parseToJsonElement(rawConfig).jsonArray

        for (moduleRaw in modules) {
            val moduleObj = moduleRaw.jsonObject

            val moduleName = moduleObj["moduleName"]?.jsonPrimitive?.content!!
            val enabled = moduleObj["enabled"]?.jsonPrimitive?.boolean!!
            val keybinds = moduleObj["keybinds"]?.jsonArray!!
            val settings = moduleObj["settings"]?.jsonArray!!

            val module = moduleManager.getModule(moduleName)

            if (module == null) {
                client.logger.warning("Unknown module: $moduleName, skipping")
                continue
            }

            if (enabled) {
                moduleManager.enableModule(moduleName)
            } else {
                moduleManager.disableModule(moduleName)
            }

            for (keybind in keybinds) {
                module.keybinds += keybind.jsonPrimitive.content.toInt()
            }

            for (settingRaw in settings) {
                val settingObj = settingRaw.jsonObject

                val settingName = settingObj["name"]!!.jsonPrimitive.content
                val settingValueObj = settingObj["value"]!!.jsonPrimitive
                val settingType = settingObj["type"]!!.jsonPrimitive.content

                val setting = module.getSettings().find { it.settingName == settingName }

                if (setting == null) {
                    continue
                }

                when (settingType) {
                    "bool" -> {
                        (setting as BoolSetting).value = settingValueObj.boolean
                    }
                    "float" -> {
                        (setting as FloatSetting).value = settingValueObj.float
                    }
                    "enum" -> {
                        val castedSetting = setting as EnumSetting<*>
                        val enumValueRaw = settingValueObj.jsonPrimitive.content

                        val enumValue = castedSetting.values.find { it.name.lowercase() == enumValueRaw.lowercase() }

                        if (enumValue == null) {
                            client.logger.warning("Unknown enum value: $settingValueObj, skipping")
                            continue
                        }

                        (setting as EnumSetting<Enum<*>>).selectedValue = enumValue
                    }
                    else -> {
                        client.logger.warning("Unknown setting type: $settingName, skipping")
                    }
                }
            }
        }
    }

    fun listConfigs(): List<String> {
        return Files.list(configsPath)
            .collect(Collectors.toList())
            .map { it.name.replace(".json", "") }
    }

    fun init() {
        val modFileDir = FabricLoader.getInstance().configDir

        clientDataPath = modFileDir.resolve("aster")
        Files.createDirectories(clientDataPath)

        configsPath = clientDataPath.resolve("configs")
        Files.createDirectories(configsPath)

        client.logger.info("Config directory: $configsPath")
    }
}