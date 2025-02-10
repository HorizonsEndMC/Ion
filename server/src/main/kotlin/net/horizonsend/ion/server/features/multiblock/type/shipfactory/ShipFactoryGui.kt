package net.horizonsend.ion.server.features.multiblock.type.shipfactory

import net.horizonsend.ion.common.database.schema.starships.Blueprint
import net.horizonsend.ion.common.utils.text.ADVANCED_SHIP_FACTORY_CHARACTER
import net.horizonsend.ion.common.utils.text.toComponent
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.GuiItems
import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.features.gui.GuiWrapper
import net.horizonsend.ion.server.features.gui.custom.blueprint.BlueprintMenu
import net.horizonsend.ion.server.features.gui.custom.misc.anvilinput.TextInputMenu.Companion.anvilInputText
import net.horizonsend.ion.server.features.gui.custom.misc.anvilinput.validator.InputValidator
import net.horizonsend.ion.server.features.gui.custom.misc.anvilinput.validator.ValidatorResult
import net.horizonsend.ion.server.features.gui.item.FeedbackItem
import net.horizonsend.ion.server.features.gui.item.FeedbackItem.FeedbackItemResult
import net.horizonsend.ion.server.features.gui.item.ValueScrollButton
import net.horizonsend.ion.server.miscellaneous.utils.slPlayerId
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.ShadowColor
import org.bukkit.entity.Player
import org.litote.kmongo.eq
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.window.Window

class ShipFactoryGui(private val viewer: Player, val entity: ShipFactoryEntity) : GuiWrapper {
	private fun isValid(): Boolean = entity.isAlive

	override fun open() {
		if (!isValid()) return

		val gui = Gui.normal()
			.setStructure(
				"s s s s s i . . .",
				"x y z . ^ ^ . d .",
				". . . . . . . . .",
				"X Y Z . v v . . .",
				"B A M R . P . e .",
				". 1 3 6 . I . . ."
			)
			.addIngredient('s', searchMenuBotton)
			.addIngredient('i', blueprintMenuBotton)
			.addIngredient('x', ValueScrollButton(GuiItem.UP.makeItem(Component.text("Increase X offset")), false, { entity.settings.offsetX },+1, -100..100) { entity.settings.offsetX = it })
			.addIngredient('y', ValueScrollButton(GuiItem.UP.makeItem(Component.text("Increase Y offset")), false, { entity.settings.offsetY },+1, -100..100) { entity.settings.offsetY = it })
			.addIngredient('z', ValueScrollButton(GuiItem.UP.makeItem(Component.text("Increase Z offset")), false, { entity.settings.offsetZ },+1, -100..100) { entity.settings.offsetZ = it })
			.addIngredient('X', ValueScrollButton(GuiItem.UP.makeItem(Component.text("Decrease X offset")), false, { entity.settings.offsetX },-1, -100..100) { entity.settings.offsetX = it })
			.addIngredient('Y', ValueScrollButton(GuiItem.UP.makeItem(Component.text("Decrease Y offset")), false, { entity.settings.offsetY },-1, -100..100) { entity.settings.offsetY = it })
			.addIngredient('Z', ValueScrollButton(GuiItem.UP.makeItem(Component.text("Decrease Z offset")), false, { entity.settings.offsetZ },-1, -100..100) { entity.settings.offsetZ = it })
			.addIngredient('^', GuiItems.CustomControlItem(Component.text("up"), GuiItem.UP))
			.addIngredient('v', GuiItems.CustomControlItem(Component.text("down"), GuiItem.DOWN))
			.addIngredient('B', GuiItems.CustomControlItem(Component.text("outline"), GuiItem.OUTLINE))
			.addIngredient('A', GuiItems.CustomControlItem(Component.text("align"), GuiItem.ALIGN))
			.addIngredient('M', GuiItems.CustomControlItem(Component.text("materials"), GuiItem.MATERIALS))
			.addIngredient('R', GuiItems.CustomControlItem(Component.text("reset"), GuiItem.CANCEL))
			.addIngredient('1', GuiItems.CustomControlItem(Component.text("preview 10s"), GuiItem.ONE_QUARTER))
			.addIngredient('3', GuiItems.CustomControlItem(Component.text("preview 30s"), GuiItem.TWO_QUARTER))
			.addIngredient('6', GuiItems.CustomControlItem(Component.text("preview 60s"), GuiItem.THREE_QUARTER))
			.addIngredient('P', GuiItems.CustomControlItem(Component.text("placement settings"), GuiItem.GEAR))
			.addIngredient('I', GuiItems.CustomControlItem(Component.text("item settings"), GuiItem.GEAR))
			.addIngredient('d', disableButton)
			.addIngredient('e', enableButton)

		Window.single()
			.setGui(gui)
			.setTitle(AdventureComponentWrapper(setGuiOverlay()))
			.build(viewer)
			.open()
	}

	private fun setGuiOverlay(): Component = GuiText("Advanced Ship Factory")
		.addBackground(GuiText.GuiBackground(
			backgroundChar = ADVANCED_SHIP_FACTORY_CHARACTER,
			backgroundWidth = 250 - 9,
			verticalShift = 10
		))
		.add(Component.text(entity.blueprintName).color(NamedTextColor.WHITE).shadowColor(ShadowColor.shadowColor(
			NamedTextColor.DARK_GRAY.red(),
			NamedTextColor.DARK_GRAY.green(),
			NamedTextColor.DARK_GRAY.blue(),
			255
		)), line = 0, verticalShift = 3)
		.build()

	private val enableButton = FeedbackItem
		.builder(GuiItem.EMPTY.makeItem(Component.text("Start"))) { _, player ->
			if (entity.userManager.currentlyUsed()) return@builder FeedbackItemResult.FailureLore(listOf(Component.text("This ship factory is already being used!", NamedTextColor.RED)))
			if (!entity.ensureBlueprintLoaded(player)) return@builder FeedbackItemResult.FailureLore(listOf(Component.text("Blueprint not found!", NamedTextColor.RED)))
			FeedbackItemResult.Success
		}
		.withFallbackLore(listOf(Component.text("Start the ship factory.")))
		.withSuccessHandler { _, player ->
			entity.enable(player)
		}
		.build()

	private val disableButton = FeedbackItem
		.builder(GuiItem.EMPTY.makeItem(Component.text("Stop"))) { _, _ ->
			if (!entity.userManager.currentlyUsed()) return@builder FeedbackItemResult.FailureLore(listOf(Component.text("This ship factory not currently being used!", NamedTextColor.RED)))
			FeedbackItemResult.Success
		}
		.withFallbackLore(listOf(Component.text("Stop the ship factory.")))
		.withSuccessHandler { _, _ ->
			entity.disable()
		}
		.build()

	private val blueprintMenuBotton = GuiItems.createButton(GuiItem.MAGNIFYING_GLASS.makeItem(Component.text("Open Blueprint Menu"))) { _, player, _ ->
		BlueprintMenu(player) { blueprint, _ ->
			entity.blueprintName = blueprint.name
			entity.ensureBlueprintLoaded(player)
			entity.openMenu(player)
		}.open()
	}

	private val searchMenuBotton = GuiItems.createButton(GuiItem.EMPTY.makeItem(Component.text("Search for Blueprint"))) { _, player, _ ->
		val playerBlueprints = Blueprint.find(Blueprint::owner eq player.slPlayerId).associate { it.name to it._id }

		player.anvilInputText(
			prompt = Component.text("Enter Blueprint Name"),
			backButtonHandler = { entity.openMenu(player) },
			inputValidator = InputValidator { text ->
				if (playerBlueprints.isEmpty()) return@InputValidator ValidatorResult.FailureResult(Component.text("You don't have any blueprints!"))
				val filtered = playerBlueprints.keys.filter { it.contains(text) }
				if (filtered.isEmpty()) return@InputValidator ValidatorResult.FailureResult(Component.text("Blueprint not found!"))
				return@InputValidator ValidatorResult.ResultsResult(filtered.map { it.toComponent() })
			}
		) { string ->
			entity.blueprintName = string
			entity.ensureBlueprintLoaded(player)
			entity.openMenu(player)
		}
	}
}
