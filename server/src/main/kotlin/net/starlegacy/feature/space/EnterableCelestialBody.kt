package net.starlegacy.feature.space

import net.starlegacy.util.Vec3i
import org.bukkit.Bukkit
import org.bukkit.World

abstract class EnterableCelestialBody(
	open val worldName: String,
	spaceWorldName: String,
	location: Vec3i
): CelestialBody(spaceWorldName, location), NamedCelestialBody {
	abstract val outerRadius: Int

	val planetWorld: World? get() = Bukkit.getWorld(worldName)
}
