package net.horizonsend.ion.server.features.npcs.traits

import net.citizensnpcs.api.CitizensAPI
import net.citizensnpcs.api.trait.Trait
import net.citizensnpcs.api.trait.TraitInfo
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.npcs.isCitizensLoaded

object NPCTraits : IonServerComponent(true) {
	private val traits = listOf<Class<out Trait>>(
		ShipDealerTrait::class.java
	)

	override fun onEnable() {
		if (!isCitizensLoaded) return

		val factory = CitizensAPI.getTraitFactory()

		for (trait in traits) {
			factory.registerTrait(TraitInfo.create(trait))
		}
	}
}
