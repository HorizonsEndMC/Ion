package net.horizonsend.ion.server.command.qol

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.machine.PowerMachines.getPower
import net.horizonsend.ion.server.features.machine.PowerMachines.setPower
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.getSelection
import net.horizonsend.ion.server.miscellaneous.utils.getStateSafe
import net.horizonsend.ion.server.miscellaneous.utils.isWallSign
import org.bukkit.entity.Player

@CommandAlias("setpower")
@CommandPermission("ion.setpower")
object SetPowerCommand : SLCommand() {
	@Default
	@Suppress("unused")
	fun onSetPower(sender: Player, amount: Int){
		val selection = sender.getSelection() ?: return
		if(selection.volume > 200000) return

		if(sender.world.name != selection.world?.name) {
			sender.userError("Selection in world ${selection.world?.name}, player is in world ${sender.world.name} - command canceled.")
			return
		}
		Tasks.async {
			for (blockPosition in selection) {
				val x = blockPosition.x
				val y = blockPosition.y
				val z = blockPosition.z

				val block = sender.world.getBlockAt(x, y, z)

				if (!block.type.isWallSign) continue

				val sign = getStateSafe(block.world, x, y, z) as? org.bukkit.block.Sign ?: continue
				sender.success("sign exists yippee")
				getPower(sign, false)
				setPower(sign, amount, false)
			}
		}
		sender.success("Set multiblock power to $amount.")
	}
}
