package net.starlegacy.listener.nations

import net.horizonsend.ion.common.extensions.information
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.BLUE
import net.kyori.adventure.text.format.NamedTextColor.GOLD
import net.kyori.adventure.title.Title
import net.kyori.adventure.title.Title.Times.times
import net.starlegacy.cache.nations.NationCache
import net.starlegacy.cache.nations.SettlementCache
import net.starlegacy.database.Oid
import net.starlegacy.database.schema.nations.NPCTerritoryOwner
import net.starlegacy.database.schema.nations.Nation
import net.starlegacy.database.schema.nations.Settlement
import net.starlegacy.database.schema.nations.SettlementZone
import net.starlegacy.database.schema.nations.Territory
import net.starlegacy.feature.nations.region.Regions
import net.starlegacy.feature.nations.region.types.Region
import net.starlegacy.feature.nations.region.types.RegionSettlementZone
import net.starlegacy.feature.nations.region.types.RegionTerritory
import net.starlegacy.listener.SLEventListener
import net.starlegacy.util.Tasks
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.lang.System.currentTimeMillis
import java.time.Duration.ofMillis
import java.util.*

object MovementListener : SLEventListener() {
	override fun supportsVanilla(): Boolean {
		return true
	}

	private val lastMoved = Collections.synchronizedMap(mutableMapOf<UUID, Long>())
	private val lastPlayerTerritories = Collections.synchronizedMap(mutableMapOf<UUID, Oid<Territory>?>())
	private val lastPlayerZones = Collections.synchronizedMap(mutableMapOf<UUID, Oid<SettlementZone>?>())

	@EventHandler
	fun onPlayerMove(event: PlayerMoveEvent) {
		if (lastMoved.containsKey(event.player.uniqueId) &&
			currentTimeMillis() - lastMoved.getOrElse(event.player.uniqueId) { currentTimeMillis() } < 1000
		) {
			return
		}
		lastMoved[event.player.uniqueId] = currentTimeMillis()

		val player: Player = event.player

		val territory: RegionTerritory? = Regions.findFirstOf(event.to)

		val uuid = player.uniqueId
		val oldTerritory: Oid<Territory>? = lastPlayerTerritories[uuid]

		if (oldTerritory != territory?.id) {
			lastPlayerTerritories[uuid] = territory?.id

			if (territory != null) {
				Tasks.async {
					var subtitle = territory.name

					territory.settlement?.let { id: Oid<Settlement> ->
						subtitle += " (${SettlementCache[id].name})"
					}

					territory.nation?.let { id: Oid<Nation> ->
						subtitle += " (${NationCache[id].name})"
					}

					territory.npcOwner?.let { id: Oid<NPCTerritoryOwner> ->
						subtitle += " (${NPCTerritoryOwner.getName(id)})"
					}

					player.showTitle(Title.title(text("Entered Territory", GOLD), text(subtitle, BLUE), times(ofMillis(1000), ofMillis(2000), ofMillis(1000))))
				}
			}
		}

		territory?.children?.firstOrNull { region ->
			region is RegionSettlementZone && region.contains(event.to.blockX, event.to.blockY, event.to.blockZ)
		}.also { zone: Region<*>? ->
			zone as RegionSettlementZone?

			val oldZone = lastPlayerZones[uuid]

			if (oldZone != zone?.id) {
				lastPlayerZones[uuid] = zone?.id

				if (zone != null) {
					player.information("Entered zone ${zone.name}")
				} else {
					oldZone?.let { Regions.get<RegionSettlementZone>(it) }?.let {
						player.information("Exited zone ${it.name}")
					}
				}
			}
		}
	}

	@EventHandler
	fun onQuit(event: PlayerQuitEvent) {
		lastMoved.remove(event.player.uniqueId)
		lastPlayerTerritories.remove(event.player.uniqueId)
	}
}
