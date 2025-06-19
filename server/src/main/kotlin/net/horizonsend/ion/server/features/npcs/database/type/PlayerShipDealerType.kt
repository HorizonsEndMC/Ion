package net.horizonsend.ion.server.features.npcs.database.type

import net.citizensnpcs.api.npc.NPC
import net.citizensnpcs.trait.LookClose
import net.horizonsend.ion.common.database.cache.nations.SettlementCache
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.database.schema.nations.SettlementRole
import net.horizonsend.ion.common.database.schema.starships.PlayerSoldShip
import net.horizonsend.ion.common.database.slPlayerId
import net.horizonsend.ion.common.database.uuid
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_LIGHT_ORANGE
import net.horizonsend.ion.common.utils.text.legacyAmpersand
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.features.economy.city.TradeCities
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.custom.settings.SettingsPageGui.Companion.createSettingsPage
import net.horizonsend.ion.server.features.gui.custom.settings.button.general.ComponentSupplierConsumerInputButton
import net.horizonsend.ion.server.features.gui.custom.settings.button.general.collection.CollectionModificationButton
import net.horizonsend.ion.server.features.nations.gui.skullItem
import net.horizonsend.ion.server.features.nations.region.Regions
import net.horizonsend.ion.server.features.nations.region.types.Region
import net.horizonsend.ion.server.features.nations.region.types.RegionRentalZone
import net.horizonsend.ion.server.features.nations.region.types.RegionSettlementZone
import net.horizonsend.ion.server.features.nations.region.types.RegionTerritory
import net.horizonsend.ion.server.features.nations.region.types.RegionTopLevel
import net.horizonsend.ion.server.features.npcs.database.UniversalNPCWrapper
import net.horizonsend.ion.server.features.npcs.database.metadata.PlayerShipDealerMetadata
import net.horizonsend.ion.server.features.npcs.database.metadata.UniversalNPCMetadata
import net.horizonsend.ion.server.features.starship.dealers.PlayerCreatedDealerShip
import net.horizonsend.ion.server.gui.invui.misc.shipdealer.PlayerShipDealerGUI
import net.horizonsend.ion.server.gui.invui.misc.util.input.TextInputMenu.Companion.searchSLPlayers
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.slPlayerId
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.entity.Player
import org.litote.kmongo.and
import org.litote.kmongo.`in`
import java.util.UUID
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

	override fun checkLocation(player: Player, location: Location): Boolean {
		val regions = Regions.find(location).sortedByDescending { it.priority }
		val cached = PlayerCache[player]

		// Go down the priority level, if any allow then it overrides the ones below
		for (region in regions) {
			when (region) {
				is RegionTerritory -> {
					if (!TradeCities.isCity(region)) continue

					val settlement = region.settlement
					if (settlement != null) {
						val cachedSettlement = SettlementCache[settlement]
						if (player.slPlayerId == cachedSettlement.leader) return true
						if (SettlementRole.hasPermission(player.slPlayerId, SettlementRole.Permission.MANAGE_NPCS)) return true
					}
				}
				is RegionSettlementZone -> {
					if (!TradeCities.isCity(Regions[region.territory])) continue

					if (region.owner == player.slPlayerId) return true
					if (region.trustedPlayers?.contains(player.slPlayerId) == true) return true
					if (region.trustedSettlements?.contains(cached.settlementOid) == true) return true
					if (region.trustedNations?.contains(cached.nationOid) == true) return true

					TODO()
				}
				is RegionRentalZone -> {
					if (region.owner == player.slPlayerId) return true
					if (region.trustedPlayers.contains(player.slPlayerId)) return true
					if (region.trustedSettlements.contains(cached.settlementOid)) return true
					if (region.trustedNations.contains(cached.nationOid)) return true
				}
				else -> continue
			}
		}

		return false
	}

	override fun handleClick(player: Player, npc: UniversalNPCWrapper<*, PlayerShipDealerMetadata>, metaData: PlayerShipDealerMetadata) {
		Tasks.async {
			val territories = Regions.find(player.location).filter { it is RegionTopLevel }.map(Region<*>::id)
			val ships = PlayerSoldShip
				.find(and(PlayerSoldShip::owner `in` metaData.sellers.mapTo(mutableSetOf(), UUID::slPlayerId), PlayerSoldShip::creationTerritory `in` territories))
				.toList()
				.map(PlayerCreatedDealerShip::create)

			PlayerShipDealerGUI(player, npc, ships).openGui()
		}
	}

	override fun manage(player: Player, managed: UniversalNPCWrapper<*, PlayerShipDealerMetadata>, newMetaDataConsumer: Consumer<PlayerShipDealerMetadata>) {
		var sellersCopy = managed.metaData.sellers
		// A copy is maintained since the DB changes take time to propogate. It's possible for there to be desync but its not the end of the world.

		var npcName = managed.metaData.name

		createSettingsPage(
			player,
			"Manage Ship Dealer",
			CollectionModificationButton(
				viewer = player,
				title = Component.text("Add sellers"),
				description = "Their ships will be sold here",
				collectionSupplier = { sellersCopy },
				modifiedConsumer = {
					sellersCopy = it.toSet()
					newMetaDataConsumer.accept(managed.metaData.copy(sellers = it.toSet()))
				},
				toMutableCollection = { it.toMutableSet() },
				itemTransformer = { skullItem(it, SLPlayer.getName(it.slPlayerId) ?: "Null") },
				getItemLines = { Component.text(SLPlayer.getName(it.slPlayerId) ?: "Null") to null },
				playerModifier = { _, _ -> },
				entryCreator = { consumer -> searchSLPlayers(player) { consumer.accept(it.uuid) } }
			),
			ComponentSupplierConsumerInputButton(
				valueSupplier = { npcName },
				valueConsumer = {
					npcName = it
					newMetaDataConsumer.accept(managed.metaData.copy(name = it))
				},
				inputDescription = Component.text("Enter new NPC name."),
				name = Component.text("Rename NPC"),
				buttonDescription = "Change the NPC's name.",
				icon = GuiItem.LIST,
				defaultValue = Component.text("Ship Dealer", HE_LIGHT_ORANGE)
			)
		).openGui()
	}
}
