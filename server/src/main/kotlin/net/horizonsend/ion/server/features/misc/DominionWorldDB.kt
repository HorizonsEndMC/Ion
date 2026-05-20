package net.horizonsend.ion.server.features.misc

import net.horizonsend.ion.common.database.schema.nations.DominionTerritory
import net.horizonsend.ion.common.database.schema.nations.NPCTerritoryOwner
import net.horizonsend.ion.common.database.schema.nations.Territory
import net.horizonsend.ion.common.database.schema.nations.TradeWorldTerritory
import net.horizonsend.ion.server.core.IonServerComponent
import net.horizonsend.ion.server.features.world.IonWorld
import net.horizonsend.ion.server.features.world.WorldFlag

object DominionWorldDB: IonServerComponent() {
	fun onStartup() {
		for (ionWorld in IonWorld.all()) {
			if (!ionWorld.hasFlag(WorldFlag.DOMINION_WORLD)) continue

			val worldName = ionWorld.world.name
			if (DominionTerritory.findByWorld(worldName) == null) {
				DominionTerritory.create(worldName, worldName)
			}

			if (ionWorld.hasFlag(WorldFlag.DOMINION_TRADE_WORLD)) {
				if (TradeWorldTerritory.findByWorld(worldName) == null) {
					// Create backing Territory with empty polygon
					val territoryId = Territory.create(worldName, worldName, ByteArray(0))
					// Create NPCTerritoryOwner so TradeCities picks it up
					NPCTerritoryOwner.create(territoryId, worldName, 0xFFFFFF, true)
					// Create TradeWorldTerritory linking everything
					TradeWorldTerritory.create(worldName, worldName, 0xFFFFFF, territoryId)
				}
			}
		}
	}
}
