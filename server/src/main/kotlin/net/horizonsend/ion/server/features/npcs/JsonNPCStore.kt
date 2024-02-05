package net.horizonsend.ion.server.features.npcs

import kotlinx.serialization.Serializable
import net.citizensnpcs.api.npc.NPCRegistry
import net.horizonsend.ion.common.utils.configuration.Configuration
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.configuration.ServerConfiguration
import org.bukkit.entity.EntityType

class JsonNPCStore<T: JsonNPCStore.NPC>(private val feature: NPCFeature, val loadCallback: (net.citizensnpcs.api.npc.NPC) -> Unit = {}) {
	private fun loadConfiguration(): Storage<T> = Configuration.load(npcStorageDirectory, "${feature.npcRegistryName}.json")

	val configuration by lazy { loadConfiguration() }

	fun loadNPCs() {
		if (!isCitizensLoaded) return

		val registry = feature.npcRegistry

		configuration.npcs.withIndex().forEach { (index, value) ->
			val npc = value.createNPC(registry, index)
			loadCallback(npc)
		}
	}

	@Serializable
	interface NPC {
		val position: ServerConfiguration.Pos
		val type: EntityType

		fun createNPC(registry: NPCRegistry, index: Int): net.citizensnpcs.api.npc.NPC
	}

	@Serializable
	data class Storage<T: NPC>(
		val npcs: List<T>
	)

	companion object {
		private val npcStorageDirectory = IonServer.configurationFolder.resolve("npcs").apply { mkdirs() }
	}
}
