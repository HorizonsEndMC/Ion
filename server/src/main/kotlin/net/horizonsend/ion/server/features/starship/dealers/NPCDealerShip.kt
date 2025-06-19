package net.horizonsend.ion.server.features.starship.dealers

import com.sk89q.worldedit.extent.clipboard.Clipboard
import kotlinx.serialization.Serializable
import net.horizonsend.ion.common.database.StarshipTypeDB
import net.horizonsend.ion.common.database.schema.starships.PlayerStarshipData
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.common.utils.text.deserializeComponent
import net.horizonsend.ion.common.utils.text.restrictedMiniMessageSerializer
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.player.NewPlayerProtection.hasProtection
import net.horizonsend.ion.server.features.starship.Starship
import net.horizonsend.ion.server.miscellaneous.utils.actualType
import net.horizonsend.ion.server.miscellaneous.utils.readSchematic
import net.horizonsend.ion.server.miscellaneous.utils.updateDisplayName
import net.horizonsend.ion.server.miscellaneous.utils.updateLore
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.lang.System.currentTimeMillis
import java.time.Duration
import java.util.UUID

class NPCDealerShip(
	val serialized: SerializableDealerShipInformation,
) : DealerShip(deserializeComponent(serialized.displayName, restrictedMiniMessageSerializer), serialized.price, serialized.protectionCanBypass, serialized.shipClass.actualType) {
	val cooldown: Duration = Duration.ofMillis(serialized.cooldown)

	private val schematicFile = IonServer.dataFolder.resolve("sold_ships").resolve("${serialized.schematicName}.schem")

	override fun getClipboard(): Clipboard {
		return readSchematic(schematicFile)!!
	}

	override fun getIcon(): ItemStack {
		return ItemStack(serialized.guiMaterial)
			.updateDisplayName(displayName)
			.updateLore(serialized.lore.map { loreLine ->
				deserializeComponent(loreLine, restrictedMiniMessageSerializer)
			})

	}

	override fun onPurchase(purchaser: Player) {
		val shipLastBuy: MutableMap<DealerShip, Long> = lastBuyTimes.getOrPut(purchaser.uniqueId) { mutableMapOf() }
		shipLastBuy[this] = currentTimeMillis()
	}

	override fun canBuy(purchaser: Player): Boolean {
		val shipLastBuy: MutableMap<DealerShip, Long> = lastBuyTimes.getOrPut(purchaser.uniqueId) { mutableMapOf() }

		if (shipLastBuy.getOrDefault(this, 0) + (cooldown.toMillis()) > currentTimeMillis()) {
			if (purchaser.hasProtection() && protectionCanBypass) {
				purchaser.information("You seem new around these parts. I usually don't do this, but I'll let you take another")
			} else {
				purchaser.userError("Didn't I sell you a ship not too long ago? These things are expensive, " +
					"and I am already selling them at a discount, leave some for other people.")
				return false
			}
		}

		return true
	}

	override fun postPilot(purchaser: Player, ship: Starship) {
		(ship.data as PlayerStarshipData).shipDealerInformation = PlayerStarshipData.ShipDealerInformation(
			soldType = serialized.schematicName,
			soldTime = currentTimeMillis(),
			creationBlockKey = ship.data.blockKey
		)
	}

	companion object {
		private val lastBuyTimes = mutableMapOf<UUID, MutableMap<DealerShip, Long>>()
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
