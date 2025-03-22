package net.horizonsend.ion.server.features.starship.type.restriction

import net.horizonsend.ion.server.features.world.IonWorld
import net.horizonsend.ion.server.features.world.WorldFlag

class WorldRestrictions(
	val requiredWorldFlags: Set<WorldFlag> = setOf(),
	val disallowedWorldFlags: Set<WorldFlag> = setOf(),
) {
	fun canPilotIn(world: IonWorld): Boolean {
		val flags = world.configuration.flags

		if (requiredWorldFlags.minus(flags).isNotEmpty()) return false
		return disallowedWorldFlags.intersect(disallowedWorldFlags).isEmpty()
	}
}
