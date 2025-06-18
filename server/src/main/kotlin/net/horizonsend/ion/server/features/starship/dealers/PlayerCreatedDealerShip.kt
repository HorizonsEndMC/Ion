package net.horizonsend.ion.server.features.starship.dealers

import com.sk89q.worldedit.extent.clipboard.Clipboard
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.schema.starships.PlayerSoldShip
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_MEDIUM_GRAY
import net.horizonsend.ion.common.utils.text.gson
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.common.utils.text.toCreditComponent
import net.horizonsend.ion.server.features.starship.StarshipType
import net.horizonsend.ion.server.miscellaneous.utils.actualType
import net.horizonsend.ion.server.miscellaneous.utils.loadClipboard
import net.horizonsend.ion.server.miscellaneous.utils.updateDisplayName
import net.horizonsend.ion.server.miscellaneous.utils.updateLore
import net.kyori.adventure.text.Component
import org.bukkit.inventory.ItemStack
import java.time.Duration
import java.util.Date

class PlayerCreatedDealerShip(
	val id: Oid<PlayerSoldShip>,
	val className: String,
	displayName: Component,
	val description: List<Component>,
	val size: Int,
	val price: Double,
	cooldown: Duration,
	protectionCanBypass: Boolean,
	starshipType: StarshipType,
	val creationDate: Date,
) : DealerShip(displayName, cooldown, protectionCanBypass, starshipType) {
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

	companion object {
		fun create(ship: PlayerSoldShip) = PlayerCreatedDealerShip(
			id = ship._id,
			className = ship.className,
			displayName = gson.deserialize(ship.name),
			description = ship.description?.map(gson::deserialize) ?: listOf(),
			size = ship.size,
			price = ship.price,
			cooldown = Duration.ofMillis(1L),
			protectionCanBypass = false,
			starshipType = ship.type.actualType,
			creationDate = ship.creationTime
		)
	}
}
