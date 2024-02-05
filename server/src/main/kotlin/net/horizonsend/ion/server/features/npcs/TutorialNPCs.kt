package net.horizonsend.ion.server.features.npcs

import kotlinx.serialization.Serializable
import net.citizensnpcs.api.npc.NPC
import net.citizensnpcs.api.npc.NPCRegistry
import net.citizensnpcs.trait.LookClose
import net.citizensnpcs.trait.SkinTrait
import net.horizonsend.ion.server.configuration.ServerConfiguration
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.entity.EntityType
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
		override val type: EntityType,
		val skinName: String,
		val skinSignature: String,
		val skinValue: String
	) : JsonNPCStore.NPC {
		override fun createNPC(registry: NPCRegistry, index: Int): NPC {
			val npc = registry.createNPC(
				type,
				UUID.randomUUID(),
				2000 + index,
				LegacyComponentSerializer.legacyAmpersand().serialize(Component.text("Tutorial Droid", NamedTextColor.LIGHT_PURPLE))
			)

			val location = position.toLocation()

			StarshipDealers.spawnNPCAsync(
				npc = npc,
				world = location.world,
				location = location,
				spawn = {
					npc.getOrAddTrait(SkinTrait::class.java).apply {
						setSkinPersistent(this@TutorialDroid.skinName, skinSignature, skinValue)
					}

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
}
