package net.horizonsend.ion.server.features.space.body

import org.bukkit.Bukkit
import org.bukkit.World

interface EnterableCelestialBody {
	val enteredWorldName: String

	val planetWorld: World? get() = Bukkit.getWorld(enteredWorldName)
}
