package net.horizonsend.ion.server.command.qol

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Subcommand
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
object RemovePowerCommand : SLCommand() {
	@Default
	@Subcommand("amount")
	@Suppress("unused")
	fun onSetPower(sender: Player, amount: Int){
		val selection = sender.getSelection() ?: return
		if(selection.volume > 200000) return

		if(sender.world != selection.world) {
			sender.userError("Selection in different world than player - command canceled.")
			return
		}

		var count = 0
		Tasks.async {
			for (blockPosition in selection) {
				val x = blockPosition.x
				val y = blockPosition.y
				val z = blockPosition.z

				val block = sender.world.getBlockAt(x, y, z)

				if (!block.type.isWallSign) continue

				val sign = getStateSafe(block.world, x, y, z) as? org.bukkit.block.Sign ?: continue

				setPower(sign, amount, true)
			}
		}
		sender.success("Set multiblock power to $amount.")
	}
}
