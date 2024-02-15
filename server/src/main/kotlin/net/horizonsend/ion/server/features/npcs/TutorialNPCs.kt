package net.horizonsend.ion.server.features.npcs

import kotlinx.serialization.Serializable
import net.citizensnpcs.api.event.NPCRightClickEvent
import net.citizensnpcs.api.npc.NPC
import net.citizensnpcs.api.npc.NPCRegistry
import net.citizensnpcs.trait.LookClose
import net.horizonsend.ion.common.utils.configuration.UUIDSerializer
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.configuration.ServerConfiguration
import net.horizonsend.ion.server.features.tutorial.npcs.TutorialNPCType
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.entity.EntityType
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import java.util.UUID

object TutorialNPCs : NPCFeature() {
	val store = JsonNPCStore<TutorialDroid>(this)

	override fun onEnable() {
		setupRegistry()

		store.loadNPCs()
	}

	override fun onDisable() {
		disableRegistry()
	}

	@Serializable
	data class TutorialDroid(
		override val position: ServerConfiguration.Pos,
		@Serializable(with = UUIDSerializer::class) override val uuid: UUID,
		val type: TutorialNPCType
	) : JsonNPCStore.NPC {
		override fun createNPC(registry: NPCRegistry, index: Int): NPC {
			val npc = registry.createNPC(
				EntityType.PLAYER,
				uuid,
				2000 + index,
				LegacyComponentSerializer.legacyAmpersand().serialize(type.npcName)
			)

			val location = position.toLocation()

			StarshipDealers.spawnNPCAsync(
				npc = npc,
				world = location.world,
				location = location,
				spawn = {
//					npc.getOrAddTrait(SkinTrait::class.java).apply {
//						setSkinPersistent(this@TutorialDroid.skinName, skinSignature, skinValue)
//					}

					npc.getOrAddTrait(LookClose::class.java).apply {
						lookClose(true)
						setRealisticLooking(true)
					}

					npc.isProtected = true

					npc.spawn(location)
				}
			)

			return npc
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	fun onClickNPC(event: NPCRightClickEvent) {
		if (!IonServer.featureFlags.tutorials) return

		val stored = store.storage.npcs.first { it.uuid == event.npc.uniqueId }

		stored.type.onRightClick(event)
	}
}
