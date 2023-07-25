package net.horizonsend.ion.server.listener.gear

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.machine.AreaShields
import net.horizonsend.ion.server.miscellaneous.registrations.legacy.CustomItems
import net.horizonsend.ion.server.listener.SLEventListener
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.LivingEntity
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockExplodeEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.util.Vector
import java.util.Locale

object DetonatorListener : SLEventListener() {
	@EventHandler(priority = EventPriority.LOWEST)
	fun onThrowDetonator(event: PlayerInteractEvent) {
		if (event.action != Action.RIGHT_CLICK_BLOCK && event.action != Action.RIGHT_CLICK_AIR) {
			return
		}

		val item = event.item ?: return
		val customItem = CustomItems[item]

		if (customItem != CustomItems.DETONATOR) {
			return
		}

		event.isCancelled = true
		val player = event.player
		val newItem = item.clone()
		newItem.amount = 1
		val detonator = player.world.dropItem(player.eyeLocation, newItem)
		detonator.pickupDelay = Integer.MAX_VALUE
		detonator.velocity = player.location.direction
		player.world.playSound(player.location, "laser", 5f, 0.5f)
		if (detonator.isDead) {
			return
		}
		Tasks.bukkitRunnable {
			if (detonator.isDead) {
				cancel()
				return@bukkitRunnable
			}

			if (detonator.velocity.distance(Vector(0, 0, 0)) == 0.0 && detonator.ticksLived < 20 * 1.5) {
				player.world.playSound(detonator.location, Sound.BLOCK_NOTE_BLOCK_BELL, 10f, 1.0f)
				return@bukkitRunnable
			}

			val hitBlock = detonator.location.add(detonator.velocity).block
			val type = hitBlock.type

			if (!detonator.isOnGround && detonator.ticksLived < 20 * 1.5 && (type == Material.AIR || hitBlock.isLiquid)) {
				return@bukkitRunnable
			}

			detonator.setGravity(false)
			detonator.velocity = Vector(0, 0, 0)

			Tasks.syncDelay(Math.max(1.0, 20 * 1.5 - detonator.ticksLived).toLong()) {
				detonator.remove()

				val blocks = ArrayList<Block>()
				val block = detonator.location.block

				for (x in -2..2) {
					for (y in -2..2) {
						for (z in -2..2) {
							val toExplode = block.getRelative(BlockFace.EAST, x)
								.getRelative(BlockFace.UP, y)
								.getRelative(BlockFace.SOUTH, z)

							if (toExplode.type != Material.AIR) {
								blocks.add(toExplode)
							}
						}
					}
				}

				val blockExplodeEvent = BlockExplodeEvent(block, blocks, 0.123f)
				AreaShields.bypassShieldEvents.add(blockExplodeEvent)
				player.world.createExplosion(detonator, 1f, false, false)
				player.world.playSound(detonator.location, Sound.ENTITY_GENERIC_EXPLODE, 10f, 0.5f)

				if (!blockExplodeEvent.callEvent() && !detonator.world.name.lowercase(Locale.getDefault())
					.contains("arena")
				) {
					return@syncDelay
				}

				blocks.forEach { it.setType(Material.AIR, false) }

				detonator.world.getNearbyEntities(detonator.location, 4.0, 4.0, 4.0)
					.stream().map { it as? LivingEntity }
					.forEach { it?.damage(80.0 / it.location.distance(detonator.location), player) }
			}
		}.runTaskTimer(IonServer, 1, 1)
		item.amount = 0
	}
}
