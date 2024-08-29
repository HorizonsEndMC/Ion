package net.horizonsend.ion.server.command.misc

import co.aikar.commands.BukkitCommandExecutionContext
import co.aikar.commands.InvalidCommandArgument
import co.aikar.commands.PaperCommandManager
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.common.utils.text.bracketed
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities.displayBlock
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities.sendEntityPacket
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.newer.MultiblockRegistration
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.getFacing
import net.horizonsend.ion.server.miscellaneous.utils.getRelativeIfLoaded
import net.horizonsend.ion.server.miscellaneous.utils.minecraft
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.block.Block
import org.bukkit.block.Sign
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.util.Vector

@CommandAlias("multiblock")
@CommandPermission("ion.multiblock")
object MultiblockCommand : SLCommand() {
	override fun onEnable(manager: PaperCommandManager) {
		manager.commandContexts.registerContext(Multiblock::class.java) { c: BukkitCommandExecutionContext ->
			val name: String = c.popFirstArg()

			MultiblockRegistration.getAllMultiblocks().firstOrNull { it.javaClass.simpleName == name }
				?: throw InvalidCommandArgument("Multiblock $name not found!")
		}

		registerStaticCompletion(
			manager,
			"multiblocks",
			MultiblockRegistration.getAllMultiblocks().joinToString("|") { it.javaClass.simpleName })
	}

	/**
	 * Prompt the player to use the multiblock command
	 **/
	fun setupCommand(player: Player, sign: Sign, lastMatch: Multiblock) {
		val multiblockType = lastMatch.name

		val possibleTiers = MultiblockRegistration.getAllMultiblocks().filter { it.name == multiblockType }

		if (possibleTiers.size == 1) {
			onCheck(player, possibleTiers.first(), sign.x, sign.y, sign.z)
		}

		val message = Component.text()
			.append(Component.text("Which type of $multiblockType are you trying to build? (Click one)"))
			.append(Component.newline())

		for (tier in possibleTiers) {
			val tierName = tier.javaClass.simpleName

			val command = "/multiblock check $tierName ${sign.x} ${sign.y} ${sign.z}"

			val tierText = bracketed(Component.text(tierName, NamedTextColor.DARK_GREEN, TextDecoration.BOLD))
				.clickEvent(ClickEvent.runCommand(command))
				.hoverEvent(Component.text(command).asHoverEvent())

			message.append(tierText)
			if (possibleTiers.indexOf(tier) != possibleTiers.size - 1) message.append(Component.text(", "))
		}

		player.sendMessage(message.build())
	}

	@Subcommand("check")
	@CommandCompletion("@multiblocks")
	@CommandPermission("ion.multiblock.check")
    fun onCheck(sender: Player, lastMatch: Multiblock, x: Int, y: Int, z: Int) {
		val sign = sender.world.getBlockAt(x, y, z).state as? Sign ?: return sender.userError("Block at $x $y $z isn't a sign!")

		val face = sign.getFacing().oppositeFace

		lastMatch.shape.getRequirementMap(face).forEach { (coords, requirement) ->
			val expected =  requirement.example

			val requirementX = coords.x
			val requirementY = coords.y
			val requirementZ = coords.z

			val oldRelative: Block = sign.block
				.getRelativeIfLoaded(requirementX, requirementY, requirementZ) ?: return

			val relative: Block = if (!lastMatch.shape.signCentered) oldRelative.getRelative(face) else oldRelative

			val requirementMet = requirement(relative, face, false)

			if (!requirementMet) {
				val (xx, yy, zz) = Vec3i(relative.location)

				sendEntityPacket(sender, displayBlock(sender.world.minecraft, expected, Vector(xx, yy, zz), 0.5f, true), 10 * 20L)
				sender.userError(
					"Block at ${Vec3i(relative.location)} doesn't match! Expected ${requirement.alias}, found ${relative.type}."
				)
			}
		}
	}

	@Subcommand("place")
	@CommandCompletion("@multiblocks")
	@CommandPermission("ion.multiblock.place")
    fun onPlace(sender: Player, multiblock: Multiblock) {
		val shape = multiblock.shape.getRequirementMap(sender.facing)

		val origin = Vec3i(sender.location)

		for ((offset, requirement) in shape) {
			val absolute = origin + offset

			val (x, y, z) = absolute

			val blockData = requirement.example

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
