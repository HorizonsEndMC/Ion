package net.horizonsend.ion.server.features.npcs

import kotlinx.serialization.Serializable
import net.citizensnpcs.api.npc.NPCRegistry
import net.horizonsend.ion.common.utils.configuration.Configuration
import net.horizonsend.ion.common.utils.configuration.UUIDSerializer
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.configuration.ServerConfiguration
import java.util.UUID

class JsonNPCStore<T: JsonNPCStore.NPC>(private val feature: NPCFeature, val loadCallback: (net.citizensnpcs.api.npc.NPC) -> Unit = {}) {
	private fun loadConfiguration(): Storage<T> = Configuration.load(npcStorageDirectory, "${feature.npcRegistryName}.json")

	val storage by lazy { loadConfiguration() }

	fun loadNPCs() {
		if (!isCitizensLoaded) return

		val registry = feature.npcRegistry

		storage.npcs.withIndex().forEach { (index, value) ->
			val npc = value.createNPC(registry, index)
			loadCallback(npc)
		}
	}

	fun saveStorage() {
		Configuration.save(storage, npcStorageDirectory, "${feature.npcRegistryName}.json")
	}

	@Serializable
	sealed interface NPC {
		val position: ServerConfiguration.Pos
		@Serializable(with = UUIDSerializer::class) val uuid: UUID

		fun createNPC(registry: NPCRegistry, index: Int): net.citizensnpcs.api.npc.NPC
	}

	@Serializable
	data class Storage<T: NPC>(
		val npcs: MutableList<T> = mutableListOf()
	)

	companion object {
		private val npcStorageDirectory = IonServer.configurationFolder.resolve("npcs").apply { mkdirs() }
	}
}
