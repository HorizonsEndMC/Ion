package net.horizonsend.ion.server.gui.invui.misc

import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.cache.nations.FrontierNationCache
import net.horizonsend.ion.common.database.schema.economy.BankedItem
import net.horizonsend.ion.common.database.schema.nations.FrontierNation
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.server.command.GlobalCompletions
import net.horizonsend.ion.server.features.economy.bazaar.Bazaars
import net.horizonsend.ion.server.features.gui.GuiItems
import net.horizonsend.ion.server.features.gui.item.AsyncItem
import net.horizonsend.ion.server.gui.invui.ListInvUIWindow
import net.horizonsend.ion.server.gui.invui.bazaar.stripAttributes
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.updateLore
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.litote.kmongo.eq
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.gui.structure.Markers
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.window.Window

class FrontierNationBankMenu(viewer: Player, val frontierNation: Oid<FrontierNation>) : ListInvUIWindow<BankedItem>(viewer, async = true) {
	override val listingsPerPage: Int = 27

	private fun getMenuGUI(): Gui = PagedGui.items()
		.setStructure(
			"x x x x x x x x x",
			"x x x x x x x x x",
			"x x x x x x x x x",
			"< . . . b . . . >"
		)
		.addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
		.addIngredient('<', GuiItems.PageLeftItem())
		.addIngredient('>', GuiItems.PageRightItem())
		.addIngredient('b', parentOrBackButton())
		.setContent(items)
		.addPageChangeHandler { _, new ->
			pageNumber = new
			refreshAll()
		}
		.build()

	override fun generateEntries(): List<BankedItem> = BankedItem.find(BankedItem::frontierNation eq frontierNation).toList()

	override fun createItem(entry: BankedItem): Item = AsyncItem(
		resultProvider = { GlobalCompletions.fromItemString(entry.itemString).stripAttributes().updateLore(listOf(
			Component.text("x${entry.quantity}", NamedTextColor.GRAY),
			Component.text("Click to withdraw item", NamedTextColor.AQUA).style(Style.style(TextDecoration.ITALIC))
		)) },
		handleClick = { _ ->
			val itemString = entry.itemString
			val quantity = entry.quantity

			val itemValidationResult = Bazaars.checkValidString(entry.itemString)
			val itemReference: ItemStack? = itemValidationResult.result
			if (itemReference == null) {
				itemValidationResult.sendReason(viewer)
				return@AsyncItem
			}

			BankedItem.delete(entry._id)
			Tasks.sync {
				val (fullStacks, remainder) = Bazaars.giveOrDropItems(itemReference, quantity, viewer)

				viewer.closeInventory()
				viewer.sendMessage(template(Component.text("Withdrew {0} of {1} ({2} stack(s) and {3} item(s))",
					NamedTextColor.GREEN), quantity, itemString, fullStacks, remainder))
			}
		}
	)

	override fun buildWindow(): Window = normalWindow(getMenuGUI())

	override fun buildTitle(): Component = Component.text("${FrontierNationCache[frontierNation].name}'s Banked Items")
}
