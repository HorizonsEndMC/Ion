package net.horizonsend.ion.server.command.admin

import co.aikar.commands.InvalidCommandArgument
import co.aikar.commands.PaperCommandManager
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.customItem
import net.horizonsend.ion.server.features.custom.items.component.CustomComponentTypes.Companion.MOD_MANAGER
import net.horizonsend.ion.server.features.custom.items.component.CustomComponentTypes.Companion.POWER_STORAGE
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.ItemModRegistry
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.ItemModification
import net.horizonsend.ion.server.features.custom.items.util.serialization.CustomItemSerialization
import net.horizonsend.ion.server.features.gui.GuiItems
import net.horizonsend.ion.server.miscellaneous.utils.text.itemName
import net.horizonsend.ion.server.features.multiblock.Multiblock
import org.bukkit.command.CommandSender
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.gui.structure.Markers
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.window.Window

@CommandAlias("itemdebug")
@CommandPermission("ion.debug.command.item")
object ItemDebugCommand : SLCommand() {
	override fun onEnable(manager: PaperCommandManager) {
		manager.commandContexts.registerContext(ItemModification::class.java) {
			val name = it.popFirstArg()
			return@registerContext ItemModRegistry[name] ?: throw InvalidCommandArgument("$name not found!")
		}

		manager.commandCompletions.registerCompletion("tool_mods") {
			return@registerCompletion ItemModRegistry.mods.keys
		}

		manager.commandCompletions.setDefaultCompletion("tool_mods", ItemModification::class.java)

		manager.commandCompletions.registerAsyncCompletion("newCustomItem") { context ->
			val results = CustomItemSerialization.getCompletions(context.input)
			results
		}
	}

	@Subcommand("getmods")
	fun getMods(sender: Player) {
		val item = sender.inventory.itemInMainHand
		val customItem = item.customItem ?: fail { "Not a valid custom item!" }
		failIf(!customItem.hasComponent(MOD_MANAGER)) { "${item.customItem?.identifier} is not moddable" }
		val modManger = customItem.getComponent(MOD_MANAGER)

		sender.information("MODS: " + modManger.getMods(item).joinToString { it.identifier })
	}

	@Subcommand("addmod")
	fun addMod(sender: Player, mod: ItemModification) {
		val item = sender.inventory.itemInMainHand
		val customItem = item.customItem ?: fail { "Not a valid custom item!" }
		failIf(!customItem.hasComponent(MOD_MANAGER)) { "${item.customItem?.identifier} is not moddable" }
		val modManger = customItem.getComponent(MOD_MANAGER)

		modManger.addMod(item, customItem, mod)

		sender.information("Added ${mod.identifier}")
	}

	@Subcommand("removemod")
	fun removeMod(sender: Player, mod: ItemModification) {
		val item = sender.inventory.itemInMainHand
		val customItem = item.customItem ?: fail { "Not a valid custom item!" }
		failIf(!customItem.hasComponent(MOD_MANAGER)) { "${item.customItem?.identifier} is not moddable" }
		val modManger = customItem.getComponent(MOD_MANAGER)

		modManger.removeMod(item, customItem, mod)

		sender.information("Removed ${mod.identifier}")
	}

	@Subcommand("set power")
	fun onSetPower(sender: Player, amount: Int) {
		val item = sender.inventory.itemInMainHand
		val customItem = item.customItem ?: fail { "Not a valid custom item!" }
		failIf(!customItem.hasComponent(POWER_STORAGE)) { "${item.customItem?.identifier} is not powered" }
		val custom = customItem.getComponent(POWER_STORAGE)

		custom.setPower(customItem, item, amount)

		sender.information("Removed power to $amount")
	}

	@Subcommand("give prepackaged")
	@CommandCompletion("@multiblocks")
	fun onGivePrepackaged(sender: CommandSender, prePackagedType: Multiblock, recipient: Player?) {
		val destination: Player = recipient ?: (sender as? Player ?: fail { "You must specify a player!" })

		destination.inventory.addItem(PackagedMultiblockItem.constructFor(prePackagedType))
		sender.information("Added to inventory")
	}

	@Subcommand("test all")
	fun onTestAll(sender: Player) {
		val allItems = CustomItemRegistry.ALL.map { item -> object : GuiItems.AbstractButtonItem(item.displayName.itemName, item.constructItemStack()) {
			override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
				player.inventory.addItem(item.constructItemStack())
			}
		} }
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
			.setTitle("Custom Items Debug")
			.build(sender)
			.open()
	}

	@Subcommand("test serialization")
	fun serializationTest(sender: Player) {
		val item = sender.inventory.itemInMainHand
		val customItem = item.customItem ?: fail { "Not a valid custom item!" }

		sender.information(customItem.serialize(item))
	}

	@Subcommand("test deserialization")
	@CommandCompletion("@newCustomItem @nothing")
	fun deserializationTest(sender: Player, value: String) {
		sender.information(CustomItemSerialization.getCompletions(value)?.joinToString { it } ?: "null")

		val data = "[${value.substringAfter('[')}"
		val customItem = CustomItemRegistry.getByIdentifier(value.substringBefore('[')) ?: fail { "Not valid custom item: ${value.substringBefore('[')}" }

		sender.inventory.addItem(customItem.deserialize(data))
	}
}
