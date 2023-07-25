package net.starlegacy.listener.misc

import io.papermc.paper.event.player.PlayerItemFrameChangeEvent
import net.horizonsend.ion.server.miscellaneous.utils.isPilot
import net.starlegacy.command.nations.SpaceStationCommand
import net.starlegacy.feature.nations.region.Regions
import net.starlegacy.feature.nations.region.types.RegionTerritory
import net.starlegacy.feature.starship.DeactivatedPlayerStarships
import net.starlegacy.listener.SLEventListener
import net.starlegacy.util.Tasks
import net.starlegacy.util.action
import net.starlegacy.util.colorize
import net.starlegacy.util.msg
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.block.Block
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
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.InventoryHolder

object ProtectionListener : SLEventListener() {
	@EventHandler
	fun onInventoryOpen(event: PlayerInteractEvent) {
		val block: Block? = event.clickedBlock

		// Only interacting with inventory blocks counts as editing blocks
		if (event.action == Action.RIGHT_CLICK_BLOCK && block?.state is InventoryHolder) {
			onBlockEdit(event, block.location, event.player)
		}
	}

	@EventHandler
	fun onItemFrameChange(event: PlayerItemFrameChangeEvent) {
		if (denyBlockAccess(event.player, event.itemFrame.location)) {
			event.isCancelled = true
		}
	}

	@EventHandler
	fun onBlockPlace(event: BlockPlaceEvent) = onBlockEdit(event, event.block.location, event.player)

	@EventHandler
	fun onBlockBreak(event: BlockBreakEvent) = onBlockEdit(event, event.block.location, event.player)

	private fun onBlockEdit(event: Cancellable, location: Location, player: Player) {
		if (denyBlockAccess(player, location)) {
			event.isCancelled = true
		}
	}

	/** Called on block break etc. GriefPrevention check should be done first.
	 *  Loops through protected regions at location, checks each one for access message
	 *  @return true if the event should be cancelled, false if it should stay the same. */
	fun denyBlockAccess(player: Player, location: Location): Boolean {
		if (isRegionDenied(player, location)) {
			return true
		}

		if (isLockedShipDenied(player, location)) {
			return true
		}

		return false
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
				if (!denied) { // only if no other region has already reached this, in order to maintain the priority of region messages
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

	private fun isLockedShipDenied(player: Player, location: Location): Boolean {
		if (SpaceStationCommand.disallowedWorlds.contains(location.world.name.lowercase())) return false
		val world = location.world
		val x = location.blockX
		val y = location.blockY
		val z = location.blockZ
		val data = DeactivatedPlayerStarships.getLockedContaining(world, x, y, z)
			?: return false
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
		if (!isProtectedCity(location)) {
			return
		}

		cancellable.isCancelled = true

		blocks.forEach { block ->
			if (Math.random() < 0.25) {
				val heart = if (Math.random() > 0.5) Particle.HEART else Particle.VILLAGER_ANGRY
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
		if (event.damager !is Player) {
			return
		}

		if (event.entity is Player && (isProtectedCity(event.entity.location) || isProtectedCity(event.damager.location))) {
			event.isCancelled = true
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
