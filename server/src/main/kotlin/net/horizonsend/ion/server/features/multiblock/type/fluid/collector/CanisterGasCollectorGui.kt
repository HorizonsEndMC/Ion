package net.horizonsend.ion.server.features.multiblock.type.fluid.collector

import net.horizonsend.ion.common.utils.text.DEFAULT_GUI_WIDTH
import net.horizonsend.ion.common.utils.text.GAS_COLLECTOR_BACKGROUND_CHARACTER
import net.horizonsend.ion.server.core.registration.IonRegistries
import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.features.gui.item.AsyncItem
import net.horizonsend.ion.server.features.world.IonWorld.Companion.ion
import net.horizonsend.ion.server.features.world.SpaceRegion
import net.horizonsend.ion.server.gui.invui.InvUIWindowWrapper
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.window.Window

class CanisterGasCollectorGui(viewer: Player, val entity: CanisterGasCollectorMultiblock.CanisterGasCollectorEntity) : InvUIWindowWrapper(viewer, async = true) {

	private val allGasses = IonRegistries.ATMOSPHERIC_GAS.getAll().filter { gas ->
		if (entity.world.name == "Ilius_horizonsend_eden" && gas.identifier == "METHANE") return@filter true

		val region = entity.world.ion.getSpaceRegion()
		when (gas.identifier) {
			"HYDROGEN" -> region == SpaceRegion.MONOLITH
			"METHANE" -> region == SpaceRegion.SPINE
			"NITROGEN" -> region == SpaceRegion.FRACTURE
			"XENON" -> false
			else -> true // everything else has no restriction
		}
	}

	override fun buildWindow(): Window? {
		if (!entity.isAlive) return null

		val guiBuilder = Gui.normal()
			.setStructure(
				". . . . . . . . .",
				"h f d b . a c e g",
				". . . . . . . . .",
				". . . . s . . . .",
				". . . . . . . . ."
			)
			.addIngredient('s', tracked { _ ->
				AsyncItem({
					entity.selectedGas?.containerKey?.getValue()?.constructItemStack()
						?: ItemStack(Material.BARRIER)
				}) {}
			})

		allGasses.forEachIndexed { index, gas ->
			val letter = 'a' + index
			guiBuilder.addIngredient(letter, tracked { _ ->
				AsyncItem({ gas.containerKey.getValue().constructItemStack() }) {
					entity.selectedGasIdentifier = gas.identifier
					refreshButtons()
				}
			})
		}

		return normalWindow(guiBuilder.build())
	}

	override fun buildTitle(): Component {
		val text = GuiText(entity.guiTitle)
			.addBackground(
				GuiText.GuiBackground(
					backgroundChar = GAS_COLLECTOR_BACKGROUND_CHARACTER,
					backgroundWidth = DEFAULT_GUI_WIDTH,
					verticalShift = 0,
					horizontalShift = 0
				)
			)
		return text.build()
	}
}
