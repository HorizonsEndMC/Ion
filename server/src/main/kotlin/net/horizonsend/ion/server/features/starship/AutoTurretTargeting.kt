package net.horizonsend.ion.server.features.starship

import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player

object AutoTurretTargeting : IonServerComponent() {
	sealed class TargetType<T>(val get: (ActiveStarship, String) -> Location?) {
		data object PlayerTarget: TargetType<Player>({ _, name ->

			val player = Bukkit.getPlayer(name)
			if (player != null) {
				val starship = ActiveStarships.findByPilot(player)
				if (starship != null) {
					starship.blocks.random()?.let { Vec3i(it).toLocation(starship.world) }
				} else {
					Bukkit.getPlayer(name)?.location
				}
			} else {
				Bukkit.getPlayer(name)?.location
			}
		})
		data object StarshipTarget: TargetType<ActiveStarship>({ _, identifier ->
			val starship = ActiveStarships[identifier]

			starship?.blocks?.random()?.let { Vec3i(it).toLocation(starship.world) }
		})
		data object HostileMobTarget: TargetType<ActiveStarship>({ ship, identifier ->
			val type = EntityType.valueOf(identifier)

			ship.world.getNearbyEntitiesByType(type.entityClass, ship.centerOfMass.toLocation(ship.world), 100.0).firstOrNull()?.location
		})
	}

	data class AutoTurretTarget<T>(
		val type: TargetType<T>,
		val identifier: String
	) {
		fun location(starship: ActiveStarship) = type.get(starship, identifier)
	}

	fun target(starship: ActiveStarship): AutoTurretTarget<*> = AutoTurretTarget(TargetType.StarshipTarget, starship.charIdentifier)
	fun target(entityType: EntityType): AutoTurretTarget<*> = AutoTurretTarget(TargetType.HostileMobTarget, entityType.toString())
	fun target(player: Player): AutoTurretTarget<*> = AutoTurretTarget(TargetType.PlayerTarget, player.name)

}
