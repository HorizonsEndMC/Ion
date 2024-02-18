package net.horizonsend.ion.server.features.npcs

import kotlinx.serialization.Serializable
import net.citizensnpcs.api.event.NPCRightClickEvent
import net.horizonsend.ion.common.utils.configuration.Configuration
import net.horizonsend.ion.common.utils.configuration.UUIDSerializer
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.tutorial.npcs.TutorialNPCType
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacyAmpersand
import org.bukkit.Location
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import java.util.UUID

object TutorialNPCs : IonServerComponent() {
	val manager = NPCManager("TutorialNPCs")

	override fun onEnable() {
		manager.enableRegistry()
	}

	override fun onDisable() {
		manager.disableRegistry()
	}

	private var npcTypes: TutorialNPCData = Configuration.load(JsonNPCStore.npcStorageDirectory, "TutorialNPCData.json")

	fun createNPC(location: Location, type: TutorialNPCType) {
		manager.createNPC(
			legacyAmpersand().serialize(type.npcName),
			UUID.randomUUID(),
			7777 + manager.allNPCs().size,
			location
		) { npc ->
			editStorage {
				types[npc.uniqueId] = type
			}
		}
	}

	fun editStorage(edit: TutorialNPCData.() -> Unit) {
		onDisable()

		Configuration.save(edit(npcTypes), JsonNPCStore.npcStorageDirectory, "TutorialNPCData.json")

		npcTypes = Configuration.load(JsonNPCStore.npcStorageDirectory, "TutorialNPCData.json")

		onEnable()
	}

	@EventHandler(priority = EventPriority.LOWEST)
	fun onClickNPC(event: NPCRightClickEvent) {
		if (!IonServer.featureFlags.tutorials) return

//		val stored = store.storage.npcs.first { it.uuid == event.npc.uniqueId }

//		stored.type.onRightClick(event)
	}

	@Serializable
	data class TutorialNPCData( // TODO expand this with more data
		val types: MutableMap<@Serializable(with = UUIDSerializer::class) UUID, TutorialNPCType> = mutableMapOf()
	)
}
