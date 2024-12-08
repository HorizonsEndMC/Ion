package net.horizonsend.ion.server.features.npcs

import kotlinx.serialization.Serializable
import net.citizensnpcs.api.event.NPCRightClickEvent
import net.citizensnpcs.trait.HologramTrait
import net.citizensnpcs.trait.LookClose
import net.citizensnpcs.trait.SkinTrait
import net.horizonsend.ion.common.extensions.serverError
import net.horizonsend.ion.common.utils.configuration.Configuration
import net.horizonsend.ion.common.utils.configuration.UUIDSerializer
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.configuration.ServerConfiguration
import net.horizonsend.ion.server.features.npcs.traits.ShipDealerTrait
import net.horizonsend.ion.server.features.tutorial.npcs.TutorialNPCType
import net.horizonsend.ion.server.miscellaneous.utils.Skins
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacyAmpersand
import org.bukkit.Location
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import java.util.UUID

object TutorialNPCs : IonServerComponent(true) {
	val manager = NPCManager(log, "TutorialNPCs")

	override fun onEnable() {
		manager.enableRegistry()

		loadNPCs()
	}

	override fun onDisable() {
		manager.disableRegistry()
	}

	private var npcMetaData: TutorialNPCs = Configuration.load(JsonNPCStore.npcStorageDirectory, "TutorialNPCs.json")

	fun createNPC(location: Location, type: TutorialNPCType, uuid: UUID, save: Boolean = true) {
		manager.createNPC(
			legacyAmpersand().serialize(type.displayName),
			uuid,
			3000 + manager.allNPCs().size,
			location
		) callback@{ npc ->
			Tasks.async {
				val skin = Skins["https://assets.horizonsend.net/training_droid.png"] ?: return@async

				Tasks.sync {
					npc.getOrAddTrait(SkinTrait::class.java).apply {
						setSkinPersistent("tutorial_droid", skin.signature, skin.value)
					}

					npc.getOrAddTrait(LookClose::class.java).apply {
						lookClose(true)
						setRealisticLooking(true)
					}

					npc.getOrAddTrait(HologramTrait::class.java).apply {
						type.billboardText.map { legacyAmpersand().serialize(it) }.forEach(this::addLine)
					}

					npc.getOrAddTrait(ShipDealerTrait::class.java).apply {

					}
				}
			}

			if (!save) return@callback

			npcMetaData.data[npc.uniqueId] = TutorialNPCs.TutorialNPCData(
				ServerConfiguration.Pos(location.world.name, location.blockX, location.blockY, location.blockZ),
				type
			)

			saveStorage()
		}
	}

	fun removeNPC(uuid: UUID) {
		manager.removeNPC(uuid)
		npcMetaData.data.remove(uuid)
		saveStorage()
	}

	private fun loadNPCs() {
		manager.clearNPCs()

		for ((uuid, data) in npcMetaData.data) {
			createNPC(data.location.toLocation(), data.type, uuid, save = false)
		}
	}

	private fun saveStorage() {
		onDisable()

		Configuration.save(npcMetaData, JsonNPCStore.npcStorageDirectory, "TutorialNPCs.json")

		npcMetaData = Configuration.load(JsonNPCStore.npcStorageDirectory, "TutorialNPCs.json")

		onEnable()
	}

	@EventHandler(priority = EventPriority.LOWEST)
	fun onClickNPC(event: NPCRightClickEvent) {
		if (!manager.contains(event.npc)) return

		if (!ConfigurationFiles.featureFlags().tutorials) {
			event.clicker.serverError("Tutorials are not enabled on this server, this NPC was probably placed in error.")
			return
		}

		val stored = npcMetaData.data[event.npc.uniqueId] ?: return

		stored.type.onRightClick(event)
	}

	@Serializable
	data class TutorialNPCs( // TODO expand this with more data
		val data: MutableMap<@Serializable(with = UUIDSerializer::class) UUID, TutorialNPCData> = mutableMapOf()
	) {
		@Serializable
		data class TutorialNPCData(
			val location: ServerConfiguration.Pos,
			val type: TutorialNPCType
		)
	}
}
