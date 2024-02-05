package net.horizonsend.ion.server.features.npcs

import net.citizensnpcs.api.CitizensAPI
import net.citizensnpcs.api.npc.MemoryNPCDataStore
import net.citizensnpcs.api.npc.NPC
import net.citizensnpcs.api.npc.NPCRegistry
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.miscellaneous.utils.loadChunkAsync
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Entity

abstract class NPCFeature : IonServerComponent(true) {
	val npcRegistryName = javaClass.name
	lateinit var npcRegistry: NPCRegistry

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

	fun isNpc(entity: Entity): Boolean? {
		if (!isCitizensLoaded) return null

		return npcRegistry.isNPC(entity)
	}

	fun spawnNPCAsync(npc: NPC, world: World, location: Location, preCheck: (NPC) -> Boolean = { true }, spawn: () -> Unit = {}) {
		loadChunkAsync(location.world, location) {
			if (!preCheck(npc)) return@loadChunkAsync

			spawn()
		}
	}
}
