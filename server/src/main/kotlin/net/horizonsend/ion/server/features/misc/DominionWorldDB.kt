package net.horizonsend.ion.server.features.misc

import net.horizonsend.ion.common.database.schema.nations.DominionTerritory
import net.horizonsend.ion.common.database.schema.nations.NPCTerritoryOwner
import net.horizonsend.ion.common.database.schema.nations.Territory
import net.horizonsend.ion.common.database.schema.nations.TradeWorldTerritory
import net.horizonsend.ion.server.core.IonServerComponent
import net.horizonsend.ion.server.features.world.IonWorld
import net.horizonsend.ion.server.features.world.WorldFlag

object DominionWorldDB : IonServerComponent() {
	fun onStartup() {
		for (ionWorld in IonWorld.all()) {
			if (!ionWorld.hasFlag(WorldFlag.DOMINION_WORLD)) continue

			val worldName = ionWorld.world.name
			if (DominionTerritory.findByWorld(worldName) == null) {
				DominionTerritory.create(worldName, worldName)
			}
		}
	}
}
