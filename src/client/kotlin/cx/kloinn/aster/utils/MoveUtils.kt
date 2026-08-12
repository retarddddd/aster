package cx.kloinn.aster.utils

import cx.kloinn.aster.client.isUsingKeyboardMoveKeys
import cx.kloinn.aster.mixin.EntityFlagAccessor
import net.minecraft.client.Minecraft
import net.minecraft.client.player.ClientInput
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.item.ItemStack

object MoveUtils {
    // there is a math formula to calculate this dynamically instead of using an array list
    // however, I am way too lazy to use it.
    val JUMP_VALS = arrayListOf(
        0.41999998688697815,
        0.33319999363422426,
        0.24813599859094637,
        0.164773281826065,
        0.08307781780646906,
        0.003016261509046103,
        -0.07544406518948676,
        -0.15233518685055714,
        -0.22768848754498805,
        -0.3015347236627832,
        -0.373904036466719,
        -0.12129684053919476
    )

    private var input: ClientInput? = null

    fun jump() {
        val client = Minecraft.getInstance()
        val player = client.player ?: return

        if (!player.isUsingKeyboardMoveKeys) return

        if (player.onGround()) {
            player.input.makeJump()

            if (input !== player.input && input !== null) {
                player.connection.send(ServerboundPlayerInputPacket(this.input!!.keyPresses))
            }

            input = player.input

            player.jumpFromGround()
        }
    }

    fun setSprinting(omni: Boolean) {
        val player = Minecraft.getInstance().player ?: return

        if (!omni && (player.zza < 0 /* moving backwards */ || player.xxa > 0 /* strafe (moving left/right) */)) {
            return
        }

        player.isSprinting = true
    }

    fun resetElytraState() {
        val player = Minecraft.getInstance().player

        (player as EntityFlagAccessor).invokeSetSharedFlag(7, false)
        player.setItemSlot(
            EquipmentSlot.CHEST,
            ItemStack.EMPTY
        )
    }
}