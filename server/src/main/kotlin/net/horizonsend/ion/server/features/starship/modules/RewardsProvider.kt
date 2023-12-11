package net.horizonsend.ion.server.features.starship.modules

import net.horizonsend.ion.server.features.starship.damager.Damager

interface RewardsProvider {
	open fun onDamaged(damager: Damager) {}

	fun onSink()
}
