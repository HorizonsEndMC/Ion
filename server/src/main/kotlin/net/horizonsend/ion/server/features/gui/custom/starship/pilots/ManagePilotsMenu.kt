package net.horizonsend.ion.server.features.gui.custom.starship.pilots

import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.database.schema.misc.SLPlayerId
import net.horizonsend.ion.common.database.schema.starships.PlayerStarshipData
import net.horizonsend.ion.common.database.uuid
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.common.utils.text.ITALIC
import net.horizonsend.ion.server.features.gui.GuiItems
import net.horizonsend.ion.server.features.gui.custom.starship.StarshipComputerMenu
import net.horizonsend.ion.server.features.nations.gui.skullItem
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.updateDisplayName
import net.horizonsend.ion.server.miscellaneous.utils.updateLore
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.NamedTextColor.WHITE
import org.bukkit.Material.PLAYER_HEAD
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import org.litote.kmongo.pull
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper
import xyz.xenondevs.invui.gui.ScrollGui
import xyz.xenondevs.invui.gui.structure.Markers
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.item.impl.AbstractItem
import xyz.xenondevs.invui.item.impl.AsyncItem
import xyz.xenondevs.invui.window.Window
import java.util.function.Supplier

class ManagePilotsMenu(val main: StarshipComputerMenu) : AbstractItem() {
	override fun getItemProvider(): ItemProvider = ItemProvider {
		ItemStack(PLAYER_HEAD).updateDisplayName(text("Add / Remove pilots", WHITE).decoration(ITALIC, false))
	}

	override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
		val data = main.data as? PlayerStarshipData
		if (data == null) {
			player.userError("You can only add pilots to player ships!")
			return
		}

		openAddPilotMenu(player, data)
	}

	private fun asyncHeadItem(id: SLPlayerId): AsyncItem = object : AsyncItem(
		{ ItemStack(PLAYER_HEAD) },
		Supplier {
			ItemProvider {
				val name = SLPlayer.getName(id) ?: "null"

				skullItem(id.uuid, name, text(name).decoration(ITALIC, false))
					.updateLore(listOf(text("Left click to remove.", NamedTextColor.GRAY).decoration(ITALIC, false)))
			}
		}
	) {
		override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
			Tasks.async {
				val name = SLPlayer.getName(id)
				val data = main.data as PlayerStarshipData

				PlayerStarshipData.updateById(data._id, pull(PlayerStarshipData::pilots, id))
				data.pilots.remove(id)

				player.success("Removed $name")

				Tasks.sync {
					player.closeInventory()
					openAddPilotMenu(player, data)
				}
			}
		}
	}

	fun openAddPilotMenu(player: Player, data: PlayerStarshipData) {
		val heads = data.pilots.map(::asyncHeadItem)
		val gui = ScrollGui.items()
			.setStructure(
				"b . a . . . . l r",
				"x x x x x x x x x",
				"x x x x x x x x x"
			)
			.addIngredient('b', main.mainMenuButton)
			.addIngredient('a', AddNewPilotButton(this))
			.addIngredient('l', GuiItems.ScrollLeftItem())
			.addIngredient('r', GuiItems.ScrollRightItem())
			.addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
			.setContent(heads)
			.build()

		Window.single()
			.setViewer(player)
			.setTitle(AdventureComponentWrapper(text("Add / Remove Pilots").decoration(ITALIC, false)))
			.setGui(gui)
			.build()
			.open()
	}
}
