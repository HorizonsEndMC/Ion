package net.horizonsend.ion.server.listener.misc

import io.papermc.paper.event.player.PlayerItemFrameChangeEvent
import net.horizonsend.ion.common.database.schema.starships.PlayerStarshipData
import net.horizonsend.ion.common.extensions.informationAction
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.common.utils.lpHasPermission
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.features.nations.region.Regions
import net.horizonsend.ion.server.features.nations.region.types.RegionSettlementZone
import net.horizonsend.ion.server.features.nations.region.types.RegionTerritory
import net.horizonsend.ion.server.features.npcs.traits.CombatNPCTrait
import net.horizonsend.ion.server.features.player.CombatNPCs
import net.horizonsend.ion.server.features.player.CombatTimer
import net.horizonsend.ion.server.features.player.CombatTimer.REASON_PVP_GROUND_COMBAT
import net.horizonsend.ion.server.features.player.CombatTimer.evaluatePvp
import net.horizonsend.ion.server.features.space.Space
import net.horizonsend.ion.server.features.starship.DeactivatedPlayerStarships
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.world.IonWorld.Companion.ion
import net.horizonsend.ion.server.features.world.WorldFlag
import net.horizonsend.ion.server.listener.SLEventListener
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.action
import net.horizonsend.ion.server.miscellaneous.utils.colorize
import net.horizonsend.ion.server.miscellaneous.utils.component1
import net.horizonsend.ion.server.miscellaneous.utils.component2
import net.horizonsend.ion.server.miscellaneous.utils.component3
import net.horizonsend.ion.server.miscellaneous.utils.component4
import net.horizonsend.ion.server.miscellaneous.utils.distance
import net.horizonsend.ion.server.miscellaneous.utils.isPilot
import net.horizonsend.ion.server.miscellaneous.utils.msg
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.block.Block
import org.bukkit.block.Jukebox
import org.bukkit.block.data.Openable
import org.bukkit.block.data.Powerable
import org.bukkit.block.data.type.Door
import org.bukkit.block.data.type.Switch
import org.bukkit.block.data.type.TrapDoor
import org.bukkit.entity.Animals
import org.bukkit.entity.EntityType
import org.bukkit.entity.Monster
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.EventHandler
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockExplodeEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.entity.EntitySpawnEvent
import org.bukkit.event.player.PlayerBucketEmptyEvent
import org.bukkit.event.player.PlayerBucketFillEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.InventoryHolder

object ProtectionListener : SLEventListener() {
	/** Handle interact events as block edits **/
	@EventHandler
	fun onClickBlock(event: PlayerInteractEvent) {
		val block: Block = event.clickedBlock ?: return
		// Chests, button doors
		if (event.action != Action.RIGHT_CLICK_BLOCK) return

		if (shouldNotBeChecked(event.player, block)) return

		onBlockEdit(event, block.location, event.player)
	}

	/** Allows exceptions to the onBlockEdit check **/
	private fun shouldNotBeChecked(player: Player, clickedBlock: Block): Boolean {
		// It is much easier to decide what should be the exception than to make exceptions
		// If something ends up getting checked that shouldn't
		// (e.g. clicking the glass in your cockpit), it could break firing weapons.

		val zone = RegionSettlementZone.getRegionSettlementZone(clickedBlock.location)

		// if there is a settlement zone and the clicked block is allowed on its list of interactable blocks,
		// do not perform protection checks
		if (zone != null && isInteractable(clickedBlock) &&
			zone.interactableBlocks.contains(clickedBlock.blockData.material.name)) {
			return true
		}

		if (isPlanetOrbitDenied(player, clickedBlock.location, true)) return true

		// Manually check these
		if (clickedBlock.blockData is Switch) return false
		if (clickedBlock.blockData is TrapDoor) return false
		if (clickedBlock.blockData is Door) return false

		return clickedBlock.state !is InventoryHolder
	}

	private fun isInteractable(block: Block): Boolean {
		// Expand list of interactable blocks as needed
		return (block.blockData !is Jukebox &&
			(block.state is InventoryHolder ||
			block.blockData is Openable ||
			block.blockData is Powerable))
	}

	@EventHandler
	fun onItemFrameChange(event: PlayerItemFrameChangeEvent) {
		if (denyBlockAccess(event.player, event.itemFrame.location, event)) {
			event.isCancelled = true
		}
	}

	@EventHandler
	fun onAnimalHurt(event: EntityDamageByEntityEvent) {
		val damager = event.damager as? Player ?: return
		if (event.entity !is Animals) return

		if (denyBlockAccess(damager, event.entity.location, event)) {
			event.isCancelled = true
		}
	}

	@EventHandler
	fun onBlockPlace(event: BlockPlaceEvent) = onBlockEdit(event, event.block.location, event.player)

	@EventHandler
	fun onBlockBreak(event: BlockBreakEvent) = onBlockEdit(event, event.block.location, event.player)

	private fun onBlockEdit(event: Cancellable, location: Location, player: Player) {
		if (denyBlockAccess(player, location, event)) {
			event.isCancelled = true
		}
	}

	@EventHandler
	fun onBucketFill(event: PlayerBucketFillEvent) = onBlockEdit(event, event.block.location, event.player)

	@EventHandler
	fun onBucketEmpty(event: PlayerBucketEmptyEvent) = onBlockEdit(event, event.block.location, event.player)

	/** Called on block break etc. GriefPrevention check should be done first.
	 *  Loops through protected regions at location, checks each one for access message
	 *  @return true if the event should be cancelled, false if it should stay the same. */
	fun denyBlockAccess(player: Player, location: Location, event: Cancellable?): Boolean {
		var denied = false

		if (isRegionDenied(player, location)) denied = true

		val (world, x, y, z) = location
		val shipContaining = DeactivatedPlayerStarships.getContaining(world, x.toInt(), y.toInt(), z.toInt())

		// Need to also check for null
		if (shipContaining !is PlayerStarshipData?) return true

		if (shipContaining?.isPilot(player) == true && event !is BlockPlaceEvent) denied = false

		val (x1, y1, z1) = Vec3i(location)
		if (ActiveStarships.findByPilot(player)?.contains(x1, y1, z1) == true) denied = false

		if (isLockedShipDenied(player, location)) return true

		if (isPlanetOrbitDenied(player, location, false)) return true

		return denied
	}

	fun isRegionDenied(player: Player, location: Location): Boolean {
		var denied = false

		for (region in Regions.find(location).sortedByDescending { it.priority }) {
			if (!region.isCached(player)) {
				Tasks.async {
					if (!player.isOnline) {
						return@async
					}

					region.cacheAccess(player)
				}

				player msg "&eCaching... &o(you probably shouldn't see this, it's probably a bug, but it's harmless)"
				denied = true
				continue
			}

			// break because if there are overlapping regions, that means that this one had the highest priority
			// and they had access
			val message = region.getInaccessMessage(player) ?: break

			// Only cancel if they're not in dutymode, otherwise tell them they are bypassing
			if (player.hasPermission("dutymode")) {
				player action "&eBypassed ${region.javaClass.simpleName.removePrefix("Region")} protection in dutymode"
				break // only show one message, they will bypass anything else anyway
			} else {
				if (!denied && PlayerCache[player].protectionMessagesEnabled) { // only if no other region has already reached this, in order to maintain the priority of region messages
					// Send them the detailed message
					player.sendTitle("", "&e$message".colorize(), 5, 20, 5)
					player.sendActionBar("&cThis place is claimed! Find an unclaimed territory with the map (https://survival.horizonsend.net)".colorize())
					denied = true
					region.onFailedToAccess(player)
				}
			}
		}

		return denied
	}

	fun isPlanetOrbitDenied(player: Player, location: Location, silent: Boolean): Boolean {
		val (world, x, y, z) = location
		val padding = 500
		var inOwnStation = false

		for (planet in Space.getOrbitingPlanets().filter { it.spaceWorld == world }) {
			val minDistance = planet.orbitDistance - padding
			val maxDistance = planet.orbitDistance + padding
			val distance = distance(x.toInt(), y.toInt(), z.toInt(), planet.sun.location.x, y.toInt(), planet.sun.location.z).toInt()

			// Within planet orbit
			if (distance in minDistance..maxDistance) {
				// Check if they are in a station
				for (region in Regions.find(location).sortedByDescending { it.priority }) {
					// They have build permissions in this region
					if (region.isCached(player)) {
						Tasks.async {
							if (!player.isOnline) {
								return@async
							}

							region.cacheAccess(player)
						}

						inOwnStation = true
						continue
					}

					if (player.hasPermission("dutymode")) {
						player.informationAction("Bypassed planet orbit protection in dutymode")
						return false
					}
				}

				if (!inOwnStation) {
					if (!silent) player.userError("You cannot build in the way of ${planet.name}'s orbit")
					return true
				}
			}
		}

		return false
	}

	private fun isLockedShipDenied(player: Player, location: Location): Boolean {
		if (location.world.ion.hasFlag(WorldFlag.NO_SHIP_LOCKS)) return false
		if (player.uniqueId.lpHasPermission("ion.bypass-locks")) return false

		val world = location.world
		val x = location.blockX
		val y = location.blockY
		val z = location.blockZ
		val data = DeactivatedPlayerStarships.getLockedContaining(world, x, y, z)
			?: return false

		if (!data.isLockActive()) return false

		if (data !is PlayerStarshipData) return false

		return !data.isPilot(player)
	}

	@EventHandler
	fun onBlockExplode(event: BlockExplodeEvent) {
		onExplode(event, event.block.location, event.blockList())
	}

	@EventHandler
	fun onEntityExplode(event: EntityExplodeEvent) {
		onExplode(event, event.entity.location, event.blockList())
	}

	private fun onExplode(cancellable: Cancellable, location: Location, blocks: List<Block>) {
		if (!isProtectedCity(location)) return

		cancellable.isCancelled = true

		blocks.forEach { block ->
			if (Math.random() < 0.25) {
				val heart = if (Math.random() > 0.5) Particle.HEART else Particle.ANGRY_VILLAGER
				val particleLoc = block.location.add(Math.random(), 1 + Math.random(), Math.random())

				block.world.spawnParticle(heart, particleLoc, 1)
			}
		}
	}

	fun isProtectedCity(location: Location): Boolean = Regions
		.find(location)
		.any { it is RegionTerritory && it.isProtected }

	@EventHandler
	fun onExplosionDamage(event: EntityDamageEvent) {
		if (event.entityType != EntityType.PLAYER && event.entityType != EntityType.ARMOR_STAND) {
			return
		}

		if (event.cause != EntityDamageEvent.DamageCause.ENTITY_EXPLOSION && event.cause != EntityDamageEvent.DamageCause.BLOCK_EXPLOSION) {
			return
		}

		if (!isProtectedCity(event.entity.location)) {
			return
		}

		event.isCancelled = true
	}

	@EventHandler
	fun onPVP(event: EntityDamageByEntityEvent) {
		val attacker = event.damager
		val defender = event.entity

		if (attacker !is Player || defender !is Player) {
			return
		}

		// Do not perform the protected city check if the NPC was in combat
		if (defender.hasMetadata("NPC")) {
			val npc = CombatNPCs.manager.getNPC(defender) ?: return
			val trait = npc.getTraitNullable(CombatNPCTrait::class.java)
			if (trait != null && trait.wasInCombat) return
		}

		// Prevent combat if defender is not combat tagged and is in a protected city, or attacker is not combat tagged and is in a protected city
		if (((!CombatTimer.isPvpCombatTagged(defender) && isProtectedCity(defender.location)) ||
					(!CombatTimer.isPvpCombatTagged(attacker) && isProtectedCity(attacker.location))
			)) {
			event.isCancelled = true
		} else  {
			// If combat occurs on ground, apply combat tag
			evaluatePvp(attacker, defender, REASON_PVP_GROUND_COMBAT)
		}
	}

	@EventHandler
	fun onSpawn(event: EntitySpawnEvent) {
		if (event.entity !is Monster &&
			event.entityType != EntityType.GHAST &&
			event.entityType != EntityType.SLIME &&
			event.entityType != EntityType.MAGMA_CUBE
		) {
			return
		}

		if (Regions.find(event.location).any { it is RegionTerritory && it.npcOwner != null }) {
			event.isCancelled = true
		}
	}
}
