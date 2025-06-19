package net.horizonsend.ion.server.features.gui.custom.starship

import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.common.utils.text.plainText
import net.horizonsend.ion.common.utils.text.restrictedMiniMessageSerializer
import net.horizonsend.ion.common.utils.text.toComponent
import net.horizonsend.ion.server.features.starship.DeactivatedPlayerStarships
import net.horizonsend.ion.server.features.starship.PilotedStarships
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.text.itemName
import net.horizonsend.ion.server.miscellaneous.utils.updateDisplayName
import net.horizonsend.ion.server.miscellaneous.utils.updateLore
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
	private var newName = ""
		set(value) {
			field = value
				.replace("\uE032", "")
				.replace("\uE033", "")
		}

	override fun getItemProvider(): ItemProvider = ItemProvider {
		ItemStack(Material.NAME_TAG)
			.updateDisplayName(text("Change Ship Name", WHITE).itemName)
			.updateLore(listOf(ofChildren(text("Current Name: ", GRAY), PilotedStarships.getDisplayName(main.data)).itemName))
	}

	override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
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
		val data = main.data
		val name = data.name

		ItemStack(PAPER)
			.updateDisplayName((name?.toComponent() ?: empty()).itemName)
			.updateLore(listOf(ofChildren(text("Formatted: ", GRAY), restrictedMiniMessageSerializer.deserialize(name ?: "")).itemName))
	}

	class RenameConfirmationButton(val parent: RenameButton) : AbstractItem() {
		private val provider = ItemProvider {
			val serialized: Component = runCatching {
				restrictedMiniMessageSerializer.deserialize(parent.newName)
			}.getOrElse {
				text("Error: ${it.message}", RED).itemName
			}

			ItemStack(EMERALD_BLOCK)
				.updateDisplayName(text("Confirm New Name:", GREEN).itemName)
				.updateLore(listOf(serialized.itemName))
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
			val serialized = restrictedMiniMessageSerializer.deserializeOrNull(newName) ?: return false

			val length = serialized.plainText().length
			if (length > 24 || length < 3) {
				player.userError("Ship names must be less between 3 and 24 characters long!")
				return false
			}

			if ((newName.contains("<rainbow>") || checkRecursively(serialized) { it.color() != null }) && !player.hasPermission("ion.starship.color")) {
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

			if (checkRecursively(serialized) { it.decorations().any { decoration -> decoration.value == TextDecoration.State.TRUE} } && !player.hasPermission("ion.starship.italic")) {
				player.userError(
					"\\<italic>, \\<bold>, \\<strikethrough> and \\<underlined> tags can only be used by $10+ patrons!\n" +
					"Donate at https://www.patreon.com/horizonsendmc/ to receive this perk."
				)

				return false
			}

			if (checkRecursively(serialized) { it.font() != null } && !player.hasPermission("ion.starship.font")) {
				player.userError(
					"\\<font> tags can only be used by $15+ patrons! Donate at\n" +
					"Donate at https://www.patreon.com/horizonsendmc/ to receive this perk."
				)

				return false
			}

			return true
		}

		private fun checkRecursively(component: Component, predicate: (Component) -> Boolean): Boolean {
			return predicate(component) || component.children().any { checkRecursively(it, predicate) }
		}
	}
}
