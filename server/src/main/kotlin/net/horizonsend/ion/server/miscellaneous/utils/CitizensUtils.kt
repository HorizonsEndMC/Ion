package net.horizonsend.ion.server.miscellaneous.utils

import io.netty.util.internal.logging.Slf4JLoggerFactory
import net.citizensnpcs.api.CitizensAPI
import net.citizensnpcs.api.npc.MemoryNPCDataStore
import net.citizensnpcs.api.npc.NPCRegistry
import net.horizonsend.ion.server.IonServer
import org.bukkit.entity.Player

val isCitizensLoaded get() = IonServer.server.pluginManager.isPluginEnabled("Citizens")
val registries: MutableList<NPCRegistry> = mutableListOf()

private val log = Slf4JLoggerFactory.getInstance("CitizensUtils")

fun createNamedMemoryRegistry(npcRegistryName: String): NPCRegistry {
	log.info("Creating Citizens memory data store $npcRegistryName")

	val dataStore = MemoryNPCDataStore()
	val registry = CitizensAPI.createNamedNPCRegistry(npcRegistryName, dataStore)
	registries.add(registry)

	return registry
}

fun Player.isNPC() = registries.any { it.isNPC(this) }
