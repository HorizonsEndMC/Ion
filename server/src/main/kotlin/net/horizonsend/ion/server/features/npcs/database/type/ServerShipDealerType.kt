package net.horizonsend.ion.server.features.npcs.database.type

import net.citizensnpcs.api.npc.NPC
import net.citizensnpcs.trait.LookClose
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_LIGHT_ORANGE
import net.horizonsend.ion.server.features.npcs.database.UniversalNPCWrapper
import net.horizonsend.ion.server.features.npcs.database.metadata.ServerShipDealerMetadata
import net.horizonsend.ion.server.features.npcs.database.metadata.UniversalNPCMetadata
import net.horizonsend.ion.server.gui.invui.misc.ShipDealerGUI
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import kotlin.reflect.KClass

object ServerShipDealerType : DatabaseNPCType<ServerShipDealerMetadata> {
	override val metaTypeClass: KClass<ServerShipDealerMetadata> = ServerShipDealerMetadata::class
	override val identifier: String = "SERVER_SHIP_DEALER"

	override fun getDisplayName(metaData: UniversalNPCMetadata): Component {
		return Component.text("Ship Dealer", HE_LIGHT_ORANGE)
	}

	override fun handleMetaDataChange(new: String, npc: UniversalNPCWrapper<*, *>) {
		npc as UniversalNPCWrapper<ServerShipDealerType, ServerShipDealerMetadata>
		npc.metaData = 	deSerializeMetaData(new)
	}

	override fun canUseType(player: Player, metaData: ServerShipDealerMetadata): Boolean {
		return player.hasPermission("ion.npc.shipDealer")
	}

	override fun handleClick(player: Player, npc: NPC, metaData: ServerShipDealerMetadata) {
		ShipDealerGUI(player).openGui()
	}

	override fun applyTraits(npc: NPC, metaData: ServerShipDealerMetadata) {
		npc.getOrAddTrait(LookClose::class.java).apply {
			lookClose(true)
			setRealisticLooking(true)
		}
	}
}
