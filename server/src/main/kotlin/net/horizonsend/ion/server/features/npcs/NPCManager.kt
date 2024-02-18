package net.horizonsend.ion.server.features.npcs

import net.citizensnpcs.api.CitizensAPI
import net.citizensnpcs.api.npc.NPC
import net.citizensnpcs.api.npc.NPCRegistry
import net.horizonsend.ion.server.miscellaneous.utils.loadChunkAsync
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.slf4j.Logger
import java.util.UUID

class NPCManager(private val logger: Logger, val name: String) {
	private lateinit var npcRegistry: NPCRegistry
	var enabled = false

	fun enableRegistry() {
		if (!isCitizensLoaded) {
			logger.warn("Citizens not loaded! $name may not function properly!")
			return
		}

		npcRegistry = createNamedMemoryRegistry(logger, name)
		enabled = true
	}

	fun disableRegistry() {
		if (!enabled) return

		enabled = false
		clearNPCs()
	}

	fun clearNPCs() {
		if (::npcRegistry.isInitialized) {
			npcRegistry.toList().forEach(NPC::destroy)
			npcRegistry.deregisterAll()
			CitizensAPI.removeNamedNPCRegistry(name)
		}
	}

	fun spawnNPCAsync(npc: NPC, location: Location, preCheck: (NPC) -> Boolean = { true }, callback: (NPC) -> Unit = {}) {
		loadChunkAsync(location.world, location) {
			if (!preCheck(npc)) return@loadChunkAsync

			npc.spawn(location)
			callback(npc)
		}
	}

	fun allNPCs(): List<NPC> {
		if (!enabled) return listOf()
		return npcRegistry.toList()
	}

	fun isNpc(entity: Entity): Boolean {
		if (!enabled) return false

		return npcRegistry.any { it.uniqueId == entity.uniqueId }
	}

	fun contains(npc: NPC): Boolean {
		if (!enabled) return false

		return npcRegistry.contains(npc)
	}

	/**
	 * Creates the NPC and spawns it into the world
	 **/
	fun createNPC(name: String, uuid: UUID, id: Int, location: Location, preCheck: (NPC) -> Boolean = { true }, callback: (NPC) -> Unit = {}): NPC {
		if (!enabled) throw IllegalStateException()

		val npc = npcRegistry.createNPC(EntityType.PLAYER, uuid, id, name)
		spawnNPCAsync(npc, location, callback = callback)

		return npc
	}

	/**
	 * Returns whether the npc was successfully removed
	 **/
	fun removeNPC(uuid: UUID): Boolean {
		if (!enabled) return false

		val npc = npcRegistry.getByUniqueId(uuid) ?: return false

		npc.destroy()
		npcRegistry.deregister(npc)

		return true
	}
}
