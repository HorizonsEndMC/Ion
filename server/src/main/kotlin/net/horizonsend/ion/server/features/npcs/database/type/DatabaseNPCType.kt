package net.horizonsend.ion.server.features.npcs.database.type

import net.citizensnpcs.api.npc.NPC
import net.horizonsend.ion.common.utils.configuration.Configuration
import net.horizonsend.ion.server.features.npcs.database.UniversalNPCWrapper
import net.horizonsend.ion.server.features.npcs.database.metadata.UniversalNPCMetadata
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import kotlin.reflect.KClass

interface DatabaseNPCType<T: UniversalNPCMetadata> {
	val metaTypeClass: KClass<T>
	val identifier: String

	fun applyTraits(npc: NPC, metaData: T)

	fun getDisplayName(metaData: UniversalNPCMetadata): Component

	fun deSerializeMetaData(raw: String): T {
		return Configuration.parse(metaTypeClass, raw)
	}

	fun serializeMetaData(raw: T): String {
		return Configuration.write(metaTypeClass, raw)
	}

	fun handleMetaDataChange(new: String, npc: UniversalNPCWrapper<*, *>)

	fun handleClick(player: Player, npc: NPC, metaData: T)

	fun canUseType(player: Player, metaData: T): Boolean
}
