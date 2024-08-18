package net.horizonsend.ion.server.features.npcs.traits

import net.citizensnpcs.api.CitizensAPI
import net.citizensnpcs.api.persistence.PersistenceLoader
import net.citizensnpcs.api.persistence.Persister
import net.citizensnpcs.api.trait.TraitInfo
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.npcs.isCitizensLoaded
import org.bukkit.inventory.ItemStack

object NPCTraits : IonServerComponent(true) {
	private val traits = listOf(
		ShipDealerTrait::class.java,
		CombatNPCTrait::class.java
	)

	private val persistenceTypes = mapOf<Class<*>, Class<out Persister<*>>>(
		Array<ItemStack?>::class.java to CombatNPCTrait.InventoryPersister::class.java
	)

	override fun onEnable() {
		if (!isCitizensLoaded) return

		val factory = CitizensAPI.getTraitFactory()

		for (trait in traits) {
			factory.registerTrait(TraitInfo.create(trait))
		}

		for ((clazz, persister) in persistenceTypes) {
			PersistenceLoader.registerPersistDelegate(clazz, persister)
		}
	}
}
