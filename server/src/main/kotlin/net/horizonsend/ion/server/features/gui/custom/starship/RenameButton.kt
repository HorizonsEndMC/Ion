package net.horizonsend.ion.server.features.gui.custom.starship

import net.horizonsend.ion.common.database.schema.starships.PlayerStarshipData
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.common.utils.text.miniMessage
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.common.utils.text.plainText
import net.horizonsend.ion.common.utils.text.toComponent
import net.horizonsend.ion.server.features.starship.DeactivatedPlayerStarships
import net.horizonsend.ion.server.features.starship.PilotedStarships
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.setDisplayNameAndGet
import net.horizonsend.ion.server.miscellaneous.utils.setLoreAndGet
import net.horizonsend.ion.server.miscellaneous.utils.text.itemName
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.empty
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.GRAY
import net.kyori.adventure.text.format.NamedTextColor.GREEN
import net.kyori.adventure.text.format.NamedTextColor.RED
import net.kyori.adventure.text.format.NamedTextColor.WHITE
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.util.HSVLike
import org.bukkit.Material
import org.bukkit.Material.EMERALD_BLOCK
import org.bukkit.Material.PAPER
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.item.impl.AbstractItem
import xyz.xenondevs.invui.item.impl.SimpleItem
import xyz.xenondevs.invui.window.AnvilWindow

class RenameButton(val main: StarshipComputerMenu) : AbstractItem() {
	var newName = ""

	override fun getItemProvider(): ItemProvider = ItemProvider {
		ItemStack(Material.NAME_TAG)
			.setDisplayNameAndGet(text("Change Ship Name", WHITE).itemName)
			.setLoreAndGet(listOf(ofChildren(text("Current Name: ", GRAY), PilotedStarships.getDisplayName(main.data)).itemName))
	}

	override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
		val data = main.data as? PlayerStarshipData
		if (data == null) {
			player.userError("You can only add pilots to player ships!")
			return
		}

		openRenameMenu(player)
	}

	private val confirmationButton = RenameConfirmationButton(this)

	private fun openRenameMenu(player: Player) {
		val gui = Gui.normal()
			.setStructure("n v x")
			.addIngredient('n', namePromptPaper)
			.addIngredient('v', main.mainMenuButton)
			.addIngredient('x', confirmationButton)

		AnvilWindow.single()
			.setViewer(player)
			.setTitle(AdventureComponentWrapper(text("Enter New Ship Name")))
			.setGui(gui)
			.addRenameHandler { string ->
				newName = string
				confirmationButton.notifyWindows()
			}
			.build()
			.open()
	}

	private val namePromptPaper = SimpleItem {
		val data = main.data as PlayerStarshipData
		val name = data.name

		// The whole empty append setup is to provide a formatted base that isn't italic purple
		ItemStack(PAPER)
			.setDisplayNameAndGet((name?.toComponent() ?: empty()).itemName)
			.setLoreAndGet(listOf(ofChildren(text("Formatted: ", GRAY), (name ?: "").miniMessage()).itemName))
	}

	class RenameConfirmationButton(val parent: RenameButton) : AbstractItem() {
		private val provider = ItemProvider {
			val serialized: Component = runCatching {
				parent.newName.miniMessage()
			}.getOrElse {
				text("Error: ${it.message}", RED).itemName
			}

			ItemStack(EMERALD_BLOCK)
				.setDisplayNameAndGet(text("Confirm New Name:", GREEN).itemName)
				.setLoreAndGet(listOf(serialized.itemName))
		}

		override fun getItemProvider(): ItemProvider = provider

		override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
			Tasks.async {
				val result = checkName(player, parent.newName)

				if (!result) return@async

				DeactivatedPlayerStarships.updateName(parent.main.data, parent.newName)

				Tasks.sync {
					player.closeInventory()
					parent.main.open()
				}
			}
		}

		private fun checkName(player: Player, newName: String): Boolean {
			val serialized = runCatching {
				newName.miniMessage()
			}.getOrNull() ?: return false

			if (serialized.plainText().length >= 24) {
				player.userError("Ship names must be less than 24 characters!")
				return false
			}

			if (serialized.clickEvent() != null ||
				newName.contains("<newline>") ||
				newName.contains("<reset>") ||
				serialized.hoverEvent() != null ||
				serialized.insertion() != null ||
				serialized.hasDecoration(TextDecoration.OBFUSCATED)
			) {
				player.userError("ERROR: Disallowed tags!")
				return false
			}

			if (
				(newName.contains("<rainbow>") ||serialized.color() != null) &&
				!player.hasPermission("ion.starship.color")
			) {
				player.userError(
					"<COLOR> tags can only be used by $5+ patrons or Discord boosters! Donate at\n" +
						"Donate at https://www.patreon.com/horizonsendmc/ to receive this perk."
				)
				return false
			}

			if ((serialized.color() as? HSVLike) != null && serialized.color()!!.asHSV().v() < 0.25) {
				player.userError("Ship names can't be too dark to read!")

				return false
			}

			if (
				serialized.decorations().any { it.value == TextDecoration.State.TRUE } &&
				!player.hasPermission("ion.starship.italic")
			) {
				player.userError(
					"\\<italic>, \\<bold>, \\<strikethrough> and \\<underlined> tags can only be used by $10+ patrons!\n" +
						"Donate at https://www.patreon.com/horizonsendmc/ to receive this perk."
				)
				return false
			}

			if (serialized.font() != null && !player.hasPermission("ion.starship.font")) {
				player.userError(
					"\\<font> tags can only be used by $15+ patrons! Donate at\n" +
						"Donate at https://www.patreon.com/horizonsendmc/ to receive this perk."
				)
				return false
			}

			return true
		}
	}
}
