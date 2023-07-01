package net.starlegacy.command.misc

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.starlegacy.command.SLCommand
import net.starlegacy.util.Vec3i
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.inventory.EquipmentSlot

@CommandAlias("multiblock")
@CommandPermission("ion.multiblock")
class MultiblockCommand : SLCommand() {

	@Subcommand("place")
	@CommandCompletion("@multiblocks")
	@Suppress("unused")
	fun onPlace(sender: Player, multiblock: Multiblock) {
		val shape = multiblock.shape.getRequirementMap(sender.facing)

		val origin = Vec3i(sender.location)

		for ((offset, requirement) in shape) {
			val (x, y, z) = offset + origin

			val blockData = requirement.first

			val existingBlock = sender.world.getBlockAt(x, y, z)

			val event = BlockPlaceEvent(
				existingBlock,
				existingBlock.state,
				existingBlock,
				sender.activeItem,
				sender,
				true,
				EquipmentSlot.HAND
			).callEvent()

			if (!event) return

			sender.world.setBlockData(x, y, z, blockData)
		}
	}
}
