package net.horizonsend.ion.server.features.gui.custom.starship.pilots

import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.database.schema.misc.SLPlayerId
import net.horizonsend.ion.common.database.schema.starships.PlayerStarshipData
import net.horizonsend.ion.common.database.uuid
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.utils.text.BOLD
import net.horizonsend.ion.server.features.gui.GuiItems
import net.horizonsend.ion.server.features.nations.gui.skullItem
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.setDisplayNameAndGet
import net.horizonsend.ion.server.miscellaneous.utils.text.itemName
import net.kyori.adventure.text.Component.empty
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.NamedTextColor.WHITE
import org.bukkit.Material
import org.bukkit.Material.BARRIER
import org.bukkit.Material.PAPER
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import org.litote.kmongo.addToSet
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.item.impl.AbstractItem
import xyz.xenondevs.invui.item.impl.SimpleItem
import xyz.xenondevs.invui.window.AnvilWindow

class AddNewPilotButton(val pilotMenu: ManagePilotsMenu) : AbstractItem() {
	var currentName = ""

	private val nameConfirmButton = NameConfirmButton(this)

	override fun getItemProvider(): ItemProvider = ItemProvider {
		ItemStack(Material.BEACON).setDisplayNameAndGet(text("Add Pilot").itemName)
	}

	override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
		open(player)
	}

	private val returnToPilotMenu = GuiItems.createButton(
		ItemStack(BARRIER).setDisplayNameAndGet(text("Go back to pilot menu", WHITE).itemName)
	) { _, player, _ ->
		pilotMenu.openAddPilotMenu(player, pilotMenu.main.data as PlayerStarshipData)
	}

	private val namePreset = SimpleItem(ItemStack(PAPER).setDisplayNameAndGet(empty()))

	fun open(player: Player) {
		val gui = Gui.normal()
			.setStructure("n v x")
			.addIngredient('n', namePreset)
			.addIngredient('v', returnToPilotMenu)
			.addIngredient('x', nameConfirmButton)

		AnvilWindow.single()
			.setViewer(player)
			.setTitle(AdventureComponentWrapper(text("Enter Pilot Name")))
			.setGui(gui)
			.addRenameHandler { string ->
				currentName = string
				nameConfirmButton.update()
			}
			.build()
			.open()
	}

	private class NameConfirmButton(val addPilot: AddNewPilotButton) : AbstractItem() {
		val playerNotFoundItem = ItemStack(Material.SKELETON_SKULL)
			.setDisplayNameAndGet(text("Player not found!", NamedTextColor.RED, BOLD).itemName)

		val loadingItem = ItemStack(Material.PLAYER_HEAD)
			.setDisplayNameAndGet(text("Loading...", NamedTextColor.GRAY).itemName)

		var currentProvider: ItemProvider = ItemProvider { loadingItem }
		var id: SLPlayerId? = null

		override fun getItemProvider(): ItemProvider {
			return currentProvider
		}

		override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
			val playerId = id
			if (playerId != null) Tasks.async {
				val data = addPilot.pilotMenu.main.data
				(data as PlayerStarshipData).pilots += playerId

				PlayerStarshipData.updateById(data._id, addToSet(PlayerStarshipData::pilots, id))

				player.success("Added ${addPilot.currentName} as a pilot to starship.")

				Tasks.sync {
					player.closeInventory()
					addPilot.pilotMenu.openAddPilotMenu(player, data)
				}
			}
		}

		fun update() {
			currentProvider = ItemProvider { loadingItem }
			notifyWindows()

			Tasks.async {
				val slPlayer = SLPlayer[addPilot.currentName]

				currentProvider = if (slPlayer != null) ItemProvider {
					id = slPlayer._id
					skullItem(slPlayer._id.uuid, addPilot.currentName, text(addPilot.currentName).itemName)
				} else {
					id = null
					ItemProvider { playerNotFoundItem }
				}

				Tasks.sync { notifyWindows() }
			}
		}
	}
}
