package cx.kloinn.aster.module.impl.other

import com.mojang.blaze3d.platform.InputConstants
import cx.kloinn.aster.module.Module
import cx.kloinn.aster.module.ModuleManager
import cx.kloinn.aster.module.setting.Setting
import net.minecraft.client.Minecraft

class AutoConfig : Module() {
    override val name = "Auto Config"
    override val keybinds: ArrayList<Int> = arrayListOf()
    override var enabled = true
    override val category = ModuleManager.Category.Movement
    override val settings: ArrayList<Setting> = arrayListOf()

    override fun onEnable() {
        Minecraft.getInstance().options.keyAdvancements.setKey(InputConstants.UNKNOWN)
        Minecraft.getInstance().options.keySocialInteractions.setKey(InputConstants.UNKNOWN)
        Minecraft.getInstance().options.keyFriends.setKey(InputConstants.UNKNOWN)
        Minecraft.getInstance().options.skipMultiplayerWarning = true

        Minecraft.getInstance().options.framerateLimit().set(260) // Setting the maximum FPS to exactly 260 makes the client disable the limit. Thanks, Mojang.
        // this.framerateLimit = new OptionInstance("options.framerateLimit", OptionInstance.noTooltip(), (caption, value) -> value == 260 ? genericValueLabel(caption, Component.translatable("options.framerateLimit.max")) : genericValueLabel(caption, Component.translatable("options.framerate", new Object[]{value})), (new OptionInstance.IntRange(1, 26)).xmap((value) -> value * 10, (value) -> value / 10, true), Codec.intRange(10, 260), 120, (value) -> Minecraft.getInstance().getFramerateLimitTracker().setFramerateLimit(value));

        Minecraft.getInstance().options.guiScale().set(2)
        Minecraft.getInstance().options.showAutosaveIndicator().set(false)

        Minecraft.getInstance().options.save()
    }
}