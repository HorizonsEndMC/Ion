package net.horizonsend.ion.server.features.npcs

import net.citizensnpcs.api.CitizensAPI
import net.citizensnpcs.api.npc.MemoryNPCDataStore
import net.citizensnpcs.api.npc.NPC
import net.citizensnpcs.api.npc.NPCRegistry
import net.horizonsend.ion.server.IonServerComponent
import org.bukkit.entity.Player

abstract class NPCFeature : IonServerComponent(true) {
	protected val npcRegistryName = javaClass.name
	protected lateinit var npcRegistry: NPCRegistry

	protected fun createNamedMemoryRegistry(npcRegistryName: String): NPCRegistry {
		log.info("Creating Citizens memory data store $npcRegistryName")

		val dataStore = MemoryNPCDataStore()
		val registry = CitizensAPI.createNamedNPCRegistry(npcRegistryName, dataStore)
		registries.add(registry)

		return registry
	}

	protected open fun setupRegistry() {
		if (!isCitizensLoaded) {
			log.warn("Citizens not loaded! $npcRegistryName may not function properly!")
			return
		}

		npcRegistry = createNamedMemoryRegistry(npcRegistryName)
	}

	protected open fun disableRegistry() {
		if (!isCitizensLoaded) return

		if (::npcRegistry.isInitialized) {
			npcRegistry.toList().forEach(NPC::destroy)
			npcRegistry.deregisterAll()
			CitizensAPI.removeNamedNPCRegistry(npcRegistryName)
		}
	}

	fun isNpc(player: Player): Boolean? {
		if (!isCitizensLoaded) return null

		return npcRegistry.isNPC(player)
	}
}
