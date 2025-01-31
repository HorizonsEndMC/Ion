package net.horizonsend.ion.server.command.qol

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.command.admin.debug
import net.horizonsend.ion.server.features.multiblock.MultiblockEntities
import net.horizonsend.ion.server.features.multiblock.entity.type.power.PoweredMultiblockEntity
import net.horizonsend.ion.server.miscellaneous.utils.getSelection
import org.bukkit.block.data.type.WallSign
import org.bukkit.entity.Player

@CommandAlias("setpower")
@CommandPermission("ion.setpower")
object SetPowerCommand : SLCommand() {
	@Default
	@Suppress("unused")
	@CommandCompletion("0|1000|500000|2147483647 true|false")
	fun onSetPower(sender: Player, amount: Int) {
		val maxSelectionVolume = 200000
		val selection = runCatching { sender.getSelection() }.getOrNull() ?: fail { "You must make a selection!" }

		if (selection.volume > maxSelectionVolume) {
			sender.userError("Selection too large! The maximum volume is $maxSelectionVolume.")
			return
		}

		if (sender.world.name != selection.world?.name) return

		var hits = 0

		for (blockPosition in selection) {
			val x = blockPosition.x()
			val y = blockPosition.y()
			val z = blockPosition.z()

			sender.debug("checking block at $x $y $z")

			val data = sender.world.getBlockData(x, y, z)
			if (data is WallSign) {
				val entity = MultiblockEntities.getMultiblockEntity(x, y, z, sender.world, data)

				if (entity is PoweredMultiblockEntity) {
					entity.powerStorage.setPower(amount)
					hits++
				}
			}

			val entity = MultiblockEntities.getMultiblockEntity(sender.world, x, y ,z)
			if (entity !is PoweredMultiblockEntity) continue

			entity.powerStorage.setPower(amount)
			hits++

			sender.debug("power sent")
		}

		sender.success("Set power to $amount in $hits multiblocks.")
	}
}
