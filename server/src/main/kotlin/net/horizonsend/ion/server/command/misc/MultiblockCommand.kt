package net.horizonsend.ion.server.command.misc

import co.aikar.commands.BukkitCommandExecutionContext
import co.aikar.commands.InvalidCommandArgument
import co.aikar.commands.PaperCommandManager
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Optional
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.common.utils.text.bracketed
import net.horizonsend.ion.common.utils.text.formatPaginatedMenu
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities.displayBlock
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities.highlightBlock
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities.sendEntityPacket
import net.horizonsend.ion.server.features.custom.items.misc.MultiblockToken
import net.horizonsend.ion.server.features.gui.GuiItems
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.MultiblockRegistration
import net.horizonsend.ion.server.features.multiblock.type.DisplayNameMultilblock.Companion.getDisplayName
import net.horizonsend.ion.server.features.multiblock.type.DisplayNameMultilblock.Companion.getIcon
import net.horizonsend.ion.server.features.world.chunk.IonChunk.Companion.ion
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toVec3i
import net.horizonsend.ion.server.miscellaneous.utils.getFacing
import net.horizonsend.ion.server.miscellaneous.utils.getRelativeIfLoaded
import net.horizonsend.ion.server.miscellaneous.utils.minecraft
import net.horizonsend.ion.server.miscellaneous.utils.text.itemName
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.Sign
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.gui.structure.Markers
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.window.Window

@CommandAlias("multiblock")
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
			return
		}

		val message = text()
			.append(text("Which type of $multiblockType are you trying to build? (Click one)"))
			.append(Component.newline())

		for (tier in possibleTiers) {
			val tierName = tier.javaClass.simpleName

			val command = "/multiblock check $tierName ${sign.x} ${sign.y} ${sign.z}"

			val tierText = bracketed(text(tierName, NamedTextColor.DARK_GREEN, TextDecoration.BOLD))
				.clickEvent(ClickEvent.runCommand(command))
				.hoverEvent(text(command).asHoverEvent())

			message.append(tierText)
			if (possibleTiers.indexOf(tier) != possibleTiers.size - 1) message.append(Component.text(", "))
		}

		player.sendMessage(message.build())
	}

	@Subcommand("check")
	@CommandCompletion("@multiblocks")
	fun onCheck(sender: Player, lastMatch: Multiblock, x: Int, y: Int, z: Int) {
		val sign = sender.world.getBlockAt(x, y, z).state as? Sign ?: return sender.userError("Block at $x $y $z isn't a sign!")

		val face = sign.getFacing().oppositeFace

		lastMatch.shape.getRequirementMap(face).forEach { (coords, requirement) ->
			val requirementX = coords.x
			val requirementY = coords.y
			val requirementZ = coords.z

			val oldRelative: Block = sign.block
				.getRelativeIfLoaded(requirementX, requirementY, requirementZ) ?: return

			val relative: Block = if (!lastMatch.shape.signCentered) oldRelative.getRelative(face) else oldRelative

			val requirementMet = requirement(relative, face, false)

			if (!requirementMet) {
				val (xx, yy, zz) = Vec3i(relative.location)

				sendEntityPacket(sender, displayBlock(sender.world.minecraft, requirement.getExample(face), Vector(xx, yy, zz), 0.5f, Color.WHITE), 10 * 20L)
				sender.userError(
					"Block at ${Vec3i(relative.location)} doesn't match! Expected ${requirement.alias}, found ${relative.type}."
				)
			}
		}
	}

	@Subcommand("place")
	@CommandCompletion("@multiblocks")
	@CommandPermission("ion.command.multiblock.place")
    fun onPlace(sender: Player, multiblock: Multiblock) {
		val shape = multiblock.shape.getRequirementMap(sender.facing)

		val origin = Vec3i(sender.location)

		for ((offset, requirement) in shape) {
			val absolute = origin + offset

			val (x, y, z) = absolute

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

			existingBlock.blockData = requirement.getExample(sender.facing)
		}

		sender.success("Placed ${multiblock.javaClass.simpleName}")
	}

	@Subcommand("give token")
	@CommandCompletion("@multiblocks")
	@CommandPermission("ion.command.multiblock.give")
	fun onGivePrepackaged(sender: CommandSender, prePackagedType: Multiblock, recipient: Player?) {
		val destination: Player = recipient ?: (sender as? Player ?: fail { "You must specify a player!" })

		destination.inventory.addItem(MultiblockToken.constructFor(prePackagedType))
		sender.information("Added to inventory")
	}

	@Subcommand("debug dump entities chunk")
	@CommandCompletion("true|false")
	@CommandPermission("ion.command.multiblock.debug")
	fun onDumpEntitiesChunk(sender: Player, @Optional visual: Boolean?, @Optional page: Int?) {
		val manager = sender.chunk.ion().multiblockManager
		val entities = manager.getAllMultiblockEntities().toList()

		sender.sendMessage(formatPaginatedMenu(
			entities,
			"/ionchunk dumpentities ${visual == true}",
			page ?: 1,
		) { entry, _ ->
			val (key, entity) = entry
			val vec = toVec3i(key)

			text("$vec : $entity")
		})

		if (visual == true) {
			for ((_, entity) in entities) {
				sender.highlightBlock(entity.globalVec3i, 30L)
			}
		}

		sender.information("Sync Ticked: ${manager.syncTickingMultiblockEntities}")
		sender.information("Async Ticked: ${manager.asyncTickingMultiblockEntities}")
	}

	@Subcommand("debug dump entities ship")
	@CommandCompletion("true|false")
	@CommandPermission("ion.command.multiblock.debug")
	fun onDumpEntitiesShip(sender: Player, @Optional visual: Boolean?, @Optional page: Int?) {
		val ship = getStarshipPiloting(sender)
		val manager = ship.multiblockManager
		val entities = manager.getAllMultiblockEntities().toList()

		sender.sendMessage(formatPaginatedMenu(
			entities.size,
			"/ionchunk dumpentities ${visual ?: false}",
			page ?: 1,
		) { index ->
			val (key, entity) = entities[index]

			val vec = toVec3i(key)

			text("$vec : $entity")
		})

		if (visual == true) {
			for ((_, entity) in entities) {
				sender.highlightBlock(entity.globalVec3i, 30L)
			}
		}

		sender.information("Sync Ticked: ${manager.syncTickingMultiblockEntities}")
		sender.information("Async Ticked: ${manager.asyncTickingMultiblockEntities}")
	}

	@Subcommand("debug remove all entities")
	@CommandPermission("ion.command.multiblock.debug")
	fun onRemoveAll(sender: Player) {
		val ionChunk = sender.chunk.ion()
		val entities = ionChunk.multiblockManager.getAllMultiblockEntities()

		for ((key, _) in entities) {
			val (x, y, z) = toVec3i(key)

			ionChunk.multiblockManager.removeMultiblockEntity(x, y, z)
		}
	}

	@Subcommand("menu")
	@CommandPermission("ion.command.multiblock.debug")
	fun onTestAll(sender: Player) {
		val allItems = MultiblockRegistration.getAllMultiblocks().map { multiblock ->
			object : GuiItems.AbstractButtonItem(
				multiblock.getDisplayName().itemName,
				multiblock.getIcon()
			) {
				override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
					player.inventory.addItem(MultiblockToken.constructFor(multiblock))
				}
			}
		}
		val gui = PagedGui.items()
			.setStructure(
				"x x x x x x x x x",
				"x x x x x x x x x",
				"x x x x x x x x x",
				"x x x x x x x x x",
				"< # # # # # # # >",
			)
			.addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
			.addIngredient('#', ItemProvider { ItemStack(Material.BLACK_STAINED_GLASS) })
			.addIngredient('<', GuiItems.PageLeftItem())
			.addIngredient('>', GuiItems.PageRightItem())
			.setContent(allItems)
			.build()

		Window
			.single()
			.setGui(gui)
			.setTitle("All Multiblocks")
			.build(sender)
			.open()
	}

}
