package net.starlegacy.feature.starship

import com.github.stefvanschie.inventoryframework.GuiItem
import java.util.LinkedList
import net.starlegacy.PLUGIN
import net.starlegacy.SLComponent
import net.starlegacy.database.Oid
import net.starlegacy.database.schema.misc.SLPlayer
import net.starlegacy.database.schema.starships.PlayerStarshipData
import net.starlegacy.database.uuid
import net.starlegacy.feature.nations.gui.playerClicker
import net.starlegacy.feature.nations.gui.skullItem
import net.starlegacy.feature.starship.active.ActiveStarships
import net.starlegacy.feature.starship.control.StarshipControl
import net.starlegacy.feature.starship.event.StarshipComputerOpenMenuEvent
import net.starlegacy.feature.starship.event.StarshipDetectEvent
import net.starlegacy.util.MenuHelper
import net.starlegacy.util.SLTextStyle
import net.starlegacy.util.Tasks
import net.starlegacy.util.action
import net.starlegacy.util.actionAndMsg
import net.starlegacy.util.colorize
import net.starlegacy.util.msg
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
			player action "&eNot holding starship controller, ignoring computer click"
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
			player actionAndMsg "&cDestroyed starship computer"
		}
	}

	operator fun get(world: World, x: Int, y: Int, z: Int): PlayerStarshipData? {
		return ActiveStarships.getByComputerLocation(world, x, y, z) ?: DeactivatedPlayerStarships[world, x, y, z]
	}

	private fun createComputer(player: Player, block: Block) {
		DeactivatedPlayerStarships.createAsync(block.world, block.x, block.y, block.z, player.uniqueId) {
			player actionAndMsg "&7Registered starship computer! Left click again to open the menu."
		}
	}

	private fun tryOpenMenu(player: Player, data: PlayerStarshipData) {
		if (!data.isPilot(player)) {
			Tasks.async {
				val name: String? = SLPlayer.getName(data.captain)
				player actionAndMsg "&cYou're not a pilot of this ship! The captain is $name"
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
			}.setName("&5Re-detect".colorize()), 0, 0)

			pane.addItem(guiButton(Material.PLAYER_HEAD) {
				openPilotsMenu(playerClicker, data)
			}.setName("&6Pilots".colorize()), 1, 0)

			pane.addItem(guiButton(Material.GHAST_TEAR) {
				openTypeMenu(playerClicker, data)
			}.setName("&fType (${data.type})".colorize()), 2, 0)

			val lockDisplayTag = if (data.isLockEnabled) "&aLock Enabled" else "&cLock Disabled"

			pane.addItem(guiButton(Material.IRON_DOOR) {
				toggleLockEnabled(playerClicker, data)
				tryOpenMenu(player, data)
			}.setName(lockDisplayTag.colorize()), 3, 0)

			pane.setOnClick { e ->
				e.isCancelled = true
			}

			gui(1, "&5${SLTextStyle.OBFUSCATED}${data._id}".colorize()).withPane(pane).show(player)
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
					player actionAndMsg "&c" + (e.message ?: "Detection failed!")
					return@async
				} catch (e: Exception) {
					e.printStackTrace()
					player actionAndMsg "&cAn error occurred while detecting"
					return@async
				}

				if (!Tasks.getSyncBlocking { StarshipDetectEvent(player, player.world).callEvent() }) {
					return@async
				}

				DeactivatedPlayerStarships.updateState(data, state)

				player actionAndMsg "&aRe-detected! New size (block count): &e${state.blockMap.keys.size.toText()}"
			}
		}
	}

	private fun openPilotsMenu(player: Player, data: PlayerStarshipData) {
		MenuHelper.apply {
			val items = LinkedList<GuiItem>()
			items.add(guiButton(Material.BEACON) {
				player.closeInventory()
				player.beginConversation(
					Conversation(PLUGIN, player, object : StringPrompt() {
						override fun getPromptText(context: ConversationContext): String {
							return "Enter player name:"
						}

						override fun acceptInput(context: ConversationContext, input: String?): Prompt? {
							if (input != null) Tasks.async {
								val id = SLPlayer.findIdByName(input)
								if (id == null) {
									player msg "&cPlayer not found"
								} else {
									DeactivatedPlayerStarships.addPilot(data, id)
									data.pilots += id
									PlayerStarshipData.updateById(data._id, addToSet(PlayerStarshipData::pilots, id))
									player msg "&7Added $input as a pilot to the starship."
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
							player msg "&7Removed $name"
						}
					})
				}
				Tasks.sync {
					player.openPaginatedMenu("Edit Pilots", items)
				}
			}
		}
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
					player msg "&7Changed type to $type"
				}
			}
			player.openPaginatedMenu("Select Type", items)
		}
	}

	private fun toggleLockEnabled(player: Player, data: PlayerStarshipData) {
		val newValue = !data.isLockEnabled

		DeactivatedPlayerStarships.updateLockEnabled(data, newValue)

		if (newValue) {
			player msg "&7Enabled lock"
		} else {
			player msg "&7Disabled lock"
		}
	}
}
