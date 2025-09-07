package net.horizonsend.ion.server.features.starship.dealers

import com.sk89q.worldedit.extent.clipboard.Clipboard
import com.sk89q.worldedit.math.BlockVector3
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.common.utils.miscellaneous.toCreditsString
import net.horizonsend.ion.common.utils.text.restrictedMiniMessageSerializer
import net.horizonsend.ion.common.utils.text.serialize
import net.horizonsend.ion.common.utils.text.toCreditComponent
import net.horizonsend.ion.server.command.starship.BlueprintCommand
import net.horizonsend.ion.server.core.IonServerComponent
import net.horizonsend.ion.server.features.progression.Levels
import net.horizonsend.ion.server.features.progression.achievements.Achievement
import net.horizonsend.ion.server.features.progression.achievements.rewardAchievement
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.getMoneyBalance
import net.horizonsend.ion.server.miscellaneous.utils.hasEnoughMoney
import net.horizonsend.ion.server.miscellaneous.utils.placeSchematicEfficiently
import org.bukkit.Location
import org.bukkit.entity.Player

object StarshipDealers : IonServerComponent(false) {
	fun loadShip(player: Player, ship: DealerShip) {
		Tasks.async {
			// Type specific checks
			if (!ship.canBuy(player)) return@async

			// General checks
			if (Levels[player] < ship.starshipType.minLevel) {
				player.userError("You are not a high enough level to pilot that ship!")
				return@async
			}

			if (!player.hasEnoughMoney(ship.price)) {
				player.userError("This ship is too expensive for you!\n It costs ${ship.price}, you currently have ${player.getMoneyBalance().toCreditsString()}.")
				return@async
			}

			loadDealerShipUnchecked(player, ship)
		}
	}

	fun loadDealerShipUnchecked(player: Player, ship: DealerShip) {
		val schematic = ship.getClipboard()

		var target = player.location
		target.y = 216.0
		target = resolveTarget(schematic, target)

		val world = player.world
		val targetVec3i = Vec3i(target)

		placeSchematicEfficiently(schematic, world, targetVec3i, true) {
			val vec3i = Vec3i(target.blockX, target.blockY, target.blockZ)
			player.teleport(target.add(0.0, 1.0, 0.0).add(ship.pilotOffset.toVector()).toCenterLocation())

			ship.onPurchase(player)

			BlueprintCommand.tryPilot(player, vec3i, ship.starshipType, ship.displayName.serialize(restrictedMiniMessageSerializer)) { starship ->
				ship.postPilot(player, starship)
			}

			player.success("Successfully bought a {0} (Cost: {1} Remaining Balance: {2})", ship.displayName, ship.price.toCreditComponent(), player.getMoneyBalance().toCreditComponent())
			player.rewardAchievement(Achievement.BUY_SPAWN_SHUTTLE)
		}
	}

	fun resolveTarget(schematic: Clipboard, destination: Location, forceEmpty : Boolean = false): Location {
		val target = destination.clone()

		var xOffset = listOf(-25, 25).random()
		var zOffset = listOf(-25, 25).random()
		var reversed = false
		val world = target.world

		for (i in 0..5000) {
			val targetVec = Vec3i(target)
			val dx = targetVec.x - schematic.origin.x()
			val dy = targetVec.y - schematic.origin.y()
			val dz = targetVec.z - schematic.origin.z()

			var obstructed = false
			val region = schematic.region
			region.expand(BlockVector3.at(-2,-2,-2), BlockVector3.at(2,2,2))
			for (blockVector3 in region) {
				if (!forceEmpty && schematic.getBlock(blockVector3).blockType.material.isAir) {
					continue
				}

				val x = blockVector3.x() + dx
				val y = blockVector3.y() + dy
				val z = blockVector3.z() + dz

				if (!world.worldBorder.isInside(Location(world, x.toDouble(), y.toDouble(), z.toDouble()))) {
					obstructed = true
					break
				}

				if (!world.getBlockAt(x, y, z).type.isAir) {
					obstructed = true
					break
				}
			}

			if (!obstructed) {
				return target
			}

			target.add(xOffset.toDouble(), 0.0, zOffset.toDouble())
			if (reversed || target.world.worldBorder.isInside(target)) {
				continue
			}
			reversed = true
			xOffset *= -1
			zOffset *= -1
		}

		return target
	}
}
