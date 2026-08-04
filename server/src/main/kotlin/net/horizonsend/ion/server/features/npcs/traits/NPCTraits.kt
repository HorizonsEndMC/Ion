package net.horizonsend.ion.server.features.npcs.traits

import net.citizensnpcs.api.CitizensAPI
import net.citizensnpcs.api.persistence.PersistenceLoader
import net.citizensnpcs.api.persistence.Persister
import net.citizensnpcs.api.trait.Trait
import net.citizensnpcs.api.trait.TraitInfo
import net.horizonsend.ion.server.core.IonServerComponent
import net.horizonsend.ion.server.features.npcs.isCitizensLoaded
import org.bukkit.inventory.ItemStack
import java.lang.Class
import kotlin.collections.listOf
import kotlin.collections.mapOf

object NPCTraits : IonServerComponent(true) {
	private lateinit var traits: List<Class<out Trait>>

	private lateinit var persistenceTypes: Map<Class<*>, Class<out Persister<*>>>

	override fun onEnable() {
		if (!isCitizensLoaded) return

		traits = listOf(
			ShipDealerTrait::class.java,
			CombatNPCTrait::class.java
		)

		persistenceTypes = mapOf<Class<*>, Class<out Persister<*>>>(
			Array<ItemStack?>::class.java to CombatNPCTrait.InventoryPersister::class.java
		)

		val factory = CitizensAPI.getTraitFactory()

		for (trait in traits) {
			factory.registerTrait(TraitInfo.create(trait))
		}

		for ((clazz, persister) in persistenceTypes) {
			PersistenceLoader.registerPersistDelegate(clazz, persister)
		}
	}
}
