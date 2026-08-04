package net.horizonsend.ion.server.features.starship.dealers

import com.sk89q.worldedit.extent.clipboard.Clipboard
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.schema.misc.SLPlayerId
import net.horizonsend.ion.common.database.schema.starships.PlayerSoldShip
import net.horizonsend.ion.common.database.schema.starships.PlayerStarshipData
import net.horizonsend.ion.common.database.uuid
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_MEDIUM_GRAY
import net.horizonsend.ion.common.utils.text.gson
import net.horizonsend.ion.common.utils.text.gui.sendDepositMessage
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.common.utils.text.toCreditComponent
import net.horizonsend.ion.server.features.economy.bazaar.Bazaars
import net.horizonsend.ion.server.features.misc.ServerInboxes
import net.horizonsend.ion.server.features.nations.region.Regions
import net.horizonsend.ion.server.features.nations.region.types.RegionNPCSpaceStation
import net.horizonsend.ion.server.features.nations.region.types.RegionTerritory
import net.horizonsend.ion.server.features.starship.Starship
import net.horizonsend.ion.server.features.starship.StarshipType
import net.horizonsend.ion.server.miscellaneous.utils.actualType
import net.horizonsend.ion.server.miscellaneous.utils.depositMoney
import net.horizonsend.ion.server.miscellaneous.utils.loadClipboard
import net.horizonsend.ion.server.miscellaneous.utils.updateDisplayName
import net.horizonsend.ion.server.miscellaneous.utils.updateLore
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.Date

class PlayerCreatedDealerShip(
	val id: Oid<PlayerSoldShip>,
	val seller: SLPlayerId,
	val className: String,
	displayName: Component,
	val description: List<Component>,
	val size: Int,
	price: Double,
	protectionCanBypass: Boolean,
	starshipType: StarshipType,
	val creationDate: Date,
) : DealerShip(displayName, price, protectionCanBypass, starshipType) {
	override fun getClipboard(): Clipboard {
		return PlayerSoldShip.findById(id)!!.loadClipboard()
	}

	override fun getIcon(): ItemStack {
		val lore = mutableListOf<Component>()

		lore.add(Component.text("Description:", HE_MEDIUM_GRAY))
		lore.addAll(description)
		lore.add(Component.empty())
		lore.add(template(Component.text("Class: {0}", HE_MEDIUM_GRAY), starshipType.displayNameComponent))
		lore.add(template(Component.text("Size: {0}", HE_MEDIUM_GRAY), size))
		lore.add(template(Component.text("Price: {0}", HE_MEDIUM_GRAY), price.toCreditComponent()))
		lore.add(template(Component.text("Listed: {0}", HE_MEDIUM_GRAY), useQuotesAroundObjects = false, creationDate))

		return starshipType.menuItemRaw.get()
			.updateDisplayName(displayName)
			.updateLore(lore)
	}

	override fun onPurchase(purchaser: Player) {
		super.onPurchase(purchaser)
		PlayerSoldShip.delete(id)

		val name: String = when (val region = Regions.find(purchaser.location).firstOrNull()) {
			is RegionNPCSpaceStation -> region.name
			is RegionTerritory -> Bazaars.cityName(region)
			else -> "UNKNOWN"
		}

		val message = template(
			Component.text("{0} purchased a {1} class starship at {2} for {3}!", HE_MEDIUM_GRAY),
			purchaser.name,
			className,
			name,
			price.toCreditComponent()
		)

		ServerInboxes.sendServerMessage(seller, Component.text("Ship Purchased!"), message)

		Bukkit.getOfflinePlayer(seller.uuid).depositMoney(price)
		Bukkit.getPlayer(seller.uuid)?.let { sendDepositMessage(it, price) }
	}

	override fun postPilot(purchaser: Player, ship: Starship) {
		(ship.data as? PlayerStarshipData)?.disallowBlueprinting = true
	}

	companion object {
		fun create(ship: PlayerSoldShip) = PlayerCreatedDealerShip(
			id = ship._id,
			seller = ship.owner,
			className = ship.className,
			displayName = gson.deserialize(ship.name),
			description = ship.description?.map(gson::deserialize) ?: listOf(),
			size = ship.size,
			price = ship.price,
			protectionCanBypass = false,
			starshipType = ship.type.actualType,
			creationDate = ship.creationTime
		)
	}
}
