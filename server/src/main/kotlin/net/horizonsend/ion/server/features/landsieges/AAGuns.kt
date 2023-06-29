package net.horizonsend.ion.server.features.landsieges

import net.horizonsend.ion.server.IonComponent
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.database.Oid
import net.horizonsend.ion.server.database.schema.nations.Nation
import net.horizonsend.ion.server.database.schema.nations.NationRelation
import net.horizonsend.ion.server.database.slPlayerId
import net.horizonsend.ion.server.features.multiblock.Multiblocks
import net.starlegacy.cache.nations.NationCache
import net.starlegacy.cache.nations.PlayerCache
import net.starlegacy.cache.nations.RelationCache
import net.starlegacy.feature.starship.PilotedStarships
import net.starlegacy.feature.starship.active.ActivePlayerStarship
import net.starlegacy.feature.starship.subsystem.weapon.projectile.TurretLaserProjectile
import net.starlegacy.util.Tasks
import net.starlegacy.util.Vec3i
import net.starlegacy.util.getFacing
import net.starlegacy.util.squared
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.entity.Player

object AAGuns : IonComponent() {
	val map = mutableMapOf<Oid<Nation>, MutableList<Location>>()

	override fun onEnable() {
		Nation.all().forEach { n ->
			map[n._id] = n.aaguns.map { it.toLocation() }.toMutableList()
		}

		Tasks.syncRepeat(0L, 1L, ::task)
	}

	fun task() {
		val list = mutableMapOf<Oid<Nation>, MutableList<Location>>()

		map.forEach { (nation, locations) ->
			list[nation] = locations
				.filter { Multiblocks[it.block.getState(false) as Sign, true, false] == null }
				.map {
					val sign = it.block.getState(false) as Sign
					val forward = sign.getFacing().oppositeFace

					it.block.getRelative(forward).getRelative(forward).getRelative(BlockFace.UP).location
				}
				.toMutableList()
		}

		for ((player, ship) in PilotedStarships.map) {
			for ((nation, locations) in list) {
				for (location in locations) {
					if (player.location.distanceSquared(location) <= 150.squared() /* && TODO target check */) {
						fireOnShip(player, ship, nation, location)
					}
				}
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
