package net.horizonsend.ion.server.features.npcs.database.type

import net.citizensnpcs.api.npc.NPC
import net.citizensnpcs.trait.LookClose
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_LIGHT_ORANGE
import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.features.npcs.database.UniversalNPCWrapper
import net.horizonsend.ion.server.features.npcs.database.metadata.ServerShipDealerMetadata
import net.horizonsend.ion.server.features.npcs.database.metadata.UniversalNPCMetadata
import net.horizonsend.ion.server.features.starship.dealers.NPCDealerShip
import net.horizonsend.ion.server.gui.invui.misc.shipdealer.ShipDealerGUI
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import java.util.function.Consumer
import kotlin.reflect.KClass

object ServerShipDealerType : UniversalNPCType<ServerShipDealerMetadata> {
	override val metaTypeClass: KClass<ServerShipDealerMetadata> = ServerShipDealerMetadata::class
	override val identifier: String = "SERVER_SHIP_DEALER"

	override fun getDisplayName(metaData: UniversalNPCMetadata): Component {
		return Component.text("Ship Dealer", HE_LIGHT_ORANGE)
	}

	override fun handleMetaDataChange(new: String, npc: UniversalNPCWrapper<*, *>) {
		@Suppress("UNCHECKED_CAST")
		npc as UniversalNPCWrapper<ServerShipDealerType, ServerShipDealerMetadata>
		npc.metaData = 	deSerializeMetaData(new)
	}

	override fun canUseType(player: Player, metaData: ServerShipDealerMetadata): Boolean {
		return player.hasPermission("ion.npc.shipDealer")
	}

	override fun canManage(player: Player, wrapper: UniversalNPCWrapper<*, ServerShipDealerMetadata>): Boolean {
		return player.hasPermission("ion.npc.shipDealer")
	}

	override fun handleClick(player: Player, npc: NPC, metaData: ServerShipDealerMetadata) {
		ShipDealerGUI(player, ConfigurationFiles.serverConfiguration().soldShips.map(::NPCDealerShip)).openGui()
	}

	override fun applyTraits(npc: NPC, metaData: ServerShipDealerMetadata) {
		npc.getOrAddTrait(LookClose::class.java).apply {
			lookClose(true)
			setRealisticLooking(true)
		}
	}

	override fun getDefaultMetaData(): ServerShipDealerMetadata {
		return ServerShipDealerMetadata()
	}

	override fun manage(player: Player, managed: UniversalNPCWrapper<*, ServerShipDealerMetadata>, newMetaDataConsumer: Consumer<ServerShipDealerMetadata>) {
		player.userError("This NPC cannot be managed!")
	}
}
