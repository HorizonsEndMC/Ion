package net.horizonsend.ion.server.features.npcs

import net.citizensnpcs.api.CitizensAPI
import net.citizensnpcs.api.npc.MemoryNPCDataStore
import net.citizensnpcs.api.npc.NPCRegistry
import net.horizonsend.ion.server.IonServer
import org.bukkit.entity.Player
import org.slf4j.Logger

val isCitizensLoaded get() = IonServer.server.pluginManager.isPluginEnabled("Citizens")
val registries: MutableList<NPCRegistry> = mutableListOf()

fun Player.isNPC() = registries.any { it.isNPC(this) }

fun createNamedMemoryRegistry(logger: Logger, npcRegistryName: String): NPCRegistry {
	logger.info("Creating Citizens memory data store $npcRegistryName")

	val dataStore = MemoryNPCDataStore()
	val registry = CitizensAPI.createNamedNPCRegistry(npcRegistryName, dataStore)
	registries.add(registry)

	return registry
}
