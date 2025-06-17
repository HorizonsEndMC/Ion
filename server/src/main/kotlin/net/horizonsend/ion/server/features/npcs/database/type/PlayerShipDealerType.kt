package net.horizonsend.ion.server.features.npcs.database.type

import net.citizensnpcs.api.npc.NPC
import net.citizensnpcs.trait.LookClose
import net.horizonsend.ion.common.utils.text.legacyAmpersand
import net.horizonsend.ion.server.features.npcs.database.UniversalNPCWrapper
import net.horizonsend.ion.server.features.npcs.database.metadata.PlayerShipDealerMetadata
import net.horizonsend.ion.server.features.npcs.database.metadata.UniversalNPCMetadata
import net.horizonsend.ion.server.gui.invui.misc.ShipDealerGUI
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import java.util.function.Consumer
import kotlin.reflect.KClass

object PlayerShipDealerType : UniversalNPCType<PlayerShipDealerMetadata> {
	override val metaTypeClass: KClass<PlayerShipDealerMetadata> = PlayerShipDealerMetadata::class
	override val identifier: String = "PLAYER_SHIP_DEALER"

	override fun getDefaultMetaData(): PlayerShipDealerMetadata {
		return PlayerShipDealerMetadata(null)
	}

	override fun getDefaultMetaData(creator: Player): PlayerShipDealerMetadata {
		return PlayerShipDealerMetadata(creator.uniqueId)
	}

	override fun handleMetaDataChange(new: String, npc: UniversalNPCWrapper<*, *>) {
		val deserialized = PlayerShipDealerType.deSerializeMetaData(new)

		@Suppress("UNCHECKED_CAST")
		npc as UniversalNPCWrapper<PlayerShipDealerType, PlayerShipDealerMetadata>
		npc.metaData = deserialized

		Tasks.sync {
			npc.npc.name = legacyAmpersand.serialize(deserialized.name)
		}
	}

	override fun getDisplayName(metaData: UniversalNPCMetadata): Component {
		metaData as PlayerShipDealerMetadata
		return metaData.name
	}

	override fun applyTraits(npc: NPC, metaData: PlayerShipDealerMetadata) {
		npc.getOrAddTrait(LookClose::class.java).apply {
			lookClose(true)
			setRealisticLooking(true)
		}
	}

	override fun canManage(player: Player, wrapper: UniversalNPCWrapper<*, PlayerShipDealerMetadata>): Boolean {
		return player.uniqueId == wrapper.metaData.owner
	}

	override fun canUseType(player: Player, metaData: PlayerShipDealerMetadata): Boolean {
		return true
	}

	override fun handleClick(player: Player, npc: NPC, metaData: PlayerShipDealerMetadata) {
		ShipDealerGUI(player, listOf()).openGui()
	}

	override fun manage(player: Player, managed: UniversalNPCWrapper<*, PlayerShipDealerMetadata>, newMetaDataConsumer: Consumer<PlayerShipDealerMetadata>) {

	}
}
