package net.horizonsend.ion.server.features.nations.gui

import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui
import net.horizonsend.ion.common.utils.text.isAlphanumeric
import net.horizonsend.ion.common.utils.text.miniMessage
import net.horizonsend.ion.server.features.gui.custom.misc.anvilinput.TextInputMenu.Companion.anvilInputText
import net.horizonsend.ion.server.features.gui.custom.misc.anvilinput.validator.InputValidator
import net.horizonsend.ion.server.features.gui.custom.misc.anvilinput.validator.ValidatorResult
import net.horizonsend.ion.server.miscellaneous.utils.SLTextStyle
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.kyori.adventure.text.Component.text
import net.md_5.bungee.api.ChatColor
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
	var name = ""
	var color = ChatColor.BLUE

	playerClicker.anvilInputText(
		prompt = text("Enter role name"),
		description = text("3-20 characters; alphanumeric"),
		inputValidator = InputValidator { input: String ->
			when {
				!input.isAlphanumeric() -> ValidatorResult.FailureResult(text("Must be alphanumeric!"))
				input.length !in 3..20 -> ValidatorResult.FailureResult(text("Must be from 3 to 20 characters!"))
				else -> ValidatorResult.SuccessResult
			}
		}
	) { result ->
		name = result

		playerClicker.anvilInputText(
			prompt = "Enter <rainbow>Color".miniMessage(),
			inputValidator = InputValidator { input: String ->
				runCatching { ChatColor.valueOf(input) }.onFailure {
					ValidatorResult.FailureResult(text("Must be one of ${ChatColor.values().joinToString { it.name }}"))
				}

				ValidatorResult.SuccessResult
			}
		) { result ->
			color = ChatColor.valueOf(result)

			playerClicker.anvilInputText(
				prompt = text("Enter role weight"),
				description = text("Must be from 0 to 1000"),
				inputValidator = InputValidator { input: String ->
					val int = input.toIntOrNull() ?: return@InputValidator ValidatorResult.FailureResult(text("Not a valid number!"))
					if (int !in 0..1000) ValidatorResult.FailureResult(text("Must be from 0 to 1000")) else ValidatorResult.SuccessResult
				}
			) { result ->
				playerClicker.performCommand("$commandName create $name ${color.name} $result")
				Tasks.syncDelay(20) { playerClicker.performCommand("$commandName edit $name") }
			}
		}
	}
}

fun editRoleGUI(
    player: Player,
    commandName: String,
    roleName: String,
    roleColor: SLTextStyle,
    roleWeight: Int
) {
	val gui = ChestGui(1, "Edit Role $roleColor$roleName")

	gui.addPane(
		outlinePane(0, 0, 8, 1).apply
		{
			addItem(backButton("$commandName manage"))

			// Name Button
			addItem(guiButton(Material.NAME_TAG) {
				playerClicker.anvilInputText(
					prompt = text("Enter role name"),
					description = text("3-20 characters; alphanumeric"),
					inputValidator = InputValidator { input: String ->
						when {
							!input.isAlphanumeric() -> ValidatorResult.FailureResult(text("Must be alphanumeric!"))
							input.length !in 3..20 -> ValidatorResult.FailureResult(text("Must be from 3 to 20 characters!"))
							else -> ValidatorResult.SuccessResult
						}
					}
				) { result ->
					playerClicker.performCommand("$commandName edit name $roleName $result")
					Tasks.sync { playerClicker.performCommand("$commandName edit $result") }
				}
			}.name("Name: $roleName"))

			// Color Button
			addItem(guiButton(Material.INK_SAC) {
				playerClicker.anvilInputText(
					prompt = "Enter <rainbow>Color".miniMessage(),
					inputValidator = InputValidator { input: String ->
						runCatching { ChatColor.valueOf(input) }.onFailure {
							ValidatorResult.FailureResult(text("Must be one of ${ChatColor.values().joinToString { it.name }}"))
						}

						ValidatorResult.SuccessResult
					}
				) { result ->
					val color = ChatColor.valueOf(result)

					playerClicker.performCommand("$commandName edit color $roleName ${color.name}")
					Tasks.sync { playerClicker.performCommand("$commandName edit $roleName") }
				}
			}.name("Color: ${roleColor.name}"))

			// Weight Button
			addItem(guiButton(Material.ANVIL) {
				playerClicker.anvilInputText(
					prompt = text("Enter role weight"),
					description = text("Must be from 0 to 1000"),
					inputValidator = InputValidator { input: String ->
						val int = input.toIntOrNull() ?: return@InputValidator ValidatorResult.FailureResult(text("Not a valid number!"))
						if (int !in 0..1000) ValidatorResult.FailureResult(text("Must be from 0 to 1000")) else ValidatorResult.SuccessResult
					}
				) { result ->
					playerClicker.performCommand("$commandName edit weight $roleName $result")

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
    roleColor: SLTextStyle,
    rolePermissions: Set<P>,
    pValues: Array<P>
) {
	val gui = ChestGui(1 + (pValues.size + 1) / 9, "Edit Permissions for $roleColor$roleName")

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
