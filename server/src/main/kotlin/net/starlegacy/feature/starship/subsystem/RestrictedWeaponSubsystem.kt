package net.starlegacy.feature.starship.subsystem

import net.starlegacy.feature.starship.active.ActiveStarship

interface RestrictedWeaponSubsystem {
	fun isRestricted(starship: ActiveStarship): Boolean
}
