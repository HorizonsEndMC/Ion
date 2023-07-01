package net.horizonsend.ion.server.features.multiblock.landsieges

import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.database.Oid
import net.horizonsend.ion.server.database.schema.nations.Nation
import net.horizonsend.ion.server.features.multiblock.FurnaceMultiblock
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.PowerStoringMultiblock
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.starlegacy.cache.nations.NationCache
import net.starlegacy.feature.nations.region.Regions
import net.starlegacy.feature.nations.region.types.Region
import net.starlegacy.feature.nations.region.types.RegionForwardOperatingBase
import net.starlegacy.feature.nations.region.types.RegionSiegeTerritory
import net.starlegacy.feature.space.Space
import net.starlegacy.feature.starship.PilotedStarships
import net.starlegacy.feature.starship.active.ActivePlayerStarship
import net.starlegacy.feature.starship.subsystem.weapon.projectile.TurretLaserProjectile
import net.starlegacy.util.Vec3i
import net.starlegacy.util.getFacing
import net.starlegacy.util.squared
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.block.Furnace
import org.bukkit.block.Sign
import org.bukkit.entity.Player
import org.bukkit.event.inventory.FurnaceBurnEvent

object AAGunMultiblock : Multiblock(), PowerStoringMultiblock, FurnaceMultiblock {
	override val name: String = "aagun"
	override val maxPower: Int = 1_000_000

	private val ACTIVE_STATE = text("Active", NamedTextColor.GREEN).apply { this.decoration(TextDecoration.BOLD) }
	private val INACTIVE_STATE = text("Inactive", NamedTextColor.RED).apply { this.decoration(TextDecoration.BOLD) }

	override val signText: Array<Component?> = arrayOf(
		text("Anti-Air", NamedTextColor.GOLD),
		text("Particle Gun", NamedTextColor.AQUA),
		null,
		INACTIVE_STATE
	)

	override fun MultiblockShape.buildStructure() {
		z(+0) {
			y(-1) {
				x(-1).stainedTerracotta()
				x(+0).diamondBlock()
				x(+1).stainedTerracotta()
			}

			y(+0) {
				x(-1).anyStairs()
				x(+0).diamondBlock()
				x(+1).anyStairs()
			}
		}

		z(+1) {
			y(-1) {
				x(-2).anyWall()
				x(-1).stainedTerracotta()
				x(+0).concrete()
				x(+1).stainedTerracotta()
				x(+2).anyWall()
			}

			y(+0) {
				x(-2).anyWall()
				x(-1).ironBlock()
				x(+0).ironBlock()
				x(+1).ironBlock()
				x(+2).anyWall()
			}
		}

		z(+2) {
			y(-1) {
				x(-2).stainedTerracotta()
				x(-1).concrete()
				x(+0).concrete()
				x(+1).concrete()
				x(+2).stainedTerracotta()
			}

			y(+0) {
				x(-2).stainedTerracotta()
				x(-1).ironBlock()
				x(+0).ironBlock()
				x(+1).ironBlock()
				x(+2).stainedTerracotta()
			}

			y(+1) {
				x(-1).anyWall()
				x(+0).type(Material.BEACON)
				x(+1).anyWall()
			}

			y(+2) {
				x(-1).anyWall()
				x(+1).anyWall()
			}
		}

		z(+3) {
			y(-1) {
				x(-2).anyWall()
				x(-1).stainedTerracotta()
				x(+0).concrete()
				x(+1).stainedTerracotta()
				x(+2).anyWall()
			}

			y(+0) {
				x(-2).anyWall()
				x(-1).ironBlock()
				x(+0).ironBlock()
				x(+1).ironBlock()
				x(+2).anyWall()
			}
		}

		z(+4) {
			y(-1) {
				x(-1).anyStairs()
				x(+0).ironBlock()
				x(+1).anyStairs()
			}

			y(+0) {
				x(-1).anyStairs()
				x(+0).ironBlock()
				x(+1).anyStairs()
			}
		}
	}

	override fun onTransformSign(player: Player, sign: Sign) {
		super<PowerStoringMultiblock>.onTransformSign(player, sign)

		if (Space.moonWorldCache[sign.world].isEmpty) {
			player.userError("You must be on a moon to setup an AA gun.")
			sign.block.breakNaturally()
			return
		}

		if (sign.line(3) == INACTIVE_STATE)
			sign.line(3, ACTIVE_STATE)
		else sign.line(3, INACTIVE_STATE)

		sign.update(true, false)
	}

	override fun onFurnaceTick(event: FurnaceBurnEvent, furnace: Furnace, sign: Sign) {
		val forward = sign.getFacing().oppositeFace
		val location = sign.block.getRelative(forward).getRelative(forward).getRelative(BlockFace.UP).location
		val territory = Regions.find(location).firstOrNull { it is RegionForwardOperatingBase } as? RegionForwardOperatingBase ?: return

		for ((player, ship) in PilotedStarships.map) {
			if (player.location.distanceSquared(event.block.location) <= 150.squared() /* && TODO target check */) {
				fireOnShip(player, ship, territory.nation ?: return, location)
			}
		}
	}

	private fun fireOnShip(target: Player, ship: ActivePlayerStarship, nation: Oid<Nation>, location: Location) {
		val targetLoc = Vec3i(ship.blocks.random()).toLocation(ship.serverLevel.world).toCenterLocation()
		val targetVec = targetLoc.toVector()
		val dir = targetVec.clone().subtract(ship.centerOfMassVec3i.toCenterVector()).normalize()

		TurretLaserProjectile(
			null,
			location,
			dir,
			IonServer.balancing.starshipWeapons.triTurret.speed * 2,
			Color.fromRGB(NationCache[nation].color),
			IonServer.balancing.starshipWeapons.triTurret.range,
			IonServer.balancing.starshipWeapons.triTurret.particleThickness,
			IonServer.balancing.starshipWeapons.triTurret.explosionPower,
			IonServer.balancing.starshipWeapons.triTurret.shieldDamageMultiplier,
			IonServer.balancing.starshipWeapons.triTurret.soundName,
			null
		).fire()
	}
}
