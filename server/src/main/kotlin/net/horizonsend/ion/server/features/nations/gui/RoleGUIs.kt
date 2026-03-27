package net.horizonsend.ion.server.features.nations.gui

import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui
import net.horizonsend.ion.common.utils.text.isAlphanumeric
import net.horizonsend.ion.common.utils.text.miniMessage
import net.horizonsend.ion.server.gui.invui.misc.util.input.TextInputMenu.Companion.openInputMenu
import net.horizonsend.ion.server.gui.invui.misc.util.input.validator.InputValidator
import net.horizonsend.ion.server.gui.invui.misc.util.input.validator.RangeIntegerValidator
import net.horizonsend.ion.server.gui.invui.misc.util.input.validator.ValidatorResult
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.toBukkitColor
import net.kyori.adventure.text.Component.text
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack

fun Player.manageRolesGUI(commandName: String, roleItems: List<GuiItem>) {
	val gui = ChestGui(4, "Manage Roles")

	// top bar buttons
	gui.addPane(
		staticPane(0, 0, 9, 1)
			.withItem(
				guiButton(Material.WRITABLE_BOOK) {
					createRoleMenu(commandName)
				}.name("Create Role"),
				0, 0
			)
			.withItem(
				guiButton(ItemStack(Material.PLAYER_HEAD, 1)) {
					playerClicker.performCommand("$commandName members")
				}.name("Members"),
				1, 0
			)
	)

	// list
	gui.addPane(outlinePane(0, 1, 9, 3).withItems(roleItems))

	gui.show(this)
}

private fun InventoryClickEvent.createRoleMenu(commandName: String) {
	// Using lateinit vars to capture state across the nested input chain
	lateinit var nameValue: String
	var redValue = 0
	var greenValue = 0
	var blueValue = 0

	// Using lateinit function references to handle forward references in back button handlers
	lateinit var promptForName: () -> Unit
	lateinit var promptForRed: () -> Unit
	lateinit var promptForGreen: () -> Unit
	lateinit var promptForBlue: () -> Unit

	fun promptForWeight() {
		playerClicker.openInputMenu(
			prompt = text("Enter role weight"),
			description = text("Must be from 0 to 1000"),
			backButtonHandler = { promptForBlue() },
			inputValidator = RangeIntegerValidator(0..1000)
		) { _, result ->
			playerClicker.performCommand("$commandName create $nameValue $redValue $greenValue $blueValue ${result.result}")
			Tasks.syncDelay(20) { playerClicker.performCommand("$commandName edit $nameValue") }
		}
	}

	fun promptForBlue() {
		playerClicker.openInputMenu(
			prompt = "Enter <blue>Blue".miniMessage(),
			description = text("0-255"),
			backButtonHandler = { promptForGreen() },
			inputValidator = RangeIntegerValidator(0..255)
		) { _, result ->
			blueValue = result.result
			promptForWeight()
		}
	}

	promptForGreen = {
		playerClicker.openInputMenu(
			prompt = "Enter <green>Green".miniMessage(),
			description = text("0-255"),
			backButtonHandler = { promptForRed() },
			inputValidator = RangeIntegerValidator(0..255)
		) { _, result ->
			greenValue = result.result
			promptForBlue()
		}
	}

	promptForRed = {
		playerClicker.openInputMenu(
			prompt = "Enter <red>Red".miniMessage(),
			description = text("0-255"),
			backButtonHandler = { promptForName() },
			inputValidator = RangeIntegerValidator(0..255)
		) { _, result ->
			redValue = result.result
			promptForGreen()
		}
	}

	promptForName = {
		playerClicker.openInputMenu(
			prompt = text("Enter role name"),
			description = text("3-20 characters; alphanumeric"),
			backButtonHandler = { it.performCommand("$commandName manage") },
			inputValidator = InputValidator { input: String ->
				when {
					!input.isAlphanumeric() -> ValidatorResult.FailureResult(text("Must be alphanumeric!"))
					input.length !in 3..20 -> ValidatorResult.FailureResult(text("Must be from 3 to 20 characters!"))
					else -> ValidatorResult.ValidatorSuccessSingleEntry(input)
				}
			}
		) { _, entry ->
			nameValue = entry.result
			promptForRed()
		}
	}

	promptForName()
}

fun editRoleGUI(
    player: Player,
    commandName: String,
    roleName: String,
    roleColorDB: String,
    roleWeight: Int
) {
	val roleColor = roleColorDB.toBukkitColor()
	val title = "Edit Role $roleName"
	val gui = ChestGui(1, title)

	gui.addPane(
		outlinePane(0, 0, 8, 1).apply
		{
			addItem(backButton("$commandName manage"))

			// Name Button
			addItem(guiButton(Material.NAME_TAG) {
				playerClicker.openInputMenu(
					prompt = text("Enter role name"),
					description = text("3-20 characters; alphanumeric"),
					backButtonHandler = { it.performCommand("$commandName edit $roleName") },
					inputValidator = InputValidator { input: String ->
						when {
							!input.isAlphanumeric() -> ValidatorResult.FailureResult(text("Must be alphanumeric!"))
							input.length !in 3..20 -> ValidatorResult.FailureResult(text("Must be from 3 to 20 characters!"))
							else -> ValidatorResult.ValidatorSuccessSingleEntry(input)
						}
					}
				) { _, result ->
					playerClicker.performCommand("$commandName edit name $roleName ${result.result}")
					Tasks.sync { playerClicker.performCommand("$commandName edit ${result.result}") }
				}
			}.name("Name: $roleName"))

			// Color Button
			addItem(guiButton(Material.INK_SAC) {
				fun promptForRed() {
					playerClicker.openInputMenu(
						prompt = "Enter <red>Red".miniMessage(),
						description = text("0-255"),
						backButtonHandler = { it.performCommand("$commandName edit $roleName") },
						inputValidator = RangeIntegerValidator(0..255)
					) { _, redResult ->
						val red = redResult.result

						fun promptForGreen() {
							playerClicker.openInputMenu(
								prompt = "Enter <green>Green".miniMessage(),
								description = text("0-255"),
								backButtonHandler = { promptForRed() },
								inputValidator = RangeIntegerValidator(0..255)
							) { _, greenResult ->
								val green = greenResult.result

								playerClicker.openInputMenu(
									prompt = "Enter <blue>Blue".miniMessage(),
									description = text("0-255"),
									backButtonHandler = { promptForGreen() },
									inputValidator = RangeIntegerValidator(0..255)
								) { _, blueResult ->
									val blue = blueResult.result

									playerClicker.performCommand("$commandName edit color $roleName $red $green $blue")
									Tasks.sync { playerClicker.performCommand("$commandName edit $roleName") }
								}
							}
						}
						promptForGreen()
					}
				}
				promptForRed()
			}
				.name("Color: RGB(${roleColor.red}, ${roleColor.green}, ${roleColor.blue})")
				.lore("Click to change color", "Enter Red, Green, and Blue values (0-255)")
			)

			// Weight Button
			addItem(guiButton(Material.ANVIL) {
				playerClicker.openInputMenu(
					prompt = text("Enter role weight"),
					description = text("Must be from 0 to 1000"),
					backButtonHandler = { it.performCommand("$commandName edit $roleName") },
					inputValidator = RangeIntegerValidator(0..1000)
				) { _, validatorResult ->
					playerClicker.performCommand("$commandName edit weight $roleName ${validatorResult.result}")

					Tasks.sync { playerClicker.performCommand("$commandName edit $roleName") }
				}
			}.name("Weight: $roleWeight"))

			// Permissions Button
			addItem(guiButton(Material.KNOWLEDGE_BOOK) {
				playerClicker.performCommand("$commandName permission gui $roleName")
			}.name("Permissions"))
		}
	)

	// Delete Button, separate pane to be on the right
	gui.addPane(
		staticPane(8, 0, 1, 1).withItem(
			guiButton(Material.BUCKET) {
				val playerClicker = playerClicker
				playerClicker.openConfirmMenu("Delete role $roleName?", {
					playerClicker.performCommand("$commandName delete $roleName")
					Tasks.sync { playerClicker.performCommand("$commandName manage") }
				}, { playerClicker.performCommand("$commandName edit $roleName") })
			}.name("Delete"),
			0, 0
		)
	)

	gui.show(player)
}

fun <P : Enum<P>> editRolePermissionGUI(
    player: Player,
    commandName: String,
    roleName: String,
    rolePermissions: Set<P>,
    pValues: Array<P>
) {
	val title = "Edit Permissions for $roleName"
	val gui = ChestGui(1 + (pValues.size + 1) / 9, title)

	val backCommand = "$commandName edit $roleName"
	gui.addPane(
		outlinePane(0, 0, 9, gui.rows)
			.withItem(backButton(backCommand))
			.withItems(
				pValues.map {
					val enabled = rolePermissions.contains(it)

					return@map guiButton(if (enabled) Material.GREEN_STAINED_GLASS_PANE else Material.RED_STAINED_GLASS_PANE) {
						player.performCommand("$commandName permission ${if (enabled) "remove" else "add"} $roleName ${it.name}")

						Tasks.sync {
							player.performCommand("$commandName permission gui $roleName")
						}
					}.name(it.name)
				}
			)
	)

	gui.show(player)
}

fun membersRoleGUI(player: Player, commandName: String, members: List<GuiItem>) {
	player.openPaginatedMenu("Members", members, listOf(backButton("$commandName manage")))
}

fun Player.memberRoleGUI(commandName: String, playerName: String, playerRoles: Set<String>, allRoles: List<String>) {
	val gui = ChestGui(1 + (allRoles.size + 1) / 9, "Manage $playerName's roles")

	gui.addPane(
		outlinePane(0, 0, 9, gui.rows)
			.withItem(backButton("$commandName members"))
			.withItems(
				allRoles.map {
					val has = playerRoles.contains(it)
					guiButton(if (has) Material.GREEN_STAINED_GLASS_PANE else Material.GRAY_STAINED_GLASS_PANE) {
						performCommand("$commandName member ${if (has) "remove" else "add"} $playerName $it")
						Tasks.sync { performCommand("$commandName member gui $playerName") }
					}.name(it)
				}
			)
	)

	gui.show(this)
}

fun backButton(backCommand: String) =
	guiButton(Material.IRON_DOOR) { playerClicker.performCommand(backCommand) }.name("Back")
