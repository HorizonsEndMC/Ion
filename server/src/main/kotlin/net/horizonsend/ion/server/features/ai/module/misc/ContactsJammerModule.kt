package net.horizonsend.ion.server.features.ai.module.misc

import net.horizonsend.ion.server.features.ai.util.AITarget
import net.horizonsend.ion.server.features.ai.util.PlayerTarget
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.features.starship.status_effects.StarshipStatusEffect
import net.horizonsend.ion.server.features.starship.status_effects.StarshipStatusEffectTypes
import java.util.function.Supplier

class ContactsJammerModule(
	controller: AIController,
	private val range: Double,
	private val targetingSupplier: Supplier<List<AITarget>>
) : net.horizonsend.ion.server.features.ai.module.AIModule(controller) {
	private var ticks = 0
	override fun tick() {
		/*
		ticks++

		if (ticks % 5 == 0) {
			val targets = targetingSupplier.get()
			for (target in targets) {
				if (target is PlayerTarget && controller.getCenter().distance(target.getVec3i()) < range) {
					starship.addStatusEffect(StarshipStatusEffect(
						StarshipStatusEffectTypes.JAMMED,
						1.0,
						20L,
					))
				}
			}
		}
		 */
	}
}
