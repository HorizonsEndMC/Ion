package net.horizonsend.ion.server.features.starship

import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.mainThreadCheck
import org.bukkit.World

open class Starship(world: World, centerOfMass: Vec3i) {
	open var world: World = world
		set(value) {
			mainThreadCheck()
			field = value
		}

	open var centerOfMass: Vec3i = centerOfMass
		set(value) {
			mainThreadCheck()
			field = value
		}
}
