package net.horizonsend.ion.server.features.starship.dealers

import com.sk89q.worldedit.extent.clipboard.Clipboard
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
	val price: Double,
	displayName: String,
	val schematicName: String,
	val guiMaterial: Material,
	cooldown: Long,
	protectionCanBypass: Boolean,
	private val shipClass: StarshipTypeDB,
	val lore: List<String>
) : DealerShip(displayName.miniMessage(), Duration.ofMillis(cooldown), protectionCanBypass, shipClass.actualType) {

	private val schematicFile = IonServer.dataFolder.resolve("sold_ships").resolve("$schematicName.schem")

	override fun getClipboard(): Clipboard {
		return readSchematic(schematicFile)!!
	}

	override fun getIcon(): ItemStack {
		return ItemStack(guiMaterial)
			.updateDisplayName(displayName)
			.updateLore(lore.map { loreLine ->
				MiniMessage.miniMessage().deserialize(loreLine)
			})

	}
}
