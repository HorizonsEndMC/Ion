package net.horizonsend.ion.server.features.gui.custom.blueprint

import net.horizonsend.ion.common.database.schema.misc.SLPlayerId
import net.horizonsend.ion.common.database.schema.starships.Blueprint
import net.horizonsend.ion.common.utils.text.miniMessage
import net.horizonsend.ion.server.command.starship.BlueprintCommand.blueprintInfo
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.GuiItems
import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.features.gui.item.AsyncItem
import net.horizonsend.ion.server.features.nations.gui.playerClicker
import net.horizonsend.ion.server.gui.CommonGuiWrapper
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.actualType
import net.horizonsend.ion.server.miscellaneous.utils.slPlayerId
import net.horizonsend.ion.server.miscellaneous.utils.updateDisplayName
import net.horizonsend.ion.server.miscellaneous.utils.updateLore
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.litote.kmongo.descendingSort
import org.litote.kmongo.eq
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.gui.structure.Markers
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.item.impl.SimpleItem
import xyz.xenondevs.invui.window.Window

class BlueprintMenu(val player: Player, private val backButton: Item? = null, val consumer: (Blueprint, Player) -> Unit) : CommonGuiWrapper {
	private val slPlayerId: SLPlayerId = player.slPlayerId

	override fun openGui() {
		Tasks.async {
			val blueprints: List<Blueprint> = Blueprint
				.find(Blueprint::owner eq slPlayerId)
				.descendingSort(Blueprint::size)
				.toList()

			val items = blueprints.map { blueprint ->
				AsyncItem(
					{
						blueprint.type.actualType.menuItem.clone()
							.updateDisplayName(text(blueprint.name))
							.updateLore(blueprintInfo(blueprint).map(String::miniMessage))
					},
					{ event ->
						consumer.invoke(blueprint, event.playerClicker)
					}
				)
			}

			val gui = PagedGui.items()
				.setStructure(
					"x x x x x x x x x",
					"x x x x x x x x x",
					"x x x x x x x x x",
					"x x x x x x x x x",
					"x x x x x x x x x",
					"b . . . . . . < >",
				)
				.addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
				.addIngredient('#', ItemProvider { ItemStack(Material.BLACK_STAINED_GLASS) })
				.addIngredient('<', GuiItems.PageLeftItem())
				.addIngredient('>', GuiItems.PageRightItem())
				.addIngredient('b', backButton ?: SimpleItem(GuiItem.EMPTY.makeItem(Component.empty())))
				.setContent(items)
				.build()

			val guiText = GuiText("Your blueprints")
				.addBackground(GuiText.GuiBackground())

			Tasks.sync {
				Window
					.single()
					.setGui(gui)
					.setTitle(AdventureComponentWrapper(guiText.build()))
					.build(player)
					.open()
			}
		}
	}
}
