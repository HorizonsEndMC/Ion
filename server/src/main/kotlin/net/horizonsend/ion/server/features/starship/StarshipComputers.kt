package net.horizonsend.ion.server.features.starship

import com.github.stefvanschie.inventoryframework.gui.GuiItem
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.database.schema.misc.SLPlayer.Companion.isMemberOfNation
import net.horizonsend.ion.common.database.schema.misc.SLPlayer.Companion.isMemberOfSettlement
import net.horizonsend.ion.common.database.schema.misc.SLPlayerId
import net.horizonsend.ion.common.database.schema.nations.Nation
import net.horizonsend.ion.common.database.schema.nations.NationRole
import net.horizonsend.ion.common.database.schema.nations.Settlement
import net.horizonsend.ion.common.database.schema.nations.SettlementRole
import net.horizonsend.ion.common.database.schema.nations.Territory
import net.horizonsend.ion.common.database.schema.starships.PlayerStarshipData
import net.horizonsend.ion.common.database.schema.starships.StarshipData
import net.horizonsend.ion.common.database.uuid
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.successActionMessage
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.common.extensions.userErrorActionMessage
import net.horizonsend.ion.common.utils.text.toComponent
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.gui.custom.starship.StarshipComputerMenu
import net.horizonsend.ion.server.features.nations.gui.anvilInput
import net.horizonsend.ion.server.features.nations.gui.playerClicker
import net.horizonsend.ion.server.features.nations.gui.skullItem
import net.horizonsend.ion.server.features.nations.region.Regions
import net.horizonsend.ion.server.features.nations.region.types.RegionTerritory
import net.horizonsend.ion.server.features.starship.PilotedStarships.getDisplayName
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.starship.control.movement.PlayerStarshipControl.isHoldingController
import net.horizonsend.ion.server.features.starship.event.StarshipComputerOpenMenuEvent
import net.horizonsend.ion.server.miscellaneous.utils.MenuHelper
import net.horizonsend.ion.server.miscellaneous.utils.PerPlayerCooldown
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.isPilot
import net.horizonsend.ion.server.miscellaneous.utils.slPlayerId
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Block
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
import java.util.LinkedList
import java.util.concurrent.TimeUnit

object StarshipComputers : IonServerComponent() {
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

		if (!isHoldingController(player)) {
			player.userError("Not holding starship controller, ignoring computer click")
			return
		}

		val starship = PilotedStarships[player]
		if (starship != null && starship.isDirectControlEnabled) {
			player.userErrorActionMessage("Cannot interact with starship computer while in Direct Control!")
			return
		}

		event.isCancelled = true
		val data: StarshipData? = StarshipComputers[event.player.world, block.x, block.y, block.z]

		when (event.action) {
			Action.LEFT_CLICK_BLOCK -> handleLeftClick(data, player, block)
			Action.RIGHT_CLICK_BLOCK -> handleRightClick(data, player)
			else -> return
		}
	}

	private fun handleLeftClick(data: StarshipData?, player: Player, block: Block) {
		if (data == null) {
			createComputer(player, block)
			return
		}

		tryOpenMenu(player, data)
	}

	private val pilotCooldown = PerPlayerCooldown.callbackCooldown(150, TimeUnit.MILLISECONDS) {
		Bukkit.getPlayer(it)?.userError("You're doing that too often!")
	}

	private fun handleRightClick(data: StarshipData?, player: Player) {
		if (data == null) {
			return
		}

		pilotCooldown.tryExec(player) {
			PilotedStarships.tryPilot(player, data)
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	fun onBlockBreak(event: BlockBreakEvent) {
		val block = event.block

		// get a DEACTIVATED computer. Using StarshipComputers#get would include activated ones
		val computer: StarshipData = DeactivatedPlayerStarships[block.world, block.x, block.y, block.z] ?: return
		val player = event.player

		DeactivatedPlayerStarships.destroyAsync(computer) {
			player.successActionMessage("Destroyed starship computer")
		}
	}

	operator fun get(world: World, x: Int, y: Int, z: Int): StarshipData? {
		return ActiveStarships.getByComputerLocation(world, x, y, z) ?: DeactivatedPlayerStarships[world, x, y, z]
	}

	private fun createComputer(player: Player, block: Block) {
//		if (isRegionDenied(player, player.location)) return player.userError("You can only detect computers in territories you can access.")

		DeactivatedPlayerStarships.createPlayerShipAsync(block.world, block.x, block.y, block.z, player.uniqueId) {
			player.successActionMessage("Registered starship computer!")
			tryOpenMenu(player, it)
		}
	}

	private fun tryOpenMenu(player: Player, data: StarshipData) {
		if (
			data is PlayerStarshipData
			&& !data.isPilot(player)
			&& !player.hasPermission("ion.core.starship.override")
			&& !player.isTerritoryOwner()
			|| (!player.isMemberOfTerritory() || // passing this implies the player is a member of the settlement
				!hasPermission(player.slPlayerId, SettlementRole.Permission.TAKE_SHIP_OWNERSHIP))
			&& (!player.isNationMemberOfTerritory() || // passing this implies the player is part of the nation
				!hasPermission(player.slPlayerId, NationRole.Permission.TAKE_SHIP_OWNERSHIP))
		) {
			Tasks.async {
				val name: String? = SLPlayer.getName(data.captain)

				player.userError("You're not a pilot of this ship! The captain is $name")
			}

			return
		}

		if (data !is PlayerStarshipData && !player.hasPermission("ion.core.starship.override")) return
		if (!StarshipComputerOpenMenuEvent(player).callEvent()) return

		StarshipComputerMenu(player, data).open()

		return
		@Suppress("UNREACHABLE_CODE")
		MenuHelper.apply {
			val pane = staticPane(0, 0, 9, 1)

			pane.addItem(
				guiButton(Material.GHAST_TEAR) {
					openTypeMenu(playerClicker, data)
				}.setName(MiniMessage.miniMessage().deserialize("<white>Type (${data.starshipType})")),
				2, 0
			)

			pane.addItem(
				guiButton(Material.NAME_TAG) {
					startRename(playerClicker, data)
				}.setName(MiniMessage.miniMessage().deserialize("<gray>Starship Name")),
				8, 0
			)

			pane.setOnClick { e ->
				e.isCancelled = true
			}

			gui(1, getDisplayName(data)).withPane(pane).show(player)
		}
	}

	private fun openPilotsMenu(player: Player, data: PlayerStarshipData) {
		MenuHelper.apply {
			val items = LinkedList<GuiItem>()

			items.add(guiButton(Material.BEACON) {
				player.closeInventory()

				player.anvilInput("Enter player name:".toComponent()) { player, input ->
					Tasks.async {
						val id = SLPlayer.findIdByName(input) ?: return@async player.userError("Player not found")

						DeactivatedPlayerStarships.addPilot(data, id)
						data.pilots += id

						PlayerStarshipData.updateById(
							data._id,
							addToSet(PlayerStarshipData::pilots, id)
						)

						player.success("Added $input as a pilot to starship.")

						Tasks.sync {
							player.closeInventory()
						}
					}

					null
				}
			}.setName("Add Pilot"))

			Tasks.async {
				for (pilot in data.pilots) {
					val name = SLPlayer.getName(pilot) ?: continue

					items.add(guiButton(skullItem(pilot.uuid, name)) {
						if (pilot != data.captain) {
							data.pilots -= pilot

							Tasks.async { PlayerStarshipData.updateById(data._id, pull(PlayerStarshipData::pilots, pilot)) }

							player.closeInventory()
							player.success("Removed $name")
						}
					})
				}

				Tasks.sync { player.openPaginatedMenu("Edit Pilots", items) }
			}
		}
	}

	private fun startRename(player: Player, data: StarshipData) {
		player.closeInventory()
		player.anvilInput("Enter new ship name:".toComponent()) { r, input ->
			Tasks.async {
				val serialized = MiniMessage.miniMessage().deserialize(input)


			}
			null
		}
	}

	private fun openTypeMenu(player: Player, data: StarshipData) {
		MenuHelper.apply {
			val items = StarshipType.getUnlockedTypes(player).map { type ->
				guiButton(type.menuItem) {
					// prevent from being piloted
					if (ActiveStarships[data._id] != null) {
						return@guiButton
					}

					DeactivatedPlayerStarships.updateType(data, type)

					playerClicker.closeInventory()
					tryOpenMenu(player, data)
					player.success("Changed type to $type")
				}
			}
			player.openPaginatedMenu("Select Type", items)
		}
	}

	private fun toggleLockEnabled(player: Player, data: StarshipData) {
		val newValue = !data.isLockEnabled

		DeactivatedPlayerStarships.updateLockEnabled(data, newValue)

		if (newValue) {
			player.success("Enabled Lock")
		} else {
			player.success("Disabled Lock")
		}

		tryOpenMenu(player, data)
		player.updateInventory()
	}

	private fun takeOwnership(player: Player, data: PlayerStarshipData) {
		PlayerStarshipData.updateById(data._id, setValue(PlayerStarshipData::captain, player.slPlayerId))
		PlayerStarshipData.updateById(data._id, setValue(PlayerStarshipData::pilots, mutableSetOf()))
	}

	private fun Player.isTerritoryOwner(): Boolean {
		val territoryId = Regions.find(this.location)
			.filterIsInstance<RegionTerritory>()
			.firstOrNull() ?: return false

		val territory = Territory.findById(territoryId.id) ?: return false
		val settlementId = territory.settlement ?: Nation.findById(territory.nation?: return false)?.capital ?: return false
		val settlement = Settlement.findById(settlementId) ?: return false

		return settlement.leader.uuid == uniqueId
	}

	fun Player.isMemberOfTerritory(): Boolean {
		val territoryId = Regions.find(this.location)
			.filterIsInstance<RegionTerritory>()
			.firstOrNull() ?: return false
		val territory = Territory.findById(territoryId.id) ?: return false
		val settlementId = territory.settlement ?: return false
		return isMemberOfSettlement(this.slPlayerId, settlementId)
	}

	fun hasPermission(player: SLPlayerId, permission: SettlementRole.Permission): Boolean {
		return SettlementRole.hasPermission(player, permission)
	}

	fun Player.isNationMemberOfTerritory(): Boolean {
		val territoryId = Regions.find(this.location)
			.filterIsInstance<RegionTerritory>()
			.firstOrNull() ?: return false
		val territory = Territory.findById(territoryId.id) ?: return false
		val nationId = territory.nation ?: return false
		return isMemberOfNation(this.slPlayerId, nationId)
	}

	fun hasPermission(player: SLPlayerId, permission: NationRole.Permission): Boolean {
		return NationRole.hasPermission(player, permission)
	}
}
