package net.horizonsend.ion.server.features.npcs.database.type

import net.citizensnpcs.api.npc.NPC
import net.citizensnpcs.trait.HologramTrait
import net.citizensnpcs.trait.LookClose
import net.horizonsend.ion.common.database.cache.nations.SettlementCache
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.database.schema.misc.UniversalNPC
import net.horizonsend.ion.common.database.schema.nations.SettlementRole
import net.horizonsend.ion.common.database.schema.starships.PlayerSoldShip
import net.horizonsend.ion.common.database.slPlayerId
import net.horizonsend.ion.common.database.uuid
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_LIGHT_ORANGE
import net.horizonsend.ion.common.utils.text.legacyAmpersand
import net.horizonsend.ion.common.utils.text.serialize
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.features.economy.city.TradeCities
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.GuiItems
import net.horizonsend.ion.server.features.gui.custom.settings.SettingsGuiItem
import net.horizonsend.ion.server.features.gui.custom.settings.SettingsPageGui
import net.horizonsend.ion.server.features.gui.custom.settings.SettingsPageGui.Companion.createSettingsPage
import net.horizonsend.ion.server.features.gui.custom.settings.button.general.ComponentSupplierConsumerInputButton
import net.horizonsend.ion.server.features.gui.custom.settings.button.general.NullableComponentSupplierConsumerInputButton
import net.horizonsend.ion.server.features.gui.custom.settings.button.general.collection.CollectionModificationButton
import net.horizonsend.ion.server.features.nations.gui.skullItem
import net.horizonsend.ion.server.features.nations.region.Regions
import net.horizonsend.ion.server.features.nations.region.types.Region
import net.horizonsend.ion.server.features.nations.region.types.RegionRentalZone
import net.horizonsend.ion.server.features.nations.region.types.RegionSettlementZone
import net.horizonsend.ion.server.features.nations.region.types.RegionTerritory
import net.horizonsend.ion.server.features.nations.region.types.RegionTopLevel
import net.horizonsend.ion.server.features.npcs.database.UniversalNPCWrapper
import net.horizonsend.ion.server.features.npcs.database.UniversalNPCs
import net.horizonsend.ion.server.features.npcs.database.metadata.PlayerShipDealerMetadata
import net.horizonsend.ion.server.features.npcs.database.metadata.UniversalNPCMetadata
import net.horizonsend.ion.server.features.starship.dealers.PlayerCreatedDealerShip
import net.horizonsend.ion.server.gui.invui.misc.shipdealer.PlayerShipDealerGUI
import net.horizonsend.ion.server.gui.invui.misc.util.input.TextInputMenu.Companion.searchSLPlayers
import net.horizonsend.ion.server.miscellaneous.utils.Skins
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.slPlayerId
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
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

			npc.npc.getOrAddTrait(HologramTrait::class.java).apply {
				val newLine = deserialized.titleLine

				if (newLine == null) clear()
				else setLine(0, newLine.serialize(legacyAmpersand))
			}
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

		if (metaData.titleLine != null) {
			npc.getOrAddTrait(HologramTrait::class.java).apply {
				setLine(0, metaData.titleLine.serialize(legacyAmpersand))
			}
		}
	}

	override fun canManage(player: Player, wrapper: UniversalNPCWrapper<*, PlayerShipDealerMetadata>): Boolean {
		return player.uniqueId == wrapper.metaData.owner
	}

	override fun canCreate(player: Player, metaData: PlayerShipDealerMetadata): Boolean {
		val currentTerritories = Regions.find(player.location)
		val otherOwned = UniversalNPCs.getAll(PlayerShipDealerType).filter { it.metaData.owner == player.uniqueId }
		val regionEmpty = otherOwned.none {
			val npcTerritories = Regions.find(it.npc.storedLocation)
			currentTerritories.intersect(npcTerritories.toSet()).isNotEmpty()
		}

		if (!regionEmpty) player.userError("You already have a NPC of this type in this region!")

		return regionEmpty
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
		var npcTitleLine = managed.metaData.titleLine

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
			),
			NullableComponentSupplierConsumerInputButton(
				valueSupplier = { npcTitleLine },
				valueConsumer = {
					npcTitleLine = it
					newMetaDataConsumer.accept(managed.metaData.copy(titleLine = it))
				},
				inputDescription = Component.text("Enter new NPC title."),
				name = Component.text("Change Title"),
				buttonDescription = "Change the title line.",
				icon = GuiItem.LIST,
				defaultValue = Component.empty()
			),
			object : SettingsGuiItem {
				override fun getFirstLine(player: Player): Component = Component.text("Change Skin", NamedTextColor.BLUE)
				override fun getSecondLine(player: Player): Component = Component.empty()

				override fun makeButton(pageGui: SettingsPageGui): GuiItems.AbstractButtonItem {
					return GuiItem.LIST.makeButton(pageGui, Component.text("Change Skin"), "Click to change the NPC's skin") { player, _, parent ->
						searchSLPlayers(player) { id ->
							Tasks.async {
								runCatching {
									val skinData: Skins.SkinData = Skins[id.uuid] ?: return@async
									UniversalNPC.updateSkinData(managed.oid, skinData.toBytes())
								}
								parent.openGui()
							}
						}
					}
				}
			}
		).openGui()
	}
}
