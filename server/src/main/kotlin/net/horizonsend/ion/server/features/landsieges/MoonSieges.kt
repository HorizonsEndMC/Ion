package net.horizonsend.ion.server.features.landsieges

import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.database.Oid
import net.horizonsend.ion.server.database.schema.misc.SLPlayerId
import net.horizonsend.ion.server.database.schema.nations.CapturableStation
import net.horizonsend.ion.server.database.schema.nations.NationRelation
import net.horizonsend.ion.server.database.schema.nations.landsieges.SiegeTerritory
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.starlegacy.SLComponent
import net.starlegacy.cache.nations.PlayerCache
import net.starlegacy.feature.nations.region.Regions
import net.starlegacy.feature.nations.region.types.RegionSiegeTerritory
import net.starlegacy.util.Notify
import net.starlegacy.util.Tasks
import org.bukkit.entity.Player
import java.time.DayOfWeek
import java.time.ZonedDateTime

object MoonSieges : SLComponent() {
	var siegeActive: Boolean = false

	val siegeDays = arrayOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)

	data class Siege(val siegerId: SLPlayerId, val territoryId: Oid<SiegeTerritory>, val start: Long)

	override fun onEnable() {
		// Update siges on the main server only
		if (IonServer.configuration.serverName != "Survival") return

		Tasks.syncRepeat(20, 20) {
			updateSieges()
			updateSiegePeriod()
		}
	}

	fun updateSiegePeriod() {
		val dayOfTheWeek = ZonedDateTime.now().dayOfWeek

		if (!siegeDays.contains(dayOfTheWeek)) {
			siegeActive = false
			return
		}

		siegeActive = true

		val notification = Component.text().color(NamedTextColor.DARK_RED)
			.append(Component.text("The siege period has begun."))
			.append(Component.text("It will last for the next 48 hours", NamedTextColor.GRAY))
			.build()

		Notify.all(notification)
	}

	fun beginSiege(player: Player) {
		val nation = PlayerCache[player].nationOid
			?: return player.userError("You need to be in a nation to siege a station.")

		val siegeTerritory = Regions.findFirstOf<RegionSiegeTerritory>(player.location)
			?: return player.userError("You must be within a station's area to siege it.")

		if (siegeTerritory.nation?.let { NationRelation.getRelationActual(nation, it).ordinal >= 5 } == true) {
			return player.userError("This station is owned by an ally of your nation.")
		}


	}
}
