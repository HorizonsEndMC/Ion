package net.horizonsend.ion.server.gui.invui.economy

import net.horizonsend.ion.common.utils.text.gui.icons.GuiIcon
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.common.utils.text.toCreditComponent
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.features.nations.region.types.RegionRentalArea
import net.horizonsend.ion.server.gui.invui.InvUIWindowWrapper
import net.horizonsend.ion.server.gui.invui.bazaar.getMenuTitleName
import net.horizonsend.ion.server.gui.invui.utils.buttons.FeedbackLike
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.NamedTextColor.WHITE
import org.bukkit.entity.Player
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.window.Window

class RentalAreaPurchaseMenu(viewer: Player, val region: RegionRentalArea) : InvUIWindowWrapper(viewer, async = true) {
	override fun buildWindow(): Window {
		val gui = Gui.normal()
			.setStructure(
				". . . . . . . . .",
				". c c c . p p p .",
				". c c c . p p p .",
				". c c c . p p p .",
			)
			.addIngredient('c', parentOrBackButton(icon = GuiItem.EMPTY))
			.addIngredient('p', purchaseButton)
			.build()

		return normalWindow(gui)
	}

	override fun buildTitle(): Component {
		val text =  GuiText("")
			.add(template(text("{0} is available for rent."), getMenuTitleName(text(region.name, WHITE)), getMenuTitleName(region.rent.toCreditComponent())), line = -1, alignment = GuiText.TextAlignment.CENTER)
			.add(template(text("{0} will be charged weekly."), getMenuTitleName(region.rent.toCreditComponent())), line = 0, alignment = GuiText.TextAlignment.CENTER)
			.setSlotOverlay(
				"# # # # # # # # #",
				"# # # # # # # # #",
				"# # # # # # # # #",
				"# # # # # # # # #",
			)
			.setGuiIconOverlay(
				". . . . . . . . .",
				". . . . . . . . .",
				". . c . . . p . .",
				". . . . . . . . ."
			)
			.addIcon('p', GuiIcon.checkmarkIcon(NamedTextColor.GREEN, true))
			.addIcon('c', GuiIcon.crossIcon(NamedTextColor.RED, true))

		// if () TODO warning for purchasing multiple

		return text.build()
	}

	private val purchaseButton = FeedbackLike.withHandler(GuiItem.EMPTY.makeItem(text("Purchase"))) { _, _ -> /*TODO*/ }
}
