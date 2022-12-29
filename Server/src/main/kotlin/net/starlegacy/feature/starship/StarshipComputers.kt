package net.starlegacy.feature.starship

import com.github.stefvanschie.inventoryframework.gui.GuiItem
import net.horizonsend.ion.server.IonServer.Companion.Ion
import net.horizonsend.ion.server.legacy.feedback.FeedbackType.SERVER_ERROR
import net.horizonsend.ion.server.legacy.feedback.FeedbackType.SUCCESS
import net.horizonsend.ion.server.legacy.feedback.FeedbackType.USER_ERROR
import net.horizonsend.ion.server.legacy.feedback.sendFeedbackActionMessage
import net.horizonsend.ion.server.legacy.feedback.sendFeedbackMessage
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.util.HSVLike
import net.starlegacy.SLComponent
import net.starlegacy.database.Oid
import net.starlegacy.database.schema.misc.SLPlayer
import net.starlegacy.database.schema.starships.PlayerStarshipData
import net.starlegacy.database.uuid
import net.starlegacy.feature.nations.gui.playerClicker
import net.starlegacy.feature.nations.gui.skullItem
import net.starlegacy.feature.starship.PilotedStarships.getDisplayName
import net.starlegacy.feature.starship.active.ActiveStarships
import net.starlegacy.feature.starship.control.StarshipControl
import net.starlegacy.feature.starship.event.StarshipComputerOpenMenuEvent
import net.starlegacy.feature.starship.event.StarshipDetectEvent
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
import java.util.LinkedList

object StarshipComputers : SLComponent() {

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
			player.sendFeedbackMessage(USER_ERROR, "Not holding starship controller, ignoring computer click")
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
			player.sendFeedbackActionMessage(SUCCESS, "Destroyed starship computer")
		}
	}

	operator fun get(world: World, x: Int, y: Int, z: Int): PlayerStarshipData? {
		return ActiveStarships.getByComputerLocation(world, x, y, z) ?: DeactivatedPlayerStarships[world, x, y, z]
	}

	private fun createComputer(player: Player, block: Block) {
		DeactivatedPlayerStarships.createAsync(block.world, block.x, block.y, block.z, player.uniqueId) {
			player.sendFeedbackActionMessage(
				SUCCESS,
				"Registered starship computer! Left click again to open the menu."
			)
		}
	}

	private fun tryOpenMenu(player: Player, data: PlayerStarshipData) {
		if (!data.isPilot(player) && !player.hasPermission("ion.core.starship.override")) {
			Tasks.async {
				val name: String? = SLPlayer.getName(data.captain)
				if (name != null) (player.sendFeedbackActionMessage(
					USER_ERROR,
					"You're not a pilot of this ship! The captain is {0}",
					name
				))
			}
			return
		}

		if (!StarshipComputerOpenMenuEvent(player).callEvent()) {
			return
		}

		MenuHelper.apply {
			val pane = staticPane(0, 0, 9, 1)

			pane.addItem(guiButton(StarshipControl.CONTROLLER_TYPE) {
				tryReDetect(playerClicker, data)
			}.setName(MiniMessage.miniMessage().deserialize("<dark_purple>Re-detect")), 0, 0)

			pane.addItem(guiButton(Material.PLAYER_HEAD) {
				openPilotsMenu(playerClicker, data)
			}.setName(MiniMessage.miniMessage().deserialize("<gold>Pilots")), 1, 0)

			pane.addItem(guiButton(Material.GHAST_TEAR) {
				openTypeMenu(playerClicker, data)
			}.setName(MiniMessage.miniMessage().deserialize("<white>Type (${data.starshipType})")), 2, 0)

			val lockDisplayTag = if (data.isLockEnabled) "<green>Lock Enabled" else "<red>Lock Disabled"

			pane.addItem(guiButton(Material.IRON_DOOR) {
				toggleLockEnabled(playerClicker, data)
				tryOpenMenu(player, data)
			}.setName(MiniMessage.miniMessage().deserialize(lockDisplayTag)), 3, 0)

			pane.addItem(guiButton(Material.NAME_TAG) {
				startRename(playerClicker, data)
				tryOpenMenu(player, data)
			}.setName(MiniMessage.miniMessage().deserialize("<gray>Starship Name")), 8, 0)

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
					player.sendFeedbackActionMessage(SERVER_ERROR, "{0} Detection failed!", e.message!!)
					return@async
				} catch (e: Exception) {
					e.printStackTrace()
					player.sendFeedbackActionMessage(SERVER_ERROR, "An error occurred while detecting")
					return@async
				}

				if (!Tasks.getSyncBlocking { StarshipDetectEvent(player, player.world).callEvent() }) {
					return@async
				}

				DeactivatedPlayerStarships.updateState(data, state)

				player.sendFeedbackActionMessage(SUCCESS, "Re-detected! New size {0}", state.blockMap.size.toText())
			}
		}
	}

	private fun openPilotsMenu(player: Player, data: PlayerStarshipData) {
		MenuHelper.apply {
			val items = LinkedList<GuiItem>()
			items.add(guiButton(Material.BEACON) {
				player.closeInventory()
				player.beginConversation(
					Conversation(Ion, player, object : StringPrompt() {
						override fun getPromptText(context: ConversationContext): String {
							return "Enter player name:"
						}

						override fun acceptInput(context: ConversationContext, input: String?): Prompt? {
							if (input != null) Tasks.async {
								val id = SLPlayer.findIdByName(input)
								if (id == null) {
									player.sendFeedbackMessage(USER_ERROR, "Player not found")
								} else {
									DeactivatedPlayerStarships.addPilot(data, id)
									data.pilots += id
									PlayerStarshipData.updateById(data._id, addToSet(PlayerStarshipData::pilots, id))
									player.sendFeedbackMessage(SUCCESS, "Added {0} as a pilot to starship.", input)
								}
							}
							return null
						}
					})
				)
			}.setName("Add Pilot"))
			Tasks.async {
				for (pilot in data.pilots) {
					val name = SLPlayer.getName(pilot) ?: continue
					items.add(guiButton(skullItem(pilot.uuid, name)) {
						if (pilot != data.captain) {
							data.pilots -= pilot
							Tasks.async {
								PlayerStarshipData.updateById(data._id, pull(PlayerStarshipData::pilots, pilot))
							}
							player.closeInventory()
							player.sendFeedbackMessage(SUCCESS, "Removed {0}", name)
						}
					})
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
			Conversation(Ion, player, object : StringPrompt() {
				override fun getPromptText(context: ConversationContext): String {
					return "Enter new starship name:"
				}

				override fun acceptInput(context: ConversationContext, input: String?): Prompt? {
					if (input != null) Tasks.async {
						val serialized = MiniMessage.miniMessage().deserialize(input)

						if (serialized.clickEvent() != null
							|| input.contains("<rainbow>")
							|| input.contains("<newline>")
							|| serialized.hoverEvent() != null
							|| serialized.insertion() != null
							|| serialized.hasDecoration(TextDecoration.OBFUSCATED)
							|| ((serialized as? TextComponent)?.content()?.length ?: 0) >= 16
						) {
							player.sendFeedbackMessage(USER_ERROR, "ERROR: Disallowed tags!")
							return@async
						}

						if (serialized.color() != null && !player.hasPermission("ion.starship.color")) {
							player.sendFeedbackMessage(
								USER_ERROR,
								"<COLOR> tags can only be used by $5+ patrons! Donate at\n" +
									"Donate at https://www.patreon.com/horizonsendmc/ to receive this perk."
							)
							return@async
						}

						if ((serialized.color() as? HSVLike) != null && serialized.color()!!.asHSV().v() < 0.25) {
							player.sendFeedbackMessage(
								USER_ERROR,
								"Ship names can't be too dark to read!"
							)
							return@async
						}

						if (
							serialized.decorations().any { it.value == TextDecoration.State.TRUE }
							&& !player.hasPermission("ion.starship.italic")
						) {
							player.sendFeedbackMessage(
								USER_ERROR,
								"\\<italic>, \\<bold>, \\<strikethrough> and \\<underlined> tags can only be used by $10+ patrons!\n" +
									"Donate at https://www.patreon.com/horizonsendmc/ to receive this perk."
							)
							return@async
						}

						if (serialized.font() != null && !player.hasPermission("ion.starship.font")) {
							player.sendFeedbackMessage(
								USER_ERROR,
								"\\<font> tags can only be used by $15+ patrons! Donate at\n" +
									"Donate at https://www.patreon.com/horizonsendmc/ to receive this perk."
							)
							return@async
						}

						DeactivatedPlayerStarships.updateName(data, input)

						player.sendFeedbackMessage(SUCCESS, "Changed starship name to $input.")
					}
					return null
				}
			})
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
					player.sendFeedbackMessage(SUCCESS, "Changed type to {0}", type)
				}
			}
			player.openPaginatedMenu("Select Type", items)
		}
	}

	private fun toggleLockEnabled(player: Player, data: PlayerStarshipData) {
		val newValue = !data.isLockEnabled

		DeactivatedPlayerStarships.updateLockEnabled(data, newValue)

		if (newValue) {
			player.sendFeedbackMessage(SUCCESS, "Enabled Lock")
		} else {
			player.sendFeedbackMessage(SUCCESS, "Disabled Lock")
		}
	}
}