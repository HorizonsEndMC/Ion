package net.horizonsend.ion.server.features.npcs.database

import net.citizensnpcs.api.npc.NPC
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.schema.misc.UniversalNPC
import net.horizonsend.ion.server.features.npcs.database.metadata.UniversalNPCMetadata
import net.horizonsend.ion.server.features.npcs.database.type.UniversalNPCType
import org.bukkit.entity.Player

class UniversalNPCWrapper<T : UniversalNPCType<M>, M : UniversalNPCMetadata>(val npc: NPC, val oid: Oid<UniversalNPC>, val type: T, metaData: UniversalNPCMetadata) {
	@Suppress("UNCHECKED_CAST")
	var metaData = metaData as M

	fun applyTraits() {
		type.applyTraits(npc, metaData)
	}

	fun canManage(player: Player): Boolean {
		return type.canManage(player, this)
	}

	fun manage(player: Player) {
		type.manage(player, this) { newMeta ->
			val serialized = type.serializeMetaData(newMeta)
			UniversalNPC.updateMetaData(oid, serialized)
		}
	}

	fun handleClick(player: Player) {
		type.handleClick(player, this, metaData)
	}
}
