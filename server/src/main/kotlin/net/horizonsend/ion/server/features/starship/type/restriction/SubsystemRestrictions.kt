package net.horizonsend.ion.server.features.starship.type.restriction

import net.horizonsend.ion.server.features.starship.Starship

class SubsystemRestrictions(vararg val subsystemRestrictions: SubsystemRestriction) {
	fun check(starship: Starship): Boolean {
		return subsystemRestrictions.all { restriction -> restriction.check(starship) }
	}
}
