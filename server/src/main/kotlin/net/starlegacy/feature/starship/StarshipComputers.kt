package net.starlegacy.feature.starship

import com.github.stefvanschie.inventoryframework.gui.GuiItem
import java.util.LinkedList
import net.horizonsend.ion.server.features.achievements.Achievement
import net.horizonsend.ion.common.extensions.hint
import net.horizonsend.ion.common.extensions.serverErrorActionMessage
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.successActionMessage
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.achievements.rewardAchievement
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor.RED
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.format.TextDecoration.BOLD
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.util.HSVLike
import net.horizonsend.ion.server.IonComponent
import net.horizonsend.ion.server.database.Oid
import net.horizonsend.ion.server.database.schema.misc.SLPlayer
import net.horizonsend.ion.server.database.schema.nations.Nation
import net.horizonsend.ion.server.database.schema.nations.Settlement
import net.horizonsend.ion.server.database.schema.nations.Territory
import net.horizonsend.ion.server.database.schema.starships.PlayerStarshipData
import net.horizonsend.ion.server.database.slPlayerId
import net.horizonsend.ion.server.database.uuid
import net.starlegacy.feature.nations.gui.playerClicker
import net.starlegacy.feature.nations.gui.skullItem
import net.starlegacy.feature.nations.region.Regions
import net.starlegacy.feature.nations.region.types.RegionTerritory
import net.starlegacy.feature.starship.PilotedStarships.getDisplayName
import net.starlegacy.feature.starship.active.ActiveStarships
import net.starlegacy.feature.starship.control.StarshipControl
import net.starlegacy.feature.starship.event.StarshipComputerOpenMenuEvent
import net.starlegacy.util.MenuHelper
import net.starlegacy.util.Tasks
import net.starlegacy.util.toText
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.conversations.Conversation
import org.bukkit.conversations.ConversationContext
import org.bukkit.conversations.Prompt
import org.bukkit.conversations.StringPrompt
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.litote.kmongo.addToSet
import org.litote.kmongo.pull
import org.litote.kmongo.setValue

object StarshipComputers : IonComponent() {

	val COMPUTER_TYPE = Material.JUKEBOX

	@EventHandler
	fun onInteract(event: PlayerInteractEvent) {
		val player = event.player
		val block = event.clickedBlock ?: return

		if (event.hand != EquipmentSlot.HAND) {
			return // it can fire with both hands
		}

		if (block.type != COMPUTER_TYPE) {
			return
		}

		if (!StarshipControl.isHoldingController(player)) {
			player.userError("Not holding starship controller, ignoring computer click")
			return
		}

		event.isCancelled = true
		val data: PlayerStarshipData? = StarshipComputers[event.player.world, block.x, block.y, block.z]

		when (event.action) {
			Action.LEFT_CLICK_BLOCK -> handleLeftClick(data, player, block)
			Action.RIGHT_CLICK_BLOCK -> handleRightClick(data, player)
			else -> return
		}
	}

	private fun handleLeftClick(data: PlayerStarshipData?, player: Player, block: Block) {
		if (data == null) {
			createComputer(player, block)
			return
		}

		tryOpenMenu(player, data)
	}

	private fun handleRightClick(data: PlayerStarshipData?, player: Player) {
		if (data == null) {
			return
		}

		PilotedStarships.tryPilot(player, data)
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	fun onBlockBreak(event: BlockBreakEvent) {
		val block = event.block
		// get a DEACTIVATED computer. Using StarshipComputers#get would include activated ones
		val computer: PlayerStarshipData = DeactivatedPlayerStarships[block.world, block.x, block.y, block.z]
			?: return
		val player = event.player
		DeactivatedPlayerStarships.destroyAsync(computer) {
			player.successActionMessage("Destroyed starship computer")
		}
	}

	operator fun get(world: World, x: Int, y: Int, z: Int): PlayerStarshipData? {
		return ActiveStarships.getByComputerLocation(world, x, y, z) ?: DeactivatedPlayerStarships[world, x, y, z]
	}

	private fun createComputer(player: Player, block: Block) {
//		if (isRegionDenied(player, player.location)) return player.userError("You can only detect computers in territories you can access.")

		DeactivatedPlayerStarships.createAsync(block.world, block.x, block.y, block.z, player.uniqueId) {
			player.successActionMessage(
				"Registered starship computer! Left click again to open the menu."
			)
		}
	}

	private fun tryOpenMenu(player: Player, data: PlayerStarshipData) {
		if (!data.isPilot(player) && !player.hasPermission("ion.core.starship.override") && !player.isTerritoryOwner()) {
			Tasks.async {
				val name: String? = SLPlayer.getName(data.captain)
				if (name != null) {
					player.userError(
						"You're not a pilot of this ship! The captain is $name"
					)
				}
			}
			return
		}

		if (!StarshipComputerOpenMenuEvent(player).callEvent()) {
			return
		}

		MenuHelper.apply {
			val pane = staticPane(0, 0, 9, 1)

			pane.addItem(
				guiButton(StarshipControl.CONTROLLER_TYPE) {
					tryReDetect(playerClicker, data)
				}.setName(MiniMessage.miniMessage().deserialize("<dark_purple>Re-detect")),
				0, 0
			)

			pane.addItem(
				guiButton(Material.PLAYER_HEAD) {
					openPilotsMenu(playerClicker, data)
				}.setName(MiniMessage.miniMessage().deserialize("<gold>Pilots")),
				1, 0
			)

			pane.addItem(
				guiButton(Material.GHAST_TEAR) {
					openTypeMenu(playerClicker, data)
				}.setName(MiniMessage.miniMessage().deserialize("<white>Type (${data.starshipType})")),
				2, 0
			)

			val lockDisplayTag = if (data.isLockEnabled) "<green>Lock Enabled" else "<red>Lock Disabled"

			pane.addItem(
				guiButton(Material.IRON_DOOR) {
					toggleLockEnabled(playerClicker, data)
					tryOpenMenu(player, data)
				}.setName(MiniMessage.miniMessage().deserialize(lockDisplayTag)),
				3, 0
			)

			pane.addItem(
				guiButton(Material.NAME_TAG) {
					startRename(playerClicker, data)
					tryOpenMenu(player, data)
				}.setName(MiniMessage.miniMessage().deserialize("<gray>Starship Name")),
				8, 0
			)

			if (player.isTerritoryOwner()) {
				pane.addItem(
					guiButton(Material.RECOVERY_COMPASS) {
						takeOwnership(player, data)
					}.setName(text("Take ownership", RED, BOLD)),
					5, 0
				)
			}

			pane.setOnClick { e ->
				e.isCancelled = true
			}

			gui(1, getDisplayName(data).replace("<[^>]*>".toRegex(), "")).withPane(pane).show(player)
		}
	}

	private val lockMap = mutableMapOf<Oid<PlayerStarshipData>, Any>()

	private fun getLock(dataId: Oid<PlayerStarshipData>): Any = lockMap.getOrPut(dataId) { Any() }

	private fun tryReDetect(player: Player, data: PlayerStarshipData) {
		Tasks.async {
			synchronized(getLock(data._id)) {
				val state = try {
					StarshipDetection.detectNewState(data)
				} catch (e: StarshipDetection.DetectionFailedException) {
					player.serverErrorActionMessage("${e.message} Detection failed!")
					player.hint("Is it touching another structure?")
					return@async
				} catch (e: Exception) {
					e.printStackTrace()
					player.serverErrorActionMessage("An error occurred while detecting")
					return@async
				}

				player.rewardAchievement(Achievement.DETECT_SHIP)

				DeactivatedPlayerStarships.updateState(data, state)

				player.success("Re-detected! New size ${state.blockMap.size.toText()}")
			}
		}
	}

	private fun openPilotsMenu(player: Player, data: PlayerStarshipData) {
		MenuHelper.apply {
			val items = LinkedList<GuiItem>()
			items.add(
				guiButton(Material.BEACON) {
					player.closeInventory()
					player.beginConversation(
						Conversation(
							IonServer, player,
							object : StringPrompt() {
								override fun getPromptText(context: ConversationContext): String {
									return "Enter player name:"
								}

								override fun acceptInput(context: ConversationContext, input: String?): Prompt? {
									if (input != null) {
										Tasks.async {
											val id = SLPlayer.findIdByName(input)
											if (id == null) {
												player.userError("Player not found")
											} else {
												DeactivatedPlayerStarships.addPilot(data, id)
												data.pilots += id
												PlayerStarshipData.updateById(
													data._id,
													addToSet(PlayerStarshipData::pilots, id)
												)
												player.success("Added $input as a pilot to starship.")
											}
										}
									}
									return null
								}
							}
						)
					)
				}.setName("Add Pilot")
			)
			Tasks.async {
				for (pilot in data.pilots) {
					val name = SLPlayer.getName(pilot) ?: continue
					items.add(
						guiButton(skullItem(pilot.uuid, name)) {
							if (pilot != data.captain) {
								data.pilots -= pilot
								Tasks.async {
									PlayerStarshipData.updateById(data._id, pull(PlayerStarshipData::pilots, pilot))
								}
								player.closeInventory()
								player.success("Removed $name")
							}
						}
					)
				}
				Tasks.sync {
					player.openPaginatedMenu("Edit Pilots", items)
				}
			}
		}
	}

	private fun startRename(player: Player, data: PlayerStarshipData) {
		player.closeInventory()
		player.beginConversation(
			Conversation(
				IonServer, player,
				object : StringPrompt() {
					override fun getPromptText(context: ConversationContext): String {
						return "Enter new starship name:"
					}

					override fun acceptInput(context: ConversationContext, input: String?): Prompt? {
						if (input != null) {
							Tasks.async {
								val serialized = MiniMessage.miniMessage().deserialize(input)

								if (serialized.clickEvent() != null ||
									input.contains("<rainbow>") ||
									input.contains("<newline>") ||
									input.contains("<reset>") ||
									serialized.hoverEvent() != null ||
									serialized.insertion() != null ||
									serialized.hasDecoration(TextDecoration.OBFUSCATED) ||
									((serialized as? TextComponent)?.content()?.length ?: 0) >= 16
								) {
									player.userError("ERROR: Disallowed tags!")
									return@async
								}

								if (serialized.color() != null && !player.hasPermission("ion.starship.color")) {
									player.userError(
										"<COLOR> tags can only be used by $5+ patrons! Donate at\n" +
											"Donate at https://www.patreon.com/horizonsendmc/ to receive this perk."
									)
									return@async
								}

								if ((serialized.color() as? HSVLike) != null && serialized.color()!!.asHSV()
									.v() < 0.25
								) {
									player.userError(
										"Ship names can't be too dark to read!"
									)
									return@async
								}

								if (
									serialized.decorations().any { it.value == TextDecoration.State.TRUE } &&
									!player.hasPermission("ion.starship.italic")
								) {
									player.userError(
										"\\<italic>, \\<bold>, \\<strikethrough> and \\<underlined> tags can only be used by $10+ patrons!\n" +
											"Donate at https://www.patreon.com/horizonsendmc/ to receive this perk."
									)
									return@async
								}

								if (serialized.font() != null && !player.hasPermission("ion.starship.font")) {
									player.userError(
										"\\<font> tags can only be used by $15+ patrons! Donate at\n" +
											"Donate at https://www.patreon.com/horizonsendmc/ to receive this perk."
									)
									return@async
								}

								DeactivatedPlayerStarships.updateName(data, input)

								player.success("Changed starship name to $input")
							}
						}
						return null
					}
				}
			)
		)
	}

	private fun openTypeMenu(player: Player, data: PlayerStarshipData) {
		MenuHelper.apply {
			val items = StarshipType.getUnlockedTypes(player).map { type ->
				guiButton(type.menuItem) {
					// prevent from being piloted
					if (ActiveStarships[data._id] != null) {
						return@guiButton
					}

					DeactivatedPlayerStarships.updateType(data, type)

					playerClicker.closeInventory()
					player.success("Changed type to $type")
				}
			}
			player.openPaginatedMenu("Select Type", items)
		}
	}

	private fun toggleLockEnabled(player: Player, data: PlayerStarshipData) {
		val newValue = !data.isLockEnabled

		DeactivatedPlayerStarships.updateLockEnabled(data, newValue)

		if (newValue) {
			player.success("Enabled Lock")
		} else {
			player.success("Disabled Lock")
		}
	}

	private fun takeOwnership(player: Player, data: PlayerStarshipData) {
		PlayerStarshipData.updateById(data._id, setValue(PlayerStarshipData::captain, player.slPlayerId))
		PlayerStarshipData.updateById(data._id, setValue(PlayerStarshipData::pilots, mutableSetOf()))
	}

	fun Player.isTerritoryOwner(): Boolean {
		val territoryId = Regions.find(this.location)
			.filterIsInstance<RegionTerritory>()
			.firstOrNull() ?: return false
		val territory = Territory.findById(territoryId.id) ?: return false
		val settlementId = territory.settlement ?: Nation.findById(territory.nation?: return false)?.capital ?: return false
		val settlement = Settlement.findById(settlementId) ?: return false
		return settlement.leader.uuid == uniqueId
	}
}
