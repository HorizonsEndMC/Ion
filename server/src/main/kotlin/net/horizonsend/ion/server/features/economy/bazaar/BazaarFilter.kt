package net.horizonsend.ion.server.features.economy.bazaar

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.horizonsend.ion.common.database.schema.economy.BazaarItem
import net.horizonsend.ion.common.database.schema.economy.BazaarOrder
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.database.schema.nations.Territory
import net.horizonsend.ion.common.database.slPlayerId
import net.horizonsend.ion.common.database.uuid
import net.horizonsend.ion.common.redis.kserializers.UUIDSerializer
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.server.features.economy.city.CityNPCs.BAZAAR_CITY_TERRITORIES
import net.horizonsend.ion.server.features.economy.city.TradeCities
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.custom.settings.button.general.collection.CollectionModificationButton
import net.horizonsend.ion.server.features.nations.gui.skullItem
import net.horizonsend.ion.server.features.nations.region.Regions
import net.horizonsend.ion.server.gui.invui.InvUIWindowWrapper
import net.horizonsend.ion.server.gui.invui.bazaar.getMenuTitleName
import net.horizonsend.ion.server.gui.invui.misc.util.input.TextInputMenu.Companion.searchSLPlayers
import net.horizonsend.ion.server.gui.invui.misc.util.input.TextInputMenu.Companion.searchTradeCities
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor.RED
import net.kyori.adventure.text.format.NamedTextColor.WHITE
import org.bson.types.ObjectId
import org.bukkit.entity.Player
import org.litote.kmongo.id.WrappedObjectId
import java.util.UUID

@Serializable
sealed interface BazaarFilter {
	fun matches(item: BazaarItem): Boolean
	fun matches(order: BazaarOrder): Boolean

	fun getSettingsMenu(player: Player, parent: PlayerFilters): InvUIWindowWrapper

	@Transient
	val description: Component

	@Serializable
	data class CityBlacklist(var cities: Set<String> = setOf()) : BazaarFilter {
		@Transient
		override val description: Component = template(Component.text("{0} cit${if (cities.size == 1) "y" else "ies"} will be hidden.", RED), cities.size)

		override fun matches(item: BazaarItem): Boolean {
			return !cities.contains(item.cityTerritory.id.toHexString())
		}
		override fun matches(order: BazaarOrder): Boolean {
			return !cities.contains(order.cityTerritory.id.toHexString())
		}

		override fun getSettingsMenu(player: Player, parent: PlayerFilters): InvUIWindowWrapper {
			return CollectionModificationButton(
				viewer = player,
				title = Component.text(""),
				description = "",
				collectionSupplier = { cities },
				modifiedConsumer = { cities = it.toSet() },
				toMutableCollection = { it.toMutableSet() },
				itemTransformer = { TradeCities.getIfCity(Regions[WrappedObjectId<Territory>(ObjectId(it))])?.planetIcon ?: GuiItem.CANCEL.makeItem(Component.text("Region is no longer a trade city.")) },
				getItemLines = {
					val name = Bazaars.cityName(Regions[WrappedObjectId<Territory>(ObjectId(it))])
					getMenuTitleName(Component.text(name, WHITE)) to Component.text("Whitelisted") },
				playerModifier = { _, _ -> },
				entryCreator = { consumer ->
					searchTradeCities(
						player,
						TradeCities.getAll().filter { BAZAAR_CITY_TERRITORIES.contains(it.territoryId) },
						backButtonHandler = { this@CollectionModificationButton.openGui() },
						handler = { _, result -> consumer.accept(result.territoryId.id.toHexString()) }
					)
				}
			)
		}
	}

	@Serializable
	data class CityWhitelist(var cities: Set<String> = setOf()) : BazaarFilter {
		@Transient
		override val description: Component = template(Component.text("{0} cit${if (cities.size == 1) "y" else "ies"} will be shown.", RED), cities.size)

		override fun matches(item: BazaarItem): Boolean {
			return cities.contains(item.cityTerritory.id.toHexString())
		}
		override fun matches(order: BazaarOrder): Boolean {
			return cities.contains(order.cityTerritory.id.toHexString())
		}

		override fun getSettingsMenu(player: Player, parent: PlayerFilters): InvUIWindowWrapper {
			return CollectionModificationButton(
				viewer = player,
				title = Component.text(""),
				description = "",
				collectionSupplier = { cities },
				modifiedConsumer = { cities = it.toSet() },
				toMutableCollection = { it.toMutableSet() },
				itemTransformer = { TradeCities.getIfCity(Regions[WrappedObjectId<Territory>(ObjectId(it))])?.planetIcon ?: GuiItem.CANCEL.makeItem(Component.text("Region is no longer a trade city.")) },
				getItemLines = {
					val name = Bazaars.cityName(Regions[WrappedObjectId<Territory>(ObjectId(it))])
					getMenuTitleName(Component.text(name, WHITE)) to Component.text("Hidden") },
				playerModifier = { _, _ -> },
				entryCreator = { consumer ->
					searchTradeCities(
						player,
						TradeCities.getAll().filter { BAZAAR_CITY_TERRITORIES.contains(it.territoryId) },
						backButtonHandler = { this@CollectionModificationButton.openGui() },
						handler = { _, result -> consumer.accept(result.territoryId.id.toHexString()) }
					)
				}
			)
		}
	}

	@Serializable
	data class PlayerBlacklist(var players: Set<@Serializable(with = UUIDSerializer::class) UUID> = setOf()) : BazaarFilter {
		@Transient
		override val description: Component = template(Component.text("{0} player${if (players.size == 1) "" else "s"} will be hidden.", RED), players.size)

		override fun matches(item: BazaarItem): Boolean {
			return !players.contains(item.seller.uuid)
		}
		override fun matches(order: BazaarOrder): Boolean {
			return !players.contains(order.player.uuid)
		}

		override fun getSettingsMenu(player: Player, parent: PlayerFilters): InvUIWindowWrapper {
			return CollectionModificationButton(
				viewer = player,
				title = Component.text(""),
				description = "",
				collectionSupplier = { players },
				modifiedConsumer = { players = it.toSet() },
				toMutableCollection = { it.toMutableSet() },
				itemTransformer = { skullItem(it, SLPlayer.getName(it.slPlayerId)!!) },
				getItemLines = {
					val name = SLPlayer.getName(it.slPlayerId)!!
					getMenuTitleName(Component.text(name, WHITE)) to Component.text("Hidden") },
				playerModifier = { _, _ -> },
				entryCreator = { consumer ->
					searchSLPlayers(viewer) { consumer.accept(it.uuid) }
				}
			)
		}
	}

	@Serializable
	data class PlayerWhitelist(var players: Set<@Serializable(with = UUIDSerializer::class) UUID> = setOf()) : BazaarFilter {
		@Transient
		override val description: Component = template(Component.text("{0} player${if (players.size == 1) "" else "s"} will be shown.", RED), players.size)

		override fun matches(item: BazaarItem): Boolean {
			return players.contains(item.seller.uuid)
		}
		override fun matches(order: BazaarOrder): Boolean {
			return players.contains(order.player.uuid)
		}

		override fun getSettingsMenu(player: Player, parent: PlayerFilters): InvUIWindowWrapper {
			return CollectionModificationButton(
				viewer = player,
				title = Component.text(""),
				description = "",
				collectionSupplier = { players },
				modifiedConsumer = { players = it.toSet() },
				toMutableCollection = { it.toMutableSet() },
				itemTransformer = { skullItem(it, SLPlayer.getName(it.slPlayerId)!!) },
				getItemLines = {
					val name = SLPlayer.getName(it.slPlayerId)!!
					getMenuTitleName(Component.text(name, WHITE)) to Component.text("Whitelisted") },
				playerModifier = { _, _ -> },
				entryCreator = { consumer ->
					searchSLPlayers(viewer) { consumer.accept(it.uuid) }
				}
			)
		}
	}
}
