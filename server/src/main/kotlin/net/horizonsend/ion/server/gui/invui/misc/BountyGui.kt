package net.horizonsend.ion.server.gui.invui.misc

import net.horizonsend.ion.common.database.cache.nations.RelationCache
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.database.schema.nations.NationRelation
import net.horizonsend.ion.common.database.uuid
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_MEDIUM_GRAY
import net.horizonsend.ion.common.utils.text.gui.GuiBorder
import net.horizonsend.ion.common.utils.text.gui.icons.GuiIcon
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.common.utils.text.toCreditComponent
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.features.gui.GuiItems
import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.gui.invui.ListInvUIWindow
import net.horizonsend.ion.server.gui.invui.utils.buttons.getHeadItem
import net.horizonsend.ion.server.gui.invui.utils.buttons.makeInformationButton
import net.horizonsend.ion.server.miscellaneous.utils.updateDisplayName
import net.horizonsend.ion.server.miscellaneous.utils.updateLore
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.RED
import org.bukkit.entity.Player
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.gui.structure.Markers
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.window.Window

class BountyGui(viewer: Player) : ListInvUIWindow<SLPlayer>(viewer, async = true) {
	override val listingsPerPage: Int = 27

	override fun generateEntries(): List<SLPlayer> = SLPlayer.all()
		.sortedByDescending { it.bounty }
		.filter { it.bounty > 0.0 }

	override fun createItem(entry: SLPlayer): Item {
		val username = entry.lastKnownName
		val relation = getRelation(viewer, entry)
		val relationDisplay = text(relation.name, relation.color)

		return getHeadItem(entry._id.uuid, username, { it
			.updateDisplayName(text(username))
			.updateLore(listOf(
				template(text("Bounty: {0}", HE_MEDIUM_GRAY), entry.bounty.toCreditComponent()),
				template(text("Your relation: {0}", HE_MEDIUM_GRAY), relationDisplay),
			))
		}) {}
	}

	override fun buildWindow(): Window {
		val gui = PagedGui.items()
			.setStructure(
				"x . . . . . . . i",
				"# # # # # # # # #",
				"# # # # # # # # #",
				"# # # # # # # # #",
				"< . . . . . . . >",
			)
			.addIngredient('x', parentOrBackButton())
			.addIngredient('i', infoButton)
			.addIngredient('<', GuiItems.PageLeftItem())
			.addIngredient('>', GuiItems.PageRightItem())
			.addIngredient('#', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
			.setContent(items)
			.handlePageChange()
			.build()

		return normalWindow(gui)
	}

	val infoButton = makeInformationButton(
		text("Information"),
		text(""),
		text(""),
		text(""),
	)

	override val pageNumberLine: Int = 8

	override fun buildTitle(): Component {
		val text = GuiText("Available Bounties")
			.setSlotOverlay(
				"# # # # # # # # #",
				". . . . . . . . .",
				". . . . . . . . .",
				". . . . . . . . .",
				"# # # # # # # # #",
			)
			.addBorder(GuiBorder.regular(RED))
			.build()

		val overlays = GuiText("")
		val displayed = getDisplayedEntries()

		for (rowIndex in 0..2) {
			for (columnIndex in 0..8) {
				val itemindex = (rowIndex * 9) + columnIndex
				val displayedPlayer = displayed.getOrNull(itemindex) ?: continue
				val relation = getRelation(viewer, displayedPlayer)

				overlays.setIcon(rowIndex + 1, columnIndex, GuiIcon.coloredSlot(relation.color))
			}
		}

		return ofChildren(withPageNumber(text), overlays.build())
	}

	private fun getRelation(viewer: Player, entry: SLPlayer): NationRelation.Level {
		val senderNation = PlayerCache[viewer].nationOid
		val bountyHolderNation = entry.nation

		val relation = if (senderNation != null && bountyHolderNation != null) RelationCache[senderNation, bountyHolderNation] else NationRelation.Level.NONE

		return relation
	}
}
