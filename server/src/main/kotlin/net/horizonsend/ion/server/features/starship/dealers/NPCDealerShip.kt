package net.horizonsend.ion.server.features.starship.dealers

import com.sk89q.worldedit.extent.clipboard.Clipboard
import kotlinx.serialization.Serializable
import net.horizonsend.ion.common.database.StarshipTypeDB
import net.horizonsend.ion.common.utils.text.miniMessage
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.miscellaneous.utils.actualType
import net.horizonsend.ion.server.miscellaneous.utils.readSchematic
import net.horizonsend.ion.server.miscellaneous.utils.updateDisplayName
import net.horizonsend.ion.server.miscellaneous.utils.updateLore
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import java.time.Duration

class NPCDealerShip(
	val serialized: SerializableDealerShipInformation
) : DealerShip(serialized.displayName.miniMessage(), Duration.ofMillis(serialized.cooldown), serialized.price, serialized.protectionCanBypass, serialized.shipClass.actualType) {

	private val schematicFile = IonServer.dataFolder.resolve("sold_ships").resolve("${serialized.schematicName}.schem")

	override fun getClipboard(): Clipboard {
		return readSchematic(schematicFile)!!
	}

	override fun getIcon(): ItemStack {
		return ItemStack(serialized.guiMaterial)
			.updateDisplayName(displayName)
			.updateLore(serialized.lore.map { loreLine ->
				MiniMessage.miniMessage().deserialize(loreLine)
			})

	}

	@Serializable
	data class SerializableDealerShipInformation(
		val price: Double,
		val schematicName: String,
		val guiMaterial: Material,
		val displayName: String,
		val cooldown: Long,
		val protectionCanBypass: Boolean,
		val shipClass: StarshipTypeDB,
		val lore: List<String>
	)
}
