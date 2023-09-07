package net.horizonsend.ion.server.features.starship

import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player

object AutoTurretTargeting : IonServerComponent() {
	sealed class TargetType<T>(val get: (String) -> Location?) {
		data object PlayerTarget: TargetType<Player>({ name -> Bukkit.getPlayer(name)?.location })
		data object StarshipTarget: TargetType<ActiveStarship>({ identifier ->
			val starship = ActiveStarships[identifier]

			starship?.blocks?.random()?.let { Vec3i(it).toLocation(starship.world) }
		})
	}

	// This could be expanded to add entity targeting

	data class AutoTurretTarget<T>(
		val type: TargetType<T>,
		val identifier: String
	) {
		fun location() = type.get(identifier)
	}

	fun target(starship: ActiveStarship): AutoTurretTarget<*> = AutoTurretTarget(TargetType.StarshipTarget, starship.charIdentifier)
	fun target(player: Player): AutoTurretTarget<*> = AutoTurretTarget(TargetType.PlayerTarget, player.name)

}
