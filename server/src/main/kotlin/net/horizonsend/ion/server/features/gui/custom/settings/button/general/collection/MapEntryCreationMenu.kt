package net.horizonsend.ion.server.features.gui.custom.settings.button.general.collection

import net.horizonsend.ion.common.utils.input.InputResult
import net.horizonsend.ion.common.utils.text.gui.icons.GuiIcon
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.gui.invui.InvUIWindowWrapper
import net.horizonsend.ion.server.gui.invui.bazaar.getMenuTitleName
import net.horizonsend.ion.server.gui.invui.input.validator.ValidatorResult
import net.horizonsend.ion.server.gui.invui.utils.buttons.FeedbackLike
import net.horizonsend.ion.server.gui.invui.utils.buttons.makeGuiButton
import net.horizonsend.ion.server.miscellaneous.utils.updateDisplayName
import net.horizonsend.ion.server.miscellaneous.utils.updateLore
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.GREEN
import net.kyori.adventure.text.format.NamedTextColor.RED
import net.kyori.adventure.text.format.NamedTextColor.WHITE
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.window.Window
import java.util.function.Consumer

open class MapEntryCreationMenu<K : Any, V : Any>(
	viewer: Player,

	private val title: String,
	private val entryValidator: (Pair<K, V>) -> InputResult,

	private val keyItemFormatter: (K) -> ItemStack,
	private val keyNameFormatter: (K) -> Component,
	private val newKey: MapEntryCreationMenu<K, V>.(Player, Consumer<K>) -> Unit,

	private val valueItemFormatter: (V) -> ItemStack,
	private val valueNameFormatter: (V) -> Component,
	private val newValue: MapEntryCreationMenu<K, V>.(Player, Consumer<V>) -> Unit,

	private val valueConsumer: Consumer<Pair<K, V>>
) : InvUIWindowWrapper(viewer) {
	open var key: K? = null
	open var value: V? = null

	override fun buildWindow(): Window {
		val gui = Gui.normal()
			.setStructure(
				"x . . . . . . . .",
				"K k k k k k k k k",
				"V v v v v v v v v",
				". . . . . . . . c"
			)
			.addIngredient('x', parentOrBackButton())
			.addIngredient('K', keyIcon)
			.addIngredient('k', keyBacking)
			.addIngredient('V', valueIcon)
			.addIngredient('v', valueBacking)
			.addIngredient('c', confirmButton)
			.build()

		return normalWindow(gui)
	}

	override fun buildTitle(): Component {
		val background = GuiText(title)
			.setSlotOverlay(
				"# # # # # # # # #",
				"# # # # # # # # #",
				"# # # # # # # # #",
				"# # # # # # # # #",
			)
			.setGuiIconOverlay(
				". . . . . . . . .",
				". l c c c c c c r",
				". l c c c c c c r",
				". . . . . . . . ."
			)
			.addIcon('l', GuiIcon.textInputBoxLeft())
			.addIcon('c', GuiIcon.textInputBoxCenter())
			.addIcon('r', GuiIcon.textInputBoxRight())


		background.add(text("Select a key and value"), line = 0, horizontalShift = 22)
		background.add(text("to add to the map."), line = 1, horizontalShift = 22)

		val key = this.key
		if (key != null) {
			background.add(getMenuTitleName(keyNameFormatter.invoke(key)), horizontalShift = 22, line = 2, verticalShift = 4)
		} else {
			background.add(getMenuTitleName(text("No key specified!", WHITE)), horizontalShift = 22, line = 2, verticalShift = 4)
		}

		val value = this.value
		if (value != null) {
			background.add(getMenuTitleName(valueNameFormatter.invoke(value)), horizontalShift = 22, line = 4, verticalShift = 4)
		} else {
			background.add(getMenuTitleName(text("No value specified!", WHITE)), horizontalShift = 22, line = 4, verticalShift = 4)
		}

		return background.build()
	}

	fun verify(): ValidatorResult<Pair<K, V>> {
		val key = this.key ?: return ValidatorResult.FailureResult(text("No value set for the key!", RED))
		val value = this.value ?: return ValidatorResult.FailureResult(text("No value set for the value!", RED))

		val pair = Pair(key, value)

		val validator = entryValidator.invoke(pair)
		if (!validator.isSuccess()) return ValidatorResult.FailureResult(validator.getReason() ?: listOf())

		return ValidatorResult.ValidatorSuccessSingleEntry(pair)
	}

	private val confirmButton = FeedbackLike.withHandler(
		providedItem = {
			val validated = verify()

			if (validated.isSuccess()) GuiItem.CHECKMARK.makeItem(text("Confirm Creation", GREEN))
			else GuiItem.CANCEL.makeItem(text("Validation Failed!", RED)).updateLore(validated.getReason() ?: listOf())
		},
		clickHandler = { _, _ -> confirmEntry() }
	).tracked()

	private fun confirmEntry() {
		val validated = verify()
		confirmButton.updateWith(validated)
		val result = validated.result ?: return

		valueConsumer.accept(result)
	}

	private val keyIcon = ItemProvider {
		if (key == null) {
			return@ItemProvider ItemStack(Material.BARRIER).updateDisplayName(text("No key specified!", RED))
		}
		keyItemFormatter.invoke(key!!)

	}.makeGuiButton { _, _ -> modifyKey() }
	private val keyBacking = GuiItem.EMPTY.makeItem(text("")).makeGuiButton { _, _ -> modifyKey() }

	private fun modifyKey() {
		val consumer = Consumer { new: K -> key = new }
		newKey.invoke(this, viewer, consumer)

		refreshAll()
	}

	private val valueIcon = ItemProvider {
		if (value == null) {
			return@ItemProvider ItemStack(Material.BARRIER).updateDisplayName(text("No value specified!", RED))
		}

		valueItemFormatter.invoke(value!!)

	}.makeGuiButton { _, _ -> modifyValue() }
	private val valueBacking = GuiItem.EMPTY.makeItem(text("")).makeGuiButton { _, _ -> modifyValue() }

	private fun modifyValue() {
		val consumer = Consumer { new: V -> value = new }
		newValue.invoke(this, viewer, consumer)
		refreshAll()
	}
}
