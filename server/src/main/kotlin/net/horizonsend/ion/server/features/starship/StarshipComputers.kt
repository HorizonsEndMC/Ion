package net.horizonsend.ion.server.features.starship

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
import net.horizonsend.ion.common.extensions.successActionMessage
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.common.extensions.userErrorActionMessage
import net.horizonsend.ion.common.utils.text.bracketed
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.gui.custom.starship.StarshipComputerMenu
import net.horizonsend.ion.server.features.nations.region.Regions
import net.horizonsend.ion.server.features.nations.region.types.RegionTerritory
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.starship.control.movement.PlayerStarshipControl.isHoldingController
import net.horizonsend.ion.server.features.starship.event.StarshipComputerOpenMenuEvent
import net.horizonsend.ion.server.miscellaneous.utils.PerPlayerCooldown
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.bukkitWorld
import net.horizonsend.ion.server.miscellaneous.utils.isPilot
import net.horizonsend.ion.server.miscellaneous.utils.slPlayerId
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.event.ClickEvent
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
import org.litote.kmongo.setValue
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
		if (data !is PlayerStarshipData) {
			if (player.hasPermission("ion.core.starship.override")) handleAIComputer(player, data)

			return
		}

		if (data.isPilot(player) || canTakeOwnership(player, data)) return openMenu(player, data)

		Tasks.async {
			val name: String? = SLPlayer.getName(data.captain)

			player.userError("You're not a pilot of this ship! The captain is $name")
		}
	}

	private fun openMenu(player: Player, data: PlayerStarshipData) {
		if (!StarshipComputerOpenMenuEvent(player).callEvent()) return
		StarshipComputerMenu(player, data).open()
	}

	private fun handleAIComputer(player: Player, data: StarshipData) {
		player.sendMessage(ofChildren(
			text("This is an AI starship computer, click here to remove it: "),
			bracketed(text("Here"))
				.clickEvent(ClickEvent.callback {
					DeactivatedPlayerStarships.destroyAsync(data) {
						player.successActionMessage("Destroyed starship computer")
					}
				})
				.hoverEvent(text("Remove Computer"))
		))
	}

	fun canTakeOwnership(player: Player, data: PlayerStarshipData): Boolean {
		return !data.isPilot(player)
			&& (player.hasPermission("ion.core.starship.override")
			|| isSettlementOwner(player, data)
			|| (isMemberOfTerritory(player, data) && hasPermission(player.slPlayerId, SettlementRole.Permission.TAKE_SHIP_OWNERSHIP))   // passing this implies the player is a member of the settlement
			|| (isNationMemberOfTerritory(player, data) && hasPermission(player.slPlayerId, NationRole.Permission.TAKE_SHIP_OWNERSHIP))) // passing this implies the player is part of the nation
	}

	fun takeOwnership(player: Player, data: PlayerStarshipData) {
		data.captain = player.slPlayerId
		PlayerStarshipData.updateById(data._id, setValue(PlayerStarshipData::captain, player.slPlayerId))
		data.pilots.clear()
		PlayerStarshipData.updateById(data._id, setValue(PlayerStarshipData::pilots, mutableSetOf()))
	}

	private fun getComputerTerritory(data: PlayerStarshipData): Territory? {
		val location = Vec3i(data.blockKey).toLocation(data.bukkitWorld())

		val territoryId = Regions.find(location)
			.filterIsInstance<RegionTerritory>()
			.firstOrNull() ?: return null

		return Territory.findById(territoryId.id)
	}

	private fun isSettlementOwner(player: Player, data: PlayerStarshipData): Boolean {
		val territory = getComputerTerritory(data) ?: return false
		val settlementId = territory.settlement ?: Nation.findById(territory.nation?: return false)?.capital ?: return false
		val settlement = Settlement.findById(settlementId) ?: return false

		return settlement.leader.uuid == player.uniqueId
	}

	private fun isMemberOfTerritory(player: Player, data: PlayerStarshipData): Boolean {
		val territory = getComputerTerritory(data) ?: return false
		val settlementId = territory.settlement ?: return false

		return isMemberOfSettlement(player.slPlayerId, settlementId)
	}

	fun hasPermission(player: SLPlayerId, permission: SettlementRole.Permission): Boolean {
		return SettlementRole.hasPermission(player, permission)
	}

	private fun isNationMemberOfTerritory(player: Player, data: PlayerStarshipData): Boolean {
		val territory = getComputerTerritory(data) ?: return false
		val nationId = territory.nation ?: return false

		return isMemberOfNation(player.slPlayerId, nationId)
	}

	fun hasPermission(player: SLPlayerId, permission: NationRole.Permission): Boolean {
		return NationRole.hasPermission(player, permission)
	}
}
