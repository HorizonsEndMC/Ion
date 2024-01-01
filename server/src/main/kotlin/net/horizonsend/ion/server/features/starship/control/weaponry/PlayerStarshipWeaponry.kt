package net.horizonsend.ion.server.features.starship.control.weaponry

import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.command.admin.debug
import net.horizonsend.ion.server.command.admin.debugBanner
import net.horizonsend.ion.server.features.starship.AutoTurretTargeting
import net.horizonsend.ion.server.features.starship.PilotedStarships
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.starship.control.movement.PlayerStarshipControl
import net.horizonsend.ion.server.features.starship.control.movement.StarshipControl
import net.horizonsend.ion.server.features.starship.control.weaponry.StarshipWeaponry.manualFire
import net.horizonsend.ion.server.features.starship.control.weaponry.StarshipWeaponry.rightClickTimes
import net.horizonsend.ion.server.features.starship.damager.damager
import net.horizonsend.ion.server.miscellaneous.utils.displayNameString
import net.horizonsend.ion.server.miscellaneous.utils.isSign
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import java.util.concurrent.TimeUnit
import org.bukkit.Material

object PlayerStarshipWeaponry : IonServerComponent() {
	@EventHandler(priority = EventPriority.LOW)
	fun onClick(event: PlayerInteractEvent) {
		val player = event.player

		player.debugBanner("INTERACT EVENT MANUAL FIRE START")
		if (!PlayerStarshipControl.isHoldingController(player)) {
			return
		}

		player.debug("player has controller")

		val starship = ActiveStarships.findByPassenger(player) ?: return

		player.debug("player is piloting")

		if (event.action.isLeftClick) {
			event.isCancelled = true
		}
		player.debug("player is rclicking")
		if (event.action.isRightClick) {
			val damager = player.damager()
			val elapsedSinceRightClick = System.nanoTime() - rightClickTimes.getOrDefault(damager, 0)

			player.debug("elapsedSinceRCLICK = $elapsedSinceRightClick")

			if (elapsedSinceRightClick > TimeUnit.MILLISECONDS.toNanos(250)) {
				player.debug("Heavy weapon fire is not on minimum cooldown")
				rightClickTimes[damager] = System.nanoTime()
				return
			}

			player.debug("it's a doubleclick, going on")
			rightClickTimes.remove(damager)
		}

		if (event.clickedBlock?.type?.isSign == true) return

		player.debug("Didn't click sign, trying to fire")

		manualFire(player, starship, event.action.isLeftClick, player.inventory.itemInMainHand)

		player.debugBanner("END")
	}
	@EventHandler(priority = EventPriority.LOW)
	fun onShiftRClick(event: PlayerInteractEvent) {

		val player = event.player

		player.debugBanner("INTERACT EVENT TURRET TARGETING START")
		if (!PlayerStarshipControl.isHoldingController(player)) {
			return
		}
		val starship = ActiveStarships.findByPassenger(player) ?: return

		if (event.action.isLeftClick) {
			event.isCancelled = true
		}

		if (player.isSneaking) {
			if (event.action.isRightClick) {
				// I am truly sorry, but i could not think of a better way to do this
				val ignoreBlockList = setOf(Material.AIR, Material.BLACK_STAINED_GLASS, Material.GRAY_STAINED_GLASS, Material.RED_STAINED_GLASS, Material.LIGHT_GRAY_STAINED_GLASS, Material.BLACK_STAINED_GLASS_PANE, Material.GRAY_STAINED_GLASS_PANE, Material.RED_STAINED_GLASS_PANE, Material.LIGHT_GRAY_STAINED_GLASS_PANE)
				val hitpoints = player.getLineOfSight(ignoreBlockList, 600)
				val detectedBlock = hitpoints.last()
				player.debug("hitpoints recorded")
				val item = player.inventory.itemInMainHand
				val clockWeaponSet = item.displayNameString.lowercase()
				var weaponSet = starship.weaponSetSelections[player.uniqueId]
				if (hitpoints.isEmpty()) {
					starship.autoTurretTargets.remove(weaponSet)
					player.debug("unset turrets as hitpoints is empty")
					return
				}
				player.debug("first hitpoints detected:")
				player.debug(detectedBlock.x.toString())
				player.debug(detectedBlock.y.toString())
				player.debug(detectedBlock.z.toString())
				player.debug("detected hit on:")
				player.debug(detectedBlock.blockData.asString)
				if (starship.weaponSets.keys().contains(clockWeaponSet)) {
					weaponSet = clockWeaponSet
					player.debug("found a weaponset")
					ActiveStarships.getInWorld(player.world).find { it.contains(detectedBlock.x, detectedBlock.y, detectedBlock.z) }?.let {
						player.debug("found a ship")
						starship.autoTurretTargets[weaponSet] = AutoTurretTargeting.target(it)
						player.debug("set the target to $it")
						return
					}
					starship.autoTurretTargets.remove(weaponSet)
				}
				return
			}
		}
	}
	@EventHandler
	fun onPlayerItemHoldEvent(event: PlayerItemHeldEvent) {
		val itemStack: ItemStack = event.player.inventory.getItem(event.newSlot) ?: return
		val starship = ActiveStarships.findByPassenger(event.player) ?: return

		if (itemStack.type != StarshipControl.CONTROLLER_TYPE) return

		val itemName = itemStack.displayNameString.lowercase()

		if (starship.weaponSets.keys().contains(itemName)) {
			event.player.sendActionBar(
				MiniMessage.miniMessage().deserialize("<gray>Now firing <aqua>$itemName<gray> weaponSet")
			)
		}
	}

	fun manualFire(
		player: Player,
		starship: ActiveStarship,
		leftClick: Boolean,
		clock: ItemStack
	) {
		// Mantain multicrew capabilities by creating a player damager if they're not the pilot
		val damager = player.damager()

		starship.debug("Manual firing")

		val loc = player.eyeLocation
		val playerFacing = player.facing
		val dir = loc.direction.normalize()

		val target: Vector = StarshipWeaponry.getTarget(loc, dir, starship)

		var weaponSet = starship.weaponSetSelections[player.uniqueId]
		val clockWeaponSet = clock.displayNameString.lowercase()

		starship.debug("Selected weapon set: $weaponSet")
		starship.debug("Clock weapon set: $clockWeaponSet")

		if (starship.weaponSets.keys().contains(clockWeaponSet)) weaponSet = clockWeaponSet

		starship.debug("Final weapon set: $clockWeaponSet")

		if (weaponSet == null && PilotedStarships[player] != starship) {
			starship.debug("Returning because of weird condition")
			return
		}

		manualFire(
			damager,
			starship,
			leftClick,
			playerFacing,
			dir,
			target,
			weaponSet
		)
	}
}
