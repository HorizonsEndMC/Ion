package net.starlegacy.feature.multiblock.defenseturret

import com.google.common.collect.HashMultimap
import java.util.UUID
import net.starlegacy.cache.nations.PlayerCache
import net.starlegacy.cache.nations.RelationCache
import net.starlegacy.cache.nations.SettlementCache
import net.starlegacy.database.Oid
import net.starlegacy.database.schema.nations.NationRelation
import net.starlegacy.feature.machine.PowerMachines
import net.starlegacy.feature.multiblock.FurnaceMultiblock
import net.starlegacy.feature.multiblock.MultiblockShape
import net.starlegacy.feature.multiblock.PowerStoringMultiblock
import net.starlegacy.feature.nations.region.Regions
import net.starlegacy.feature.nations.region.types.RegionSpaceStation
import net.starlegacy.feature.nations.region.types.RegionTerritory
import net.starlegacy.feature.starship.subsystem.weapon.projectile.APLaserProjectile
import net.starlegacy.util.getFacing
import net.starlegacy.util.randomDouble
import org.bukkit.FluidCollisionMode
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.block.Furnace
import org.bukkit.block.Sign
import org.bukkit.entity.Player
import org.bukkit.event.inventory.FurnaceBurnEvent
import org.bukkit.util.Vector

object APTurret : PowerStoringMultiblock(), FurnaceMultiblock {
	private const val MAX_DISTANCE = 64.0
	private const val POWER_USAGE = 100

	override val name: String = "apturret"
	override val maxPower: Int = 10000

	val regionalTargets = HashMultimap.create<Oid<*>, UUID>()

	override val signText: List<String> = createSignText(
		"&cAP",
		"&eTurret",
		null,
		null
	)

	override fun MultiblockShape.buildStructure() {
		z(+0) {
			y(-1) {
				x(-1).anyStairs()
				x(+0).wireInputComputer()
				x(+1).anyStairs()
			}
			y(+0) {
				x(-1).anyStairs()
				x(+0).machineFurnace()
				x(+1).anyStairs()
			}
		}
		z(+1) {
			y(-1) {
				x(-1).anyGlass()
				x(+0).titaniumBlock()
				x(+1).anyGlass()
			}
			y(+0) {
				x(-1).anyGlass()
				x(+0).redstoneBlock()
				x(+1).anyGlass()
			}
			y(+1) {
				x(+0).redstoneLamp()
			}
		}
		z(+2) {
			y(-1) {
				x(-1).anyStairs()
				x(+0).stoneBrick()
				x(+1).anyStairs()
			}
			y(+0) {
				x(-1).anyStairs()
				x(+0).stoneBrick()
				x(+1).anyStairs()
			}
		}
	}

	override fun onFurnaceTick(event: FurnaceBurnEvent, furnace: Furnace, sign: Sign) {
		event.isBurning = false
		event.burnTime = 0
		event.isCancelled = true

		val smelting = furnace.inventory.smelting ?: return
		val fuel = furnace.inventory.fuel ?: return

		if (smelting.type != Material.PRISMARINE_CRYSTALS || fuel.type != Material.PRISMARINE_CRYSTALS) {
			return
		}

		val power = PowerMachines.getPower(sign, true)

		if (power < POWER_USAGE) {
			return
		}

		val didShoot = shootNearby(sign)

		if (didShoot) {
			PowerMachines.removePower(sign, POWER_USAGE)
		}

		event.isBurning = false
		event.burnTime = if (didShoot) 5 else 20
		furnace.cookTime = (-1000).toShort()
		event.isCancelled = false
	}

	private fun shootNearby(sign: Sign): Boolean {
		val sourceBlock = sign.block
			.getRelative(sign.getFacing().oppositeFace, 2)
			.getRelative(BlockFace.UP, 2)
		val sourceLocation = sourceBlock.location.toCenterLocation()
		sourceLocation.y = sourceLocation.blockY + 0.1

		val player = sourceBlock.world
			.getNearbyPlayers(sourceLocation, MAX_DISTANCE)
			.filterNot { isObstructed(sourceLocation, it) }
			.filter { player -> isHostile(player, sourceLocation) }
			.filter { player -> player.gameMode == GameMode.SURVIVAL }
			.randomOrNull()
			?: return false

		val direction = getDirection(sourceLocation, player)
		val horizontalAxis = direction.clone()
		horizontalAxis.y = 0.0
		horizontalAxis.rotateAroundY(90.0)
		horizontalAxis.normalize()

		direction.rotateAroundAxis(horizontalAxis, Math.toRadians(randomDouble(-3.0, 3.0)))
		direction.rotateAroundY(Math.toRadians(randomDouble(-3.0, 3.0)))
		APLaserProjectile(null, sourceLocation, direction, MAX_DISTANCE, null).fire()
		return true
	}

	private fun isObstructed(sourceLocation: Location, player: Player): Boolean {
		val direction = getDirection(sourceLocation, player)
		val result = sourceLocation.world.rayTrace(
			sourceLocation,
			direction,
			MAX_DISTANCE,
			FluidCollisionMode.NEVER,
			true,
			1.0
		) { true }
		return result?.hitEntity != player
	}

	private fun isHostile(player: Player, sourceLocation: Location): Boolean {
		val playerNation = PlayerCache[player].nation

		for (region in Regions.find(sourceLocation)) {
			if (regionalTargets[region.id].contains(player.uniqueId)) {
				return true
			}

			if (playerNation == null) {
				continue
			}

			when (region) {
				is RegionTerritory -> {
					val settlement = region.settlement
					if (settlement != null) {
						val settlementNation = SettlementCache[settlement].nation
							?: continue

						return RelationCache[settlementNation, playerNation] <= NationRelation.Level.ENEMY
					}

					val nation = region.nation
						?: continue
					if (RelationCache[nation, playerNation] <= NationRelation.Level.ENEMY) {
						return true
					}
				}
				is RegionSpaceStation -> {
					if (RelationCache[region.nation, playerNation] <= NationRelation.Level.ENEMY) {
						return true
					}
				}
			}
		}

		return false
	}

	private fun getDirection(sourceLocation: Location, player: Player): Vector {
		return player.eyeLocation.toVector().subtract(sourceLocation.toVector()).normalize()
	}
}
