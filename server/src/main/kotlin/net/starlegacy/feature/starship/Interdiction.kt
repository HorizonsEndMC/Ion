package net.starlegacy.feature.starship

import net.horizonsend.ion.server.legacy.feedback.FeedbackType.ALERT
import net.horizonsend.ion.server.legacy.feedback.FeedbackType.SUCCESS
import net.horizonsend.ion.server.legacy.feedback.FeedbackType.USER_ERROR
import net.horizonsend.ion.server.legacy.feedback.sendFeedbackMessage
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import net.starlegacy.SLComponent
import net.starlegacy.feature.misc.CustomItems
import net.starlegacy.feature.multiblock.Multiblocks
import net.starlegacy.feature.multiblock.gravitywell.GravityWellMultiblock
import net.starlegacy.feature.space.SpaceWorlds
import net.starlegacy.feature.starship.active.ActivePlayerStarship
import net.starlegacy.feature.starship.active.ActiveStarship
import net.starlegacy.feature.starship.active.ActiveStarships
import net.starlegacy.feature.starship.control.StarshipCruising
import net.starlegacy.listen
import net.starlegacy.util.LegacyItemUtils
import net.starlegacy.util.isWallSign
import net.starlegacy.util.toLocation
import org.bukkit.World
import org.bukkit.block.Sign
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent

object Interdiction : SLComponent() {
	override fun onEnable() {
		listen<PlayerInteractEvent> { event ->
			val player = event.player
			val block = event.clickedBlock ?: return@listen
			if (!block.type.isWallSign) {
				return@listen
			}
			val sign = block.state as Sign
			val multiblock = Multiblocks[sign]
			if (multiblock !is GravityWellMultiblock) {
				return@listen
			}
			val starship = ActiveStarships.findByPassenger(player)
				?: return@listen player.sendFeedbackMessage(USER_ERROR, "You're not riding the starship")
			if (!starship.contains(block.x, block.y, block.z)) {
				return@listen
			}
			when (event.action) {
				Action.RIGHT_CLICK_BLOCK -> {
					toggleGravityWell(starship)
				}

				Action.LEFT_CLICK_BLOCK -> {
					pulseGravityWell(player, starship, sign)
				}

				else -> return@listen
			}
		}
	}

	private fun toggleGravityWell(starship: ActiveStarship) {
		when (starship.isInterdicting) {
			true -> for (player in starship.serverLevel.world.getNearbyPlayers(
				starship.centerOfMass.toLocation(starship.serverLevel.world),
				starship.type.interdictionRange.toDouble()
			)) {
				player.playSound(
					Sound.sound(
						Key.key("minecraft:entity.zombie_villager.converted"),
						Sound.Source.AMBIENT,
						5f,
						1.00f
					)
				)
			}

			false -> for (player in starship.serverLevel.world.getNearbyPlayers(
				starship.centerOfMass.toLocation(starship.serverLevel.world),
				starship.type.interdictionRange.toDouble()
			)) {
				player.playSound(
					Sound.sound(
						Key.key("minecraft:entity.zombie_villager.converted"),
						Sound.Source.AMBIENT,
						5f,
						0.05f
					)
				)
			}
		}
		starship.setIsInterdicting(!starship.isInterdicting)
	}

	private fun pulseGravityWell(player: Player, starship: ActiveStarship, sign: Sign) {
		val world = sign.world

		if (!SpaceWorlds.contains(world)) {
			player.sendFeedbackMessage(USER_ERROR, "You cannot use gravity wells within other gravity wells.")
			return
		}

		if (world.environment == World.Environment.NETHER) {
			player.sendFeedbackMessage(USER_ERROR, "You can't use gravity wells in hyperspace.")
			return
		}

		if (!starship.isInterdicting) {
			player.sendFeedbackMessage(SUCCESS, "Gravity well is disabled.")
			return
		}

		val input = GravityWellMultiblock.getInput(sign)

		if (LegacyItemUtils.getTotalItems(input, CustomItems.MINERAL_CHETHERITE.singleItem()) < 2) {
			player.sendFeedbackMessage(
				USER_ERROR,
				"Not enough hypermatter in the dropper. Two chetherite shards are required!"
			)
			return
		}

		for (cruisingShip in ActiveStarships.getInWorld(world)) {
			if (cruisingShip !is ActivePlayerStarship) {
				continue
			}

			if (cruisingShip == starship) {
				continue
			}

			if (!StarshipCruising.isCruising(cruisingShip)) {
				continue
			}

			val pilot = cruisingShip.pilot ?: continue

			if (pilot.location.distance(sign.location) > starship.interdictionRange) {
				continue
			}

			cruisingShip.cruiseData.velocity.multiply(0.8)
			cruisingShip.onlinePassengers.forEach { passenger ->
				passenger.sendFeedbackMessage(ALERT, "Quantum fluctuations detected - velocity has been reduced by 20%.")
			}
		}

		input.removeItem(CustomItems.MINERAL_CHETHERITE.itemStack(2))
		starship.onlinePassengers.forEach { passenger ->
			passenger.sendFeedbackMessage(ALERT, "Gravity pulse has been invoked by ${player.name}.")
		}
	}
}
