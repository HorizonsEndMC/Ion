package net.horizonsend.ion.server.features.starship.control.weaponry

import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.command.admin.debug
import net.horizonsend.ion.server.command.admin.debugBanner
import net.horizonsend.ion.server.features.starship.PilotedStarships
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.starship.control.controllers.player.ActivePlayerController
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
			val damager = player.damager(starship)
			val elapsedSinceRightClick = System.nanoTime() - rightClickTimes.getOrDefault(damager, 0)

			player.debug("elapsedSinceRCLICK = $elapsedSinceRightClick")

			if (elapsedSinceRightClick > TimeUnit.MILLISECONDS.toNanos(250)) {
				player.debug("click isn't doubleclick, adding...")
				rightClickTimes[damager] = System.nanoTime()
				return
			}

			player.debug("it's a doubleclick, going on")
			rightClickTimes.remove(damager)
		}

		if (event.clickedBlock?.type?.isSign == true) return

		player.debug("didnt click sign, trying to fire")

		manualFire(player, starship, event.action.isLeftClick, player.inventory.itemInMainHand)

		player.debugBanner("END")
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
		val damager = ActivePlayerController[player] ?: player.damager(starship)

		val loc = player.eyeLocation
		val playerFacing = player.facing
		val dir = loc.direction.normalize()

		val target: Vector = StarshipWeaponry.getTarget(loc, dir, starship)

		var weaponSet = starship.weaponSetSelections[player.uniqueId]
		val clockWeaponSet = clock.displayNameString.lowercase()

		if (starship.weaponSets.keys().contains(clockWeaponSet)) weaponSet = clockWeaponSet

		if (weaponSet == null && PilotedStarships[player] != starship) return

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
