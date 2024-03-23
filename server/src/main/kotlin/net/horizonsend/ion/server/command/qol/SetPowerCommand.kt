package net.horizonsend.ion.server.command.qol

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Optional
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.machine.PowerMachines.setPower
import net.horizonsend.ion.server.miscellaneous.utils.getSelection
import net.horizonsend.ion.server.miscellaneous.utils.isWallSign
import org.bukkit.entity.Player
import net.horizonsend.ion.server.command.admin.debug

@CommandAlias("setpower")
@CommandPermission("ion.setpower")
object SetPowerCommand : SLCommand() {
	@Default
	@Suppress("unused")

	fun onSetPower(sender: Player, amount: Int, @Optional restricted: Boolean?){
		val maxSelectionVolume = 200000
		val selection = sender.getSelection() ?: return
		val limited = restricted ?: true

		if(selection.volume > maxSelectionVolume) {
			sender.userError("Selection too large! The maximum volume is $maxSelectionVolume.")
			return
		}

		if(sender.world.name != selection.world?.name) {
			sender.userError("Selection in world ${selection.world?.name}, player is in world ${sender.world.name} - command canceled.")
			return
		}

		for (blockPosition in selection) {
			val x = blockPosition.x
			val y = blockPosition.y
			val z = blockPosition.z

			val block = sender.world.getBlockAt(x, y, z)
			sender.debug("checking block at $x $y $z")
			if (!block.type.isWallSign) continue
			val sign = block.state as? org.bukkit.block.Sign ?: continue
			sender.debug("sign found at $x $y $z")

			setPower(sign, amount, true)
			sender.debug("power sent")
		}
		sender.success("Set multiblock power to $amount.")
	}
}
