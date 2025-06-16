package net.horizonsend.ion.server.features.npcs.database

import net.citizensnpcs.api.npc.NPC
import net.horizonsend.ion.server.features.npcs.database.metadata.UniversalNPCMetadata
import net.horizonsend.ion.server.features.npcs.database.type.DatabaseNPCType
import org.bukkit.entity.Player

class UniversalNPCWrapper<T : DatabaseNPCType<M>, M : UniversalNPCMetadata>(val npc: NPC, val type: T, metaData: UniversalNPCMetadata) {
	@Suppress("UNCHECKED_CAST")
	var metaData = metaData as M

	fun applyTraits() {
		type.applyTraits(npc, metaData)
	}

	fun handleClick(player: Player) {
		type.handleClick(player, npc, metaData)
	}
}
