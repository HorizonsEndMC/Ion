package net.horizonsend.ion.server.features.npcs.database.type

import net.citizensnpcs.api.npc.NPC
import net.horizonsend.ion.common.utils.configuration.Configuration
import net.horizonsend.ion.server.features.npcs.database.DatabaseNPCs
import net.horizonsend.ion.server.features.npcs.database.UniversalNPCWrapper
import net.horizonsend.ion.server.features.npcs.database.metadata.UniversalNPCMetadata
import net.horizonsend.ion.server.miscellaneous.utils.Skins
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.entity.Player
import java.util.function.Consumer
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
	fun canManage(player: Player, wrapper: UniversalNPCWrapper<*, T>): Boolean

	fun getDefaultMetaData(): T

	fun createNew(location: Location, skinData: Skins.SkinData) {
		DatabaseNPCs.create(location, this, getDefaultMetaData(), skinData)
	}

	fun createNew(player: Player, skinData: Skins.SkinData) {
		DatabaseNPCs.create(player, player.location, this, getDefaultMetaData(), skinData)
	}

	fun manage(player: Player, managed: UniversalNPCWrapper<*, T>, newMetaDataConsumer: Consumer<T>) {}
}
