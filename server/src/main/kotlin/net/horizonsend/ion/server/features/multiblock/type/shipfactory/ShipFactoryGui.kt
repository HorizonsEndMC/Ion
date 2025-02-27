package net.horizonsend.ion.server.features.multiblock.type.shipfactory

import net.horizonsend.ion.common.database.schema.starships.Blueprint
import net.horizonsend.ion.common.utils.text.ADVANCED_SHIP_FACTORY_CHARACTER
import net.horizonsend.ion.common.utils.text.DEFAULT_GUI_WIDTH
import net.horizonsend.ion.common.utils.text.toComponent
import net.horizonsend.ion.server.command.starship.BlueprintCommand.showMaterials
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.GuiItems
import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.features.gui.GuiWrapper
import net.horizonsend.ion.server.features.gui.custom.blueprint.BlueprintMenu
import net.horizonsend.ion.server.features.gui.custom.misc.anvilinput.TextInputMenu.Companion.anvilInputText
import net.horizonsend.ion.server.features.gui.custom.misc.anvilinput.validator.InputValidator
import net.horizonsend.ion.server.features.gui.custom.misc.anvilinput.validator.ValidatorResult
import net.horizonsend.ion.server.features.gui.custom.settings.SettingsPageGui.Companion.createSettingsPage
import net.horizonsend.ion.server.features.gui.custom.settings.button.BooleanSupplierConsumerButton
import net.horizonsend.ion.server.features.gui.item.FeedbackItem
import net.horizonsend.ion.server.features.gui.item.FeedbackItem.FeedbackItemResult
import net.horizonsend.ion.server.features.gui.item.ValueScrollButton
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.slPlayerId
import net.horizonsend.ion.server.miscellaneous.utils.text.itemLore
import net.horizonsend.ion.server.miscellaneous.utils.updateDisplayName
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.ShadowColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.litote.kmongo.eq
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.window.Window

class ShipFactoryGui(private val viewer: Player, val entity: ShipFactoryEntity) : GuiWrapper {
	private fun isValid(): Boolean = entity.isAlive

	override fun open() = Tasks.async {
		val gui = Gui.normal()
			.setStructure(
				"s s s s s i . . .",
				"x y z . . . . d .",
				". . . . C c . . .",
				"X Y Z . . . . . .",
				"B A M R . P . e .",
				". 1 3 6 . I . . ."
			)
			.addIngredient('s', searchMenuBotton)
			.addIngredient('i', blueprintMenuBotton)
			.addIngredient('x', ValueScrollButton({ GuiItem.UP.makeItem(Component.text("Increase X offset")) }, false, { entity.settings.offsetX },+1, -100..100) { entity.settings.offsetX = it; entity.reCalculate() })
			.addIngredient('y', ValueScrollButton({ GuiItem.UP.makeItem(Component.text("Increase Y offset")) }, false, { entity.settings.offsetY },+1, -100..100) { entity.settings.offsetY = it; entity.reCalculate() })
			.addIngredient('z', ValueScrollButton({ GuiItem.UP.makeItem(Component.text("Increase Z offset")) }, false, { entity.settings.offsetZ },+1, -100..100) { entity.settings.offsetZ = it; entity.reCalculate() })
			.addIngredient('X', ValueScrollButton({ GuiItem.DOWN.makeItem(Component.text("Decrease X offset")) }, false, { entity.settings.offsetX },-1, -100..100) { entity.settings.offsetX = it; entity.reCalculate() })
			.addIngredient('Y', ValueScrollButton({ GuiItem.DOWN.makeItem(Component.text("Decrease Y offset")) }, false, { entity.settings.offsetY },-1, -100..100) { entity.settings.offsetY = it; entity.reCalculate() })
			.addIngredient('Z', ValueScrollButton({ GuiItem.DOWN.makeItem(Component.text("Decrease Z offset")) }, false, { entity.settings.offsetZ },-1, -100..100) { entity.settings.offsetZ = it; entity.reCalculate() })
			.addIngredient('c', ValueScrollButton({ GuiItem.CLOCKWISE.makeItem(Component.text("Rotate 90 degrees clockwise")) }, true, { entity.settings.rotation }, 90, -180..180) { entity.settings.rotation = it; entity.reCalculate() })
			.addIngredient('C', ValueScrollButton({ GuiItem.COUNTERCLOCKWISE.makeItem(Component.text("Rotate 90 degrees counterclockwise")) }, true, { entity.settings.rotation }, -90, -180..180) { entity.settings.rotation = it; entity.reCalculate() })
			.addIngredient('B', boundingBoxPreview)
			.addIngredient('A', GuiItems.CustomControlItem(Component.text("align"), GuiItem.ALIGN))
			.addIngredient('M', materialsButton)
			.addIngredient('R', resetButton)
			.addIngredient('1', getPreviewButton(GuiItem.ONE_QUARTER, 10))
			.addIngredient('3', getPreviewButton(GuiItem.TWO_QUARTER, 30))
			.addIngredient('6', getPreviewButton(GuiItem.THREE_QUARTER, 60))
			.addIngredient('P', placementMenu)
			.addIngredient('I', itemMenu)
			.addIngredient('d', disableButton)
			.addIngredient('e', enableButton)

		if (!isValid()) return@async

		entity.ensureBlueprintLoaded(viewer)

		Tasks.sync {
			Window
				.single()
				.setGui(gui)
				.setTitle(AdventureComponentWrapper(setGuiOverlay()))
				.build(viewer)
				.open()
		}
	}

	private fun setGuiOverlay(): Component {
		val text = GuiText(entity.guiTitle)
			.addBackground(GuiText.GuiBackground(
				backgroundChar = ADVANCED_SHIP_FACTORY_CHARACTER,
				backgroundWidth = 176 - 9,
				verticalShift = 10
			))
			.add(
				Component.text("Blueprint Stats").itemLore,
				verticalShift = -7 /* down 1 line */ + 2 /* Padding */,
				horizontalShift = DEFAULT_GUI_WIDTH + 2
			)
			.add(Component.text(entity.blueprintName).color(NamedTextColor.WHITE).shadowColor(ShadowColor.shadowColor(
				NamedTextColor.DARK_GRAY.red(),
				NamedTextColor.DARK_GRAY.green(),
				NamedTextColor.DARK_GRAY.blue(),
				255
			)), line = 0, verticalShift = 3, horizontalShift = 1)

//		val blueprint = entity.cachedBlueprintData
//		if (blueprint != null) {
//			text.add(
//				Component.text(blueprint.name).itemLore,
//				line = 1,
//				verticalShift = -7 /* down 1 line */ + 2 /* Padding */,
//				horizontalShift = DEFAULT_GUI_WIDTH + 2
//			)
//
//			val info = BlueprintCommand.blueprintInfo(blueprint).map(String::miniMessage)
//			info.forEachIndexed { index, component ->
//				text.add(
//					component.itemLore,
//					line = 2 + index,
//					verticalShift = -7 /* down 1 line */ + 2 /* Padding */,
//					horizontalShift = DEFAULT_GUI_WIDTH + 2
//				)
//			}
//		}

		return text.build()
	}

	private val enableButton: FeedbackItem = FeedbackItem
		.builder({ if (entity.isRunning) GuiItem.SHIP_FACTORY_RUNNING.makeItem(Component.text("Start")) else GuiItem.EMPTY.makeItem(Component.text("Start")) }) { _, player ->
			if (entity.userManager.currentlyUsed()) return@builder FeedbackItemResult.FailureLore(listOf(Component.text("This ship factory is already being used!", NamedTextColor.RED)))
			if (!entity.ensureBlueprintLoaded(player)) return@builder FeedbackItemResult.FailureLore(listOf(Component.text("Blueprint not found!", NamedTextColor.RED)))

			val otherCheckResults = entity.checkEnableButton(player)
			if (otherCheckResults != null) return@builder otherCheckResults

			FeedbackItemResult.SuccessLore(listOf(Component.text("Enabled ship factory.", NamedTextColor.GREEN)))
		}
		.withFallbackLore(listOf(Component.text("Start the ship factory.")))
		.withSuccessHandler { _, player ->
			Tasks.async {
				entity.enable(player)

				Tasks.sync {
					notifyWindows()
					disableButton.notifyWindows()
				}
			}
		}
		.build()

	private val disableButton: FeedbackItem = FeedbackItem
		.builder(GuiItem.EMPTY.makeItem(Component.text("Stop"))) { _, _ ->
			if (!entity.userManager.currentlyUsed()) return@builder FeedbackItemResult.FailureLore(listOf(Component.text("This ship factory not currently being used!", NamedTextColor.RED)))

			FeedbackItemResult.SuccessLore(listOf(Component.text("Disabled ship factory.", NamedTextColor.GREEN)))
		}
		.withFallbackLore(listOf(Component.text("Stop the ship factory.")))
		.withSuccessHandler { _, _ ->
			entity.disable()
			notifyWindows()
			enableButton.notifyWindows()
		}
		.build()

	private val blueprintMenuBotton = GuiItems.createButton(GuiItem.MAGNIFYING_GLASS.makeItem(Component.text("Open Blueprint Menu"))) { _, player, _ ->
		BlueprintMenu(player, GuiItems.createButton(ItemStack(Material.BARRIER).updateDisplayName(Component.text("Go back"))) { _, _, _ -> entity.openMenu(player) }) { blueprint, _ ->
			entity.setBlueprint(blueprint)
			entity.ensureBlueprintLoaded(player)
			entity.openMenu(player)
		}.open()
	}

	private val searchMenuBotton = GuiItems.createButton(GuiItem.EMPTY.makeItem(Component.text("Search for Blueprint"))) { _, player, _ ->
		val playerBlueprints = Blueprint.find(Blueprint::owner eq player.slPlayerId).associateBy { it.name }

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
			val blueprint = playerBlueprints[string]!!
			entity.setBlueprint(blueprint)

			entity.ensureBlueprintLoaded(player)
			entity.openMenu(player)
		}
	}

	private fun getPreviewButton(icon: GuiItem, seconds: Int) = FeedbackItem.builder(icon.makeItem(Component.text("Preview ${seconds}s"))) { _, player ->
			val ticks = seconds * 20L
			val preview = entity.getPreview(player, ticks) ?: return@builder FeedbackItemResult.FailureLore(listOf(Component.text("Blueprint not found!", NamedTextColor.RED)))

			Tasks.async {
				preview.preview()
			}

			FeedbackItemResult.Success
		}
		.build()

	private val boundingBoxPreview = FeedbackItem.builder(GuiItem.OUTLINE.makeItem(Component.text("Display Bounding Box"))) { _, player ->
			if (!entity.toggleBoundingBox(player))  return@builder FeedbackItemResult.FailureLore(listOf(Component.text("Blueprint not found!", NamedTextColor.RED)))
			entity.tickBoundingBoxTasks() // Tick now

			FeedbackItemResult.SuccessLore(listOf(Component.text("Toggled bounding box", NamedTextColor.GREEN)))
		}
		.build()

	private val resetButton = FeedbackItem.builder(GuiItem.CANCEL.makeItem(Component.text("Reset Offset"))) { _, _ ->
			if (!entity.canEditSettings()) FeedbackItemResult.FailureLore(listOf(Component.text("Placement settings can't be altered while running", NamedTextColor.RED)))
			FeedbackItemResult.Success
		}
		.withSuccessHandler { _, _ ->
			with(entity.settings) {
				offsetX = 0
				offsetY = 0
				offsetZ = 0
				rotation = 0
			}

			entity.reCalculate()
		}
		.build()

	private val materialsButton = FeedbackItem.builder(GuiItem.MATERIALS.makeItem(Component.text("Get Materials"))) { _, player ->
			if (!entity.ensureBlueprintLoaded(player)) {
				return@builder FeedbackItemResult.FailureLore(listOf(Component.text("You must load a blueprint first!", NamedTextColor.RED)))
			}
			FeedbackItemResult.Success
		}
		.withSuccessHandler { _, _ ->
			Tasks.async {
				showMaterials(viewer, entity.cachedBlueprintData ?: return@async)
			}
		}
		.build()

	private val placementMenu = GuiItems.createButton(GuiItem.GEAR.makeItem(Component.text("Placement Settings"))) { _, _, _ ->
		placementSettings.open()
	}
	private val placementSettings = createSettingsPage(
		viewer,
		"Placement Settings",
		BooleanSupplierConsumerButton(
			entity.settings::markObstrcutedBlocksAsComplete,
			{ entity.settings.markObstrcutedBlocksAsComplete = it },
			Component.text("Mark Obstructions Complete"),
			"Marks obstructed blocks as completed, rather than missing materials.",
			GuiItem.MATERIALS,
			true
		),
		BooleanSupplierConsumerButton(
			entity.settings::overrideReplaceableBlocks,
			{ entity.settings.overrideReplaceableBlocks = it },
			Component.text("Override Replaceable"),
			"Blueprint blocks will be placed in \"Replacable\" blocks (e.g. ferns), rather than marking them as obstructed. Defaults to true.",
			GuiItem.MATERIALS,
			true
		),
		BooleanSupplierConsumerButton(
			entity.settings::placeBlocksUnderwater,
			{ entity.settings.placeBlocksUnderwater = it },
			Component.text("Place Underwater"),
			"Allows blueprint blocks to be placed in water. Defaults to false.",
			GuiItem.MATERIALS,
			false
		),
	).apply { parent = this@ShipFactoryGui }

	private val itemMenu = GuiItems.createButton(GuiItem.GEAR.makeItem(Component.text("Item Settings"))) { _, _, _ ->
		itemSettings.open()
	}
	private val itemSettings = createSettingsPage(
		viewer,
		"Item Settings",
		BooleanSupplierConsumerButton(
			entity.settings::leaveItemRemaining,
			{ entity.settings.leaveItemRemaining = it },
			Component.text("Leave One Item Remaining"),
			"Leaves one of each item remaining in input inventories.",
			GuiItem.MATERIALS,
			false
		),
		BooleanSupplierConsumerButton(
			entity.settings::grabFromNetworkedPipes,
			{ entity.settings.grabFromNetworkedPipes = it },
			Component.text("Grab From Networked Pipes"),
			"Toggles whether to pathfind to find network chests. Only applicable to the Advanced Ship Factory.",
			GuiItem.MATERIALS,
			false
		),
	).apply { parent = this@ShipFactoryGui }
}
