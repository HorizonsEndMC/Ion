package net.horizonsend.ion.server.features.npcs

import kotlinx.serialization.Serializable
import net.citizensnpcs.api.npc.NPC
import net.citizensnpcs.api.npc.NPCDataStore
import net.citizensnpcs.api.npc.NPCRegistry
import net.horizonsend.ion.common.utils.configuration.Configuration
import net.horizonsend.ion.common.utils.configuration.UUIDSerializer
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import org.bukkit.entity.EntityType
import java.util.UUID

class JsonNPCStore(private val start: Int, val name: String) : NPCDataStore {
	private fun loadConfiguration(): NPCStorage = Configuration.load(npcStorageDirectory, "$name.json")

	private fun editConfiguration(edit: NPCStorage.() -> Unit) = Configuration.save(edit(storage), npcStorageDirectory, "${javaClass.simpleName}.json")

	private var storage = loadConfiguration()

	@Serializable
	private data class NPCStorage(
		val npcs: MutableList<StoredNPC> = mutableListOf()
	)

	@Serializable
	private data class StoredNPC(
		val name: String,
		val x: Double,
		val y: Double,
		val z: Double,
		val world: String,
		@Serializable(with = UUIDSerializer::class) val uuid: UUID,
		val id: Int,
		val type: EntityType
	)

	val npc: MutableList<NPC> = mutableListOf()

	override fun clearData(npc: NPC) = editConfiguration { npcs.removeAll { it.uuid == npc.uniqueId } }

	override fun createUniqueNPCId(registry: NPCRegistry): Int {
		return start + storage.npcs.size
	}

	override fun loadInto(registry: NPCRegistry) {
		for (npc in storage.npcs) {
			registry.createNPC(npc.type, npc.uuid, createUniqueNPCId(registry), npc.name)
		}
	}

	override fun reloadFromSource() {
		storage = loadConfiguration()
	}

	override fun saveToDisk() = Tasks.async {
		Configuration.save(storage, npcStorageDirectory, "${javaClass.simpleName}.json")
	}

	override fun saveToDiskImmediate() {
		Configuration.save(storage, npcStorageDirectory, "${javaClass.simpleName}.json")
	}

	override fun store(npc: NPC) {
		storage.npcs.add(StoredNPC(
			npc.name,
			npc.storedLocation.x,
			npc.storedLocation.y,
			npc.storedLocation.z,
			npc.storedLocation.world.name,
			npc.uniqueId,
			npc.id,
			npc.entity.type
		))
	}

	override fun storeAll(registry: NPCRegistry) {
		for (npc in registry) {
			store(npc)
		}
	}

	companion object {
		val npcStorageDirectory = IonServer.configurationFolder.resolve("npcs").apply { mkdirs() }
	}
}
