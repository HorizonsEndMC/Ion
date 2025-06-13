package net.horizonsend.ion.server.features.multiblock.type.shipfactory

import com.google.common.collect.Maps
import net.horizonsend.ion.common.database.schema.starships.Blueprint
import net.horizonsend.ion.common.utils.input.InputResult
import net.horizonsend.ion.common.utils.text.ADVANCED_SHIP_FACTORY_CHARACTER
import net.horizonsend.ion.common.utils.text.DEFAULT_GUI_WIDTH
import net.horizonsend.ion.common.utils.text.miniMessage
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.common.utils.text.toCreditComponent
import net.horizonsend.ion.server.command.GlobalCompletions.fromItemString
import net.horizonsend.ion.server.command.starship.BlueprintCommand.blueprintInfo
import net.horizonsend.ion.server.command.starship.BlueprintCommand.showMaterials
import net.horizonsend.ion.server.features.economy.bazaar.Bazaars
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.GuiItems
import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.features.gui.custom.settings.SettingsPageGui.Companion.createSettingsPage
import net.horizonsend.ion.server.features.gui.custom.settings.button.general.BooleanSupplierConsumerButton
import net.horizonsend.ion.server.features.gui.custom.settings.button.general.DoubleSupplierConsumerInputButton
import net.horizonsend.ion.server.features.gui.custom.settings.button.general.collection.CollectionModificationButton
import net.horizonsend.ion.server.features.gui.custom.settings.button.general.collection.MapEntryCreationMenu
import net.horizonsend.ion.server.features.gui.custom.settings.button.general.collection.MapEntryEditorMenu
import net.horizonsend.ion.server.features.gui.item.FeedbackItem
import net.horizonsend.ion.server.features.gui.item.ValueScrollButton
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.type.DisplayNameMultilblock.Companion.getDisplayName
import net.horizonsend.ion.server.features.multiblock.type.economy.BazaarTerminalMultiblock
import net.horizonsend.ion.server.features.multiblock.type.processing.automason.AutoMasonMultiblockEntity
import net.horizonsend.ion.server.gui.invui.InvUIWindowWrapper
import net.horizonsend.ion.server.gui.invui.bazaar.getMenuTitleName
import net.horizonsend.ion.server.gui.invui.misc.BlueprintMenu
import net.horizonsend.ion.server.gui.invui.misc.util.input.ItemMenu
import net.horizonsend.ion.server.gui.invui.misc.util.input.TextInputMenu.Companion.anvilInputText
import net.horizonsend.ion.server.gui.invui.misc.util.input.TextInputMenu.Companion.searchEntires
import net.horizonsend.ion.server.gui.invui.misc.util.input.validator.RangeDoubleValidator
import net.horizonsend.ion.server.gui.invui.utils.buttons.makeGuiButton
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.actualType
import net.horizonsend.ion.server.miscellaneous.utils.displayNameComponent
import net.horizonsend.ion.server.miscellaneous.utils.slPlayerId
import net.horizonsend.ion.server.miscellaneous.utils.text.itemLore
import net.horizonsend.ion.server.miscellaneous.utils.toMap
import net.horizonsend.ion.server.miscellaneous.utils.updateDisplayName
import net.horizonsend.ion.server.miscellaneous.utils.updateLore
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.NamedTextColor.GREEN
import net.kyori.adventure.text.format.NamedTextColor.RED
import net.kyori.adventure.text.format.NamedTextColor.WHITE
import net.kyori.adventure.text.format.ShadowColor
import org.bukkit.entity.Player
import org.litote.kmongo.eq
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.window.Window
import java.util.function.Consumer

class ShipFactoryGui(viewer: Player, val entity: ShipFactoryEntity) : InvUIWindowWrapper(viewer, async = true) {
	private fun isValid(): Boolean = entity.isAlive

	override fun buildWindow(): Window? {
		val gui = Gui.normal()
			.setStructure(
				"s s s s s i . . .",
				"x y z . . . . d .",
				". . . . C c . . .",
				"X Y Z . . . . . .",
				"B A M R . P . e .",
				"m 1 3 6 . I . . ."
			)
			.addIngredient('s', searchMenuBotton)
			.addIngredient('i', blueprintMenuBotton)
			.addIngredient('x', tracked { id ->
				ValueScrollButton({ GuiItem.UP.makeItem(text("Increase X offset")) }, false, { entity.settings.offsetX },+1, -100..100) {
					entity.settings.offsetX = it
					entity.reCalculate()
					refreshButtons(id)
				}
			})
			.addIngredient('y', tracked { id ->
				ValueScrollButton({ GuiItem.UP.makeItem(text("Increase Y offset")) }, false, { entity.settings.offsetY },+1, -100..100) {
					entity.settings.offsetY = it
					entity.reCalculate()
					refreshButtons(id)
				}
			})
			.addIngredient('z', tracked { id ->
				ValueScrollButton({ GuiItem.UP.makeItem(text("Increase Z offset")) }, false, { entity.settings.offsetZ },+1, -100..100) {
					entity.settings.offsetZ = it
					entity.reCalculate()
					refreshButtons(id)
				}
			})
			.addIngredient('X', tracked { id ->
				ValueScrollButton({ GuiItem.DOWN.makeItem(text("Decrease X offset")) }, false, { entity.settings.offsetX },-1, -100..100) {
					entity.settings.offsetX = it
					entity.reCalculate()
					refreshButtons(id)
				}
			})
			.addIngredient('Y', tracked { id ->
				ValueScrollButton({ GuiItem.DOWN.makeItem(text("Decrease Y offset")) }, false, { entity.settings.offsetY },-1, -100..100) {
					entity.settings.offsetY = it
					entity.reCalculate()
					refreshButtons(id)
				}
			})
			.addIngredient('Z', tracked { id ->
				ValueScrollButton({ GuiItem.DOWN.makeItem(text("Decrease Z offset")) }, false, { entity.settings.offsetZ },-1, -100..100) {
					entity.settings.offsetZ = it
					entity.reCalculate()
					refreshButtons(id)
				}
			})
			.addIngredient('c', tracked { id ->
				ValueScrollButton({ GuiItem.CLOCKWISE.makeItem(text("Rotate 90 degrees clockwise")) }, true, { entity.settings.rotation }, 90, -180..180) {
					entity.settings.rotation = it
					entity.reCalculate()
					refreshButtons(id)
				}
			})
			.addIngredient('C', tracked { id ->
				ValueScrollButton({ GuiItem.COUNTERCLOCKWISE.makeItem(text("Rotate 90 degrees counterclockwise")) }, true, { entity.settings.rotation }, -90, -180..180) {
					entity.settings.rotation = it
					entity.reCalculate()
					refreshButtons(id)
				}
			})
			.addIngredient('B', boundingBoxPreview)
			.addIngredient('A', GuiItems.CustomControlItem(text("Align [Coming Soon]"), GuiItem.ALIGN))
			.addIngredient('M', materialsButton)
			.addIngredient('R', resetButton)
			.addIngredient('1', getPreviewButton(GuiItem.ONE_QUARTER, 10))
			.addIngredient('3', getPreviewButton(GuiItem.TWO_QUARTER, 30))
			.addIngredient('6', getPreviewButton(GuiItem.THREE_QUARTER, 60))
			.addIngredient('P', placementMenu)
			.addIngredient('I', itemMenu)
			.addIngredient('d', disableButton)
			.addIngredient('e', enableButton)
			.addIngredient('m', getMergeIndicator())
			.build()

		if (!isValid()) return null

		entity.ensureBlueprintLoaded(viewer)

		return normalWindow(gui)
	}

	override fun buildTitle(): Component {
		val text = GuiText(entity.guiTitle)
			.addBackground(GuiText.GuiBackground(
				backgroundChar = ADVANCED_SHIP_FACTORY_CHARACTER,
				backgroundWidth = 176 - 9,
				verticalShift = 10
			))
			.add(
				text("Blueprint Stats").itemLore,
				verticalShift = -7 /* down 1 line */ + 2 /* Padding */,
				horizontalShift = DEFAULT_GUI_WIDTH + 2
			)
			.add(text(entity.blueprintName).color(NamedTextColor.WHITE).shadowColor(ShadowColor.shadowColor(
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

	val enableButton: FeedbackItem = FeedbackItem
		.builder({ if (entity.isRunning) GuiItem.SHIP_FACTORY_RUNNING.makeItem(text("Start")) else GuiItem.EMPTY.makeItem(text("Start")) }) { _, player ->
			if (entity.userManager.currentlyUsed()) return@builder InputResult.FailureReason(listOf(text("This ship factory is already being used!", RED)))
			if (!entity.ensureBlueprintLoaded(player)) return@builder InputResult.FailureReason(listOf(text("Blueprint not found!", RED)))

			val otherCheckResults = entity.checkEnableButton(player)
			if (otherCheckResults != null) return@builder otherCheckResults

			InputResult.SuccessReason(listOf(text("Enabled ship factory.", GREEN)))
		}
		.withStaticFallbackLore(listOf(text("Start the ship factory.")))
		.withSuccessHandler { _, player ->
			Tasks.async {
				entity.enable(player, this@ShipFactoryGui)

				Tasks.sync {
					notifyWindows()
					disableButton.notifyWindows()
				}
			}
		}
		.build()
		.tracked()

	private val disableButton: FeedbackItem = FeedbackItem
		.builder(GuiItem.EMPTY.makeItem(text("Stop"))) { _, _ ->
			if (!entity.userManager.currentlyUsed()) return@builder InputResult.FailureReason(listOf(text("This ship factory not currently being used!", RED)))

			InputResult.SuccessReason(listOf(text("Disabled ship factory.", GREEN)))
		}
		.withStaticFallbackLore(listOf(text("Stop the ship factory.")))
		.withSuccessHandler { _, _ ->
			entity.disable()
			notifyWindows()
			enableButton.notifyWindows()
		}
		.build()

	private val blueprintMenuBotton = GuiItems.createButton(GuiItem.MAGNIFYING_GLASS.makeItem(text("Open Blueprint Menu"))) { _, player, _ ->
		BlueprintMenu(player) { blueprint, _ ->
			entity.setBlueprint(blueprint)
			entity.ensureBlueprintLoaded(player)
			entity.openMenu(player)
		}.openGui(this)
	}

	private val searchMenuBotton = GuiItems.createButton(GuiItem.EMPTY.makeItem(text("Search for Blueprint"))) { _, player, _ ->
		val playerBlueprints = Blueprint.find(Blueprint::owner eq player.slPlayerId).toList()

		player.searchEntires(
			entries = playerBlueprints,
			searchTermProvider = { listOf(it.name, it.type) },
			prompt = text("Search for Blueprint"),
			backButtonHandler = { entity.openMenu(player) },
			componentTransformer = { text(it.name, it.type.actualType.textColor) },
			itemTransformer = { blueprint ->
				blueprint.type.actualType.menuItem.clone()
					.updateDisplayName(text(blueprint.name))
					.updateLore(blueprintInfo(blueprint).map(String::miniMessage))
			},
			handler = { _, blueprint ->
				entity.setBlueprint(blueprint)
				entity.ensureBlueprintLoaded(player)
				entity.openMenu(player)
			}
		)

//		player.anvilInputText(
//			prompt = text("Enter Blueprint Name"),
//			backButtonHandler = { entity.openMenu(player) },
//			inputValidator = InputValidator { text ->
//				if (playerBlueprints.isEmpty()) return@InputValidator ValidatorResult.FailureResult(text("You don't have any blueprints!"))
//
//				val filtered = searchEntriesMultipleTerms(text, playerBlueprints) { listOf(it.name, it.type) }
//
////				val filtered = playerBlueprints.keys.filter { it.startsWith(text) }
//				if (filtered.isEmpty()) return@InputValidator ValidatorResult.FailureResult(text("Blueprint not found!"))
//
//				val exactMatch = playerBlueprints.firstOrNull { it.name == text }
//				if (exactMatch != null) {
//					return@InputValidator ValidatorResult.ValidatorSuccessSingleEntry(exactMatch.name, exactMatch to exactMatch.name.toComponent())
//				}
//
//				return@InputValidator ValidatorResult.ValidatorSuccessMultiEntry(text, filtered.map { it to it.name.toComponent() })
//			},
//			componentTransformer = { it.second }
//		) { _, (_, result) ->
//			val blueprint: Blueprint = (
//				if (result is ValidatorResult.ValidatorSuccessSingleEntry) result.result.first
//				else result.result.first
//			)
//
//			entity.setBlueprint(blueprint)
//			entity.ensureBlueprintLoaded(player)
//			entity.openMenu(player)
//		}
	}

	private fun getPreviewButton(icon: GuiItem, seconds: Int) = FeedbackItem.builder(icon.makeItem(text("Preview ${seconds}s"))) { _, player ->
			val ticks = seconds * 20L
			val preview = entity.getPreview(player, ticks) ?: return@builder InputResult.FailureReason(listOf(text("Blueprint not found!", RED)))

			Tasks.async {
				preview.preview()
			}

			InputResult.InputSuccess
		}
		.build()

	private val boundingBoxPreview = FeedbackItem.builder(GuiItem.OUTLINE.makeItem(text("Display Bounding Box"))) { _, player ->
			if (!entity.toggleBoundingBox(player))  return@builder InputResult.FailureReason(listOf(text("Blueprint not found!", RED)))
			entity.tickBoundingBoxTasks() // Tick now

			InputResult.SuccessReason(listOf(text("Toggled bounding box", GREEN)))
		}
		.build()

	private val resetButton = FeedbackItem.builder(GuiItem.CANCEL.makeItem(text("Reset Offset"))) { _, _ ->
			if (!entity.canEditSettings()) return@builder InputResult.FailureReason(listOf(text("Placement settings can't be altered while running", RED)))
			InputResult.InputSuccess
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

	private val materialsButton = FeedbackItem.builder(GuiItem.MATERIALS.makeItem(text("Get Materials"))) { _, player ->
			if (!entity.ensureBlueprintLoaded(player)) {
				return@builder InputResult.FailureReason(listOf(text("You must load a blueprint first!", RED)))
			}
			InputResult.InputSuccess
		}
		.withSuccessHandler { _, _ ->
			Tasks.async {
				showMaterials(viewer, entity.cachedBlueprintData ?: return@async)
			}
		}
		.build()

	private val placementMenu = GuiItems.createButton(GuiItem.GEAR.makeItem(text("Placement Settings"))) { _, _, _ ->
		placementSettings.openGui()
	}

	private val placementSettings = createSettingsPage(
		viewer,
		"Placement Settings",
		BooleanSupplierConsumerButton(
			entity.settings::markObstrcutedBlocksAsComplete,
			{ entity.settings.markObstrcutedBlocksAsComplete = it },
			text("Mark Obstructions Complete"),
			"Marks obstructed blocks as completed, rather than missing materials.",
			GuiItem.MATERIALS,
			true
		),
		BooleanSupplierConsumerButton(
			entity.settings::overrideReplaceableBlocks,
			{ entity.settings.overrideReplaceableBlocks = it },
			text("Override Replaceable"),
			"Blueprint blocks will be placed in \"Replacable\" blocks (e.g. ferns), rather than marking them as obstructed. Defaults to true.",
			GuiItem.MATERIALS,
			true
		),
		BooleanSupplierConsumerButton(
			entity.settings::placeBlocksUnderwater,
			{ entity.settings.placeBlocksUnderwater = it },
			text("Place Underwater"),
			"Allows blueprint blocks to be placed in water. Defaults to false.",
			GuiItem.MATERIALS,
			false
		),
	).apply { setParent(this@ShipFactoryGui) }

	private val itemMenu = GuiItems.createButton(GuiItem.GEAR.makeItem(text("Item Settings"))) { _, _, _ ->
		itemSettings.openGui()
	}
	private val itemSettings = createSettingsPage(
		viewer,
		"Item Settings",
		BooleanSupplierConsumerButton(
			entity.settings::leaveItemRemaining,
			{ entity.settings.leaveItemRemaining = it },
			text("Leave One Item Remaining"),
			"Leaves one of each item remaining in input inventories.",
			GuiItem.MATERIALS,
			false
		),
		BooleanSupplierConsumerButton(
			entity.settings::grabFromNetworkedPipes,
			{ entity.settings.grabFromNetworkedPipes = it },
			text("Grab From Networked Pipes"),
			"Toggles whether to pathfind to find network chests. Only applicable to the Advanced Ship Factory.",
			GuiItem.MATERIALS,
			false
		),
	).apply { setParent(this@ShipFactoryGui) }

	private fun getMergeIndicator(): Item {
		val empty = GuiItem.EMPTY.makeItem(Component.empty()).makeGuiButton { _, _ -> }

		if (entity !is AdvancedShipFactoryParent.AdvancedShipFactoryEntity) return empty

		val mergePartners = entity.getMergedWith()

		return when {
			mergePartners.isEmpty() -> empty
			mergePartners.size == 1 -> getMergeButtonForEntity(mergePartners.first())
			else -> getMultiMergeButton(mergePartners)
		}
	}

	private fun getMultiMergeButton(partners: Collection<MultiblockEntity>): Item {
		val buttons = partners.map(::getMergeButtonForEntity)

		return GuiItem.CHECKMARK.makeItem(template(text("Merged with {0}", GREEN), entity.multiblock.getDisplayName())).makeGuiButton { _, player ->
			ItemMenu(text("Select Merged"), player, buttons) { _ -> openGui() }.openGui(this)
		}
	}

	private fun getMergeButtonForEntity(mergePartner: MultiblockEntity): Item {
		return when (mergePartner) {
			is BazaarTerminalMultiblock.BazaarTerminalMultiblockEntity -> getShipFactoryMergeButton(mergePartner)
			is AutoMasonMultiblockEntity -> getAutoMasonMergeButton(mergePartner)
			else -> throw NotImplementedError() //TODO
		}
	}

	private fun getAutoMasonMergeButton(entity: AutoMasonMultiblockEntity): Item {
		return GuiItem.CHECKMARK.makeItem(template(text("Merged with {0}", GREEN), entity.multiblock.getDisplayName())).makeGuiButton { _, _ ->  }
	}

	private fun getShipFactoryMergeButton(entity: BazaarTerminalMultiblock.BazaarTerminalMultiblockEntity): Item {
		return GuiItem.CHECKMARK.makeItem(template(text("Merged with {0}", GREEN), entity.multiblock.getDisplayName())).makeGuiButton { _, _ ->
			createSettingsPage(
				viewer,
				"Bazaar Integration Settings",
				BooleanSupplierConsumerButton(
					valueSupplier = entity::enableShipFactoryIntegration,
					valueConsumer = { entity.enableShipFactoryIntegration = it },
					name = text("Enable Bazaar Integration"),
					description = "Toggles whether missing items should be bought from the bazaar, when available.",
					icon = GuiItem.LIST,
					defaultValue = true
				),
				BooleanSupplierConsumerButton(
					valueSupplier = entity::shipFactoryAllowRemote,
					valueConsumer = { entity.shipFactoryAllowRemote = it },
					name = text("Enable Remote Purchase"),
					description = "Toggles whether items should be purchased from remote bazaar listings.",
					icon = GuiItem.LIST,
					defaultValue = true
				),
				CollectionModificationButton(
					viewer = viewer,
					title = text("Item Price Caps"),
					description = "Click to Modify",
					collectionSupplier = { entity.shipFactoryMaxUnitPrice.entries },
					modifiedConsumer = { entity.shipFactoryMaxUnitPrice = it.toMap() },
					itemTransformer = { fromItemString(it.key) },
					getItemLines = { (key, value) ->
						Pair(
							template(text("Key: {0}"), getMenuTitleName(text(key, WHITE))),
							template(text("Value: {0}"), getMenuTitleName(value.toCreditComponent())),
						)
					},
					toMutableCollection = { it.toMutableSet() },
					playerModifier = { existing: Map.Entry<String, Double>, newConsumer: Consumer<Map.Entry<String, Double>> ->
						MapEntryEditorMenu(
							viewer = viewer,
							initKey = existing.key,
							initValue = existing.value,
							title = "Add a Price Limit",
							entryValidator = {
								if (!entity.shipFactoryMaxUnitPrice.containsKey(it.first)) return@MapEntryEditorMenu InputResult.FailureReason(listOf(
									text("This entry is no longer present!", RED),
								))

								InputResult.InputSuccess
							},
							keyItemFormatter = { fromItemString(it) },
							keyNameFormatter = { fromItemString(it).displayNameComponent },
							newKey = { player: Player, consumer: Consumer<String> ->
								Bazaars.searchStrings(
									player = player,
									prompt = text("Enter new key"),
									description = text("Any bazaar string"),
									backButtonHandler = { this.openGui() },
									consumer = {
										consumer.accept(it)
										this@MapEntryEditorMenu.openGui()
									}
								)
							},
							valueItemFormatter = { GuiItem.RIGHT.makeItem(text(it)) },
							valueNameFormatter = { it.toCreditComponent() },
							newValue = { player: Player, consumer: Consumer<Double> ->
								player.anvilInputText(
									prompt = text("Enter new value"),
									description = text("Between 0.01 & 10,000,000"),
									backButtonHandler = { this.openGui() },
									componentTransformer = { double: Double -> double.toCreditComponent() },
									inputValidator = RangeDoubleValidator(0.001..10_000_000.0),
									handler = { _, (_, validator) ->
										consumer.accept(validator.result)
										this@MapEntryEditorMenu.openGui()
									}
								)
							},
							valueConsumer = { newConsumer.accept(Maps.immutableEntry(it.first, it.second)) }
						).openGui(this@CollectionModificationButton)
					},
					entryCreator = { valueConsumer ->
						MapEntryCreationMenu(
							title = "Add a Price Limit",
							viewer = viewer,
							entryValidator = {
								if (entity.shipFactoryMaxUnitPrice.containsKey(it.first)) return@MapEntryCreationMenu InputResult.FailureReason(listOf(
									text("There is already a value present for this item!", RED),
									text("You must remove that value, or modify it.", RED),
								))

								InputResult.InputSuccess
							},
							keyItemFormatter = { fromItemString(it) },
							keyNameFormatter = { fromItemString(it).displayNameComponent },
							newKey = { player: Player, consumer: Consumer<String> ->
								Bazaars.searchStrings(
									player = player,
									prompt = text("Enter new key"),
									description = text("Any bazaar string"),
									backButtonHandler = { this.openGui() },
									consumer = {
										consumer.accept(it)
										this@MapEntryCreationMenu.openGui()
									}
								)
							},
							valueItemFormatter = { GuiItem.RIGHT.makeItem(text(it)) },
							valueNameFormatter = { it.toCreditComponent() },
							newValue = { player: Player, consumer: Consumer<Double> ->
								player.anvilInputText(
									prompt = text("Enter new value"),
									description = text("Between 0.01 & 10,000,000"),
									backButtonHandler = { this.openGui() },
									componentTransformer = { double: Double -> double.toCreditComponent() },
									inputValidator = RangeDoubleValidator(0.001..10_000_000.0),
									handler = { _, (_, validator) ->
										consumer.accept(validator.result)
										this@MapEntryCreationMenu.openGui()
									}
								)
							},
							valueConsumer = { valueConsumer.accept(Maps.immutableEntry(it.first, it.second)) }
						).openGui(this)
					}
				),
				DoubleSupplierConsumerInputButton(
					valueSupplier = entity::shipFactoryPriceCap,
					valueConsumer = { entity.shipFactoryPriceCap = it },
					0.001,
					10_000_000.0,
					text("Printing Price Cap"),
					"Sets a maximum credit usage during a print operation.",
					icon = GuiItem.LIST,
					defaultValue = 10_000_000.0
				),
				CollectionModificationButton(
					viewer = viewer,
					title = text("Restricted Item List"),
					description = "Click to Modify",
					collectionSupplier = { entity.shipFactoryItemRestriction.toList() },
					modifiedConsumer = { entity.shipFactoryItemRestriction = it.toTypedArray() },
					itemTransformer = { fromItemString(it) },
					getItemLines = { entry -> Pair(getMenuTitleName(text(entry, WHITE)), null) },
					toMutableCollection = { it.toMutableList() },
					playerModifier = null,
					entryCreator = { consumer ->
						Bazaars.searchStrings(
							player = viewer,
							prompt = text("Enter new key"),
							description = text("Any bazaar string"),
							backButtonHandler = { this.openGui() },
							consumer = {
								consumer.accept(it)
								this@CollectionModificationButton.openGui()
							}
						)
					}
				),
				BooleanSupplierConsumerButton(
					valueSupplier = entity::shipFactoryWhitelistMode,
					valueConsumer = { entity.shipFactoryWhitelistMode = it },
					name = text("Item List Whitelist Mode"),
					description = "When enabled, uses the restricted items list as a whitelist. When disabled, a blacklist.",
					icon = GuiItem.LIST,
					defaultValue = true
				),
			).openGui(this)
		}
	}
}
