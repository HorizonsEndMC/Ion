package net.starlegacy.command.misc

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.miscellaneous.Vec3i
import net.starlegacy.command.SLCommand
import net.starlegacy.feature.multiblock.Multiblock
import net.starlegacy.util.getFacing
import net.starlegacy.util.getRelativeIfLoaded
import org.bukkit.block.Block
import org.bukkit.block.Sign
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.inventory.EquipmentSlot

@CommandAlias("multiblock")
@CommandPermission("ion.multiblock")
class MultiblockCommand : SLCommand() {
	@Subcommand("check")
	@CommandCompletion("@multiblocks")
	@CommandPermission("ion.multiblock.check")
	@Suppress("unused")
	fun onCheck(sender: Player, lastMatch: Multiblock, x: Int, y: Int, z: Int) {
		val sign = sender.world.getBlockAt(x, y, z).state as? Sign ?: return sender.userError("Block at $x $y $z isn't a sign!")

		val face = sign.getFacing().oppositeFace

		lastMatch.shape.getRequirementMap(face).forEach { (coords, requirementMap) ->
			val (expected, requirement) = requirementMap

			val requirementX = coords.x
			val requirementY = coords.y
			val requirementZ = coords.z

			val oldRelative: Block = sign.block
				.getRelativeIfLoaded(requirementX, requirementY, requirementZ) ?: return

			val relative: Block = if (!lastMatch.shape.signCentered) oldRelative.getRelative(face) else oldRelative

			val requirementMet = requirement(relative, face)

			if (!requirementMet) {
				sender.userError(
					"Block at ${Vec3i(relative.location)} doesn't match! Expected ${expected.material}, found ${relative.type}."
				)
			}
		}
	}

	@Subcommand("place")
	@CommandCompletion("@multiblocks")
	@CommandPermission("ion.multiblock.place")
	@Suppress("unused")
	fun onPlace(sender: Player, multiblock: Multiblock) {
		val shape = multiblock.shape.getRequirementMap(sender.facing)

		val origin = Vec3i(sender.location)

		for ((offset, requirement) in shape) {
			val absolute = origin + offset

			val (x, y, z) = absolute

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

			if (!event) return sender.userError("You can't build here!")

			existingBlock.blockData = blockData
		}

		sender.success("Placed ${multiblock.javaClass.simpleName}")
	}
}
