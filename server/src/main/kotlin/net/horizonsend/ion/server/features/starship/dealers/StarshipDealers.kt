package net.horizonsend.ion.server.features.starship.dealers

import com.sk89q.worldedit.extent.clipboard.Clipboard
import net.horizonsend.ion.common.database.schema.starships.PlayerStarshipData
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.common.utils.text.gui.sendWithdrawMessage
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.command.starship.BlueprintCommand
import net.horizonsend.ion.server.features.player.NewPlayerProtection.hasProtection
import net.horizonsend.ion.server.features.progression.Levels
import net.horizonsend.ion.server.features.progression.achievements.Achievement
import net.horizonsend.ion.server.features.progression.achievements.rewardAchievement
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.getMoneyBalance
import net.horizonsend.ion.server.miscellaneous.utils.hasEnoughMoney
import net.horizonsend.ion.server.miscellaneous.utils.placeSchematicEfficiently
import net.horizonsend.ion.server.miscellaneous.utils.withdrawMoney
import net.kyori.adventure.text.minimessage.MiniMessage.miniMessage
import org.bukkit.Location
import org.bukkit.entity.Player
import java.lang.System.currentTimeMillis
import java.util.UUID

object StarshipDealers : IonServerComponent(true) {
	private val lastBuyTimes = mutableMapOf<DealerShip, MutableMap<UUID, Long>>()

	fun doPurchase(player: Player, ship: DealerShip) {

	}

	fun loadShip(player: Player, ship: DealerShip) {
		Tasks.async {
			val schematic = ship.getClipboard()

			val shipLastBuy = lastBuyTimes.getOrDefault(ship, mutableMapOf())

			if (shipLastBuy.getOrDefault(player.uniqueId, 0) + (ship.cooldown.toMillis()) > currentTimeMillis()) {
				if (player.hasProtection() && ship.protectionCanBypass) {
					player.information("You seem new around these parts. I usually don't do this, but I'll let you take another")
				} else {
					player.userError("Didn't I sell you a ship not too long ago? These things are expensive, " +
						"and I am already selling them at a discount, leave some for other people.")
					return@async
				}
			}

			if (Levels[player] < ship.starshipType.minLevel) {
				player.userError("You are not a high enough level to pilot that ship!")
				return@async
			}

			if (ship is NPCDealerShip && !player.hasEnoughMoney(ship.serialized.price)) {
				player.userError("This ship is too expensive for you\n It costs ${ship.serialized.price}, you currently have ${player.getMoneyBalance()}")
				return@async
			}

			var target = player.location
			target.y = 216.0
			target = resolveTarget(schematic, target)

			val world = player.world
			val targetVec3i = Vec3i(target)

			placeSchematicEfficiently(schematic, world, targetVec3i, true) {
				val vec3i = Vec3i(target.blockX, target.blockY, target.blockZ)
				player.teleport(target.add(0.0, 1.0, 0.0).toCenterLocation())

				player.withdrawMoney(ship.price)
				sendWithdrawMessage(player, ship.price)

				ship.onPurchase(player)

				shipLastBuy[player.uniqueId] = currentTimeMillis()
				lastBuyTimes[ship] = shipLastBuy

				BlueprintCommand.tryPilot(player, vec3i, ship.starshipType, miniMessage().serialize(ship.displayName)) {
					if (ship is NPCDealerShip) (it.data as PlayerStarshipData).shipDealerInformation = PlayerStarshipData.ShipDealerInformation(
						soldType = ship.serialized.schematicName,
						soldTime = currentTimeMillis(),
						creationBlockKey = it.data.blockKey
					)
				}

				if (ship is NPCDealerShip) player.success("Successfully bought a ${ship.serialized.schematicName} (Cost: ${ship.serialized.price}\n Remaining Balance: ${player.getMoneyBalance()})")

				player.rewardAchievement(Achievement.BUY_SPAWN_SHUTTLE)
			}
		}
	}

	fun resolveTarget(schematic: Clipboard, destination: Location): Location {
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
			for (blockVector3 in schematic.region) {
				if (schematic.getBlock(blockVector3).blockType.material.isAir) {
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
