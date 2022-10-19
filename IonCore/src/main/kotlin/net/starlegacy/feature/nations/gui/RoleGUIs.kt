package net.starlegacy.feature.nations.gui

import com.github.stefvanschie.inventoryframework.Gui
import com.github.stefvanschie.inventoryframework.GuiItem
import net.md_5.bungee.api.ChatColor
import net.starlegacy.PLUGIN
import net.starlegacy.util.SLTextStyle
import net.starlegacy.util.Tasks
import net.starlegacy.util.isAlphanumeric
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack

fun Player.manageRolesGUI(commandName: String, roleItems: List<GuiItem>) {
	val gui = Gui(PLUGIN, 4, "Manage Roles")

	// top bar buttons
	gui.addPane(
		staticPane(0, 0, 9, 1)
			.withItem(guiButton(Material.WRITABLE_BOOK) {
				createRoleMenu(commandName)
			}.name("Create Role"), 0, 0)
			.withItem(
				guiButton(ItemStack(Material.PLAYER_HEAD, 1)) {
					playerClicker.performCommand("$commandName members")
				}.name("Members"),
				1, 0
			)
	)

	//list
	gui.addPane(outlinePane(0, 1, 9, 3).withItems(roleItems))

	gui.show(this)
}

private fun InventoryClickEvent.createRoleMenu(commandName: String) {
	var name = ""
	var color = ChatColor.BLUE
	playerClicker.inputs(
		AnvilInput("Name") { _, r ->
			when {
				!r.isAlphanumeric() -> "Must be alphanumeric"
				r.length !in 3..20 -> "Must be from 3 to 20 characters"
				else -> {
					name = r; return@AnvilInput null
				}
			}
		},
		AnvilInput("Color") { _, r ->
			try {
				color = ChatColor.valueOf(r)
			} catch (e: Exception) {
				return@AnvilInput "Must be one of ${ChatColor.values().joinToString { it.name }}"
			}
			return@AnvilInput null
		},
		AnvilInput("Weight") { p, r ->
			if ((r.toIntOrNull() ?: return@AnvilInput "Must be a number")
				!in 0..1000
			) return@AnvilInput "Must be from 0 to 1000"

			// Final
			p.performCommand("$commandName create $name ${color.name} $r")
			Tasks.syncDelay(20) { p.performCommand("$commandName edit $name") }
			return@AnvilInput null
		})
}

fun editRoleGUI(
	player: Player,
	commandName: String,
	roleName: String,
	roleColor: SLTextStyle,
	roleWeight: Int
) {
	val gui = Gui(PLUGIN, 1, "Edit Role $roleColor$roleName")

	gui.addPane(
		outlinePane(0, 0, 8, 1).apply
		{
			addItem(backButton("$commandName manage"))

			// Name Button
			addItem(guiButton(Material.NAME_TAG) {
				playerClicker.input("Name") { p, r ->
					when {
						!r.isAlphanumeric() -> "Must be alphanumeric"
						r.length !in 3..20 -> "Must be from 3 to 20 characters"
						else -> {
							p.performCommand("$commandName edit name $roleName $r")
							Tasks.sync { p.performCommand("$commandName edit $r") }; return@input null
						}
					}
				}
			}.name("Name: $roleName"))

			// Color Button
			addItem(guiButton(Material.INK_SAC) {
				playerClicker.input("Color") { p, r ->
					try {
						val color = ChatColor.valueOf(r)

						p.performCommand("$commandName edit color $roleName ${color.name}")
						Tasks.sync { p.performCommand("$commandName edit $roleName") }

						return@input null
					} catch (e: Exception) {
						return@input "Must be one of ${ChatColor.values().joinToString { it.name }}"
					}
				}
			}.name("Color: ${roleColor.name}"))

			// Weight Button
			addItem(guiButton(Material.ANVIL) {
				playerClicker.input("Weight") { p, r ->
					if ((r.toIntOrNull() ?: return@input "Must be a number")
						!in 0..1000
					) return@input "Must be from 0 to 1000"
					else {
						p.performCommand("$commandName edit weight $roleName $r")

						Tasks.sync { p.performCommand("$commandName edit $roleName") }; return@input null
					}
				}
			}.name("Weight: $roleWeight"))

			// Permissions Button
			addItem(guiButton(Material.KNOWLEDGE_BOOK) {
				playerClicker.performCommand("$commandName permission gui $roleName")
			}.name("Permissions"))
		})

	// Delete Button, separate pane to be on the right
	gui.addPane(
		staticPane(8, 0, 1, 1).withItem(
			guiButton(Material.BUCKET)
			{
				val playerClicker = playerClicker
				playerClicker.openConfirmMenu("Delete role $roleName?", {
					playerClicker.performCommand("$commandName delete $roleName")
					Tasks.sync { playerClicker.performCommand("$commandName manage") }
				}, { playerClicker.performCommand("$commandName edit $roleName") })
			}.name("Delete"), 0, 0
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
	val gui = Gui(PLUGIN, 1 + (pValues.size + 1) / 9, "Edit Permissions for $roleColor$roleName")

	val backCommand = "$commandName edit $roleName"
	gui.addPane(
		outlinePane(0, 0, 9, gui.rows)
			.withItem(backButton(backCommand))
			.withItems(pValues.map {
				val enabled = rolePermissions.contains(it)

				return@map guiButton(if (enabled) Material.GREEN_STAINED_GLASS_PANE else Material.RED_STAINED_GLASS_PANE) {
					player.performCommand("$commandName permission ${if (enabled) "remove" else "add"} $roleName ${it.name}")

					Tasks.sync {
						player.performCommand("$commandName permission gui $roleName")
					}
				}.name(it.name)
			})
	)

	gui.show(player)
}

fun membersRoleGUI(player: Player, commandName: String, members: List<GuiItem>) {
	player.openPaginatedMenu("Members", members, listOf(backButton("$commandName manage")))
}

fun Player.memberRoleGUI(commandName: String, playerName: String, playerRoles: Set<String>, allRoles: List<String>) {
	val gui = Gui(PLUGIN, 1 + (allRoles.size + 1) / 9, "Manage $playerName's roles")

	gui.addPane(
		outlinePane(0, 0, 9, gui.rows)
			.withItem(backButton("$commandName members"))
			.withItems(allRoles.map {
				val has = playerRoles.contains(it)
				guiButton(if (has) Material.GREEN_STAINED_GLASS_PANE else Material.GRAY_STAINED_GLASS_PANE) {
					performCommand("$commandName member ${if (has) "remove" else "add"} $playerName $it")
					Tasks.sync { performCommand("$commandName member gui $playerName") }
				}.name(it)
			})
	)

	gui.show(this)
}

fun backButton(backCommand: String) =
	guiButton(Material.IRON_DOOR) { playerClicker.performCommand(backCommand) }.name("Back")
