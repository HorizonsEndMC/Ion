package net.horizonsend.ion.server.miscellaneous

import net.horizonsend.ion.common.Redis
import net.horizonsend.ion.common.database.cache.nations.NationCache
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.datasync.*
import net.horizonsend.ion.common.utils.DBVec3i
import net.horizonsend.ion.common.utils.redisaction.RedisActions
import net.horizonsend.ion.server.features.nations.StationSieges
import net.horizonsend.ion.server.features.nations.region.Regions
import net.horizonsend.ion.server.features.nations.region.types.RegionCapturableStation
import net.horizonsend.ion.server.features.starship.PilotedStarships
import net.horizonsend.ion.server.features.starship.active.ActivePlayerStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.miscellaneous.utils.toCreditsString
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import java.util.*

fun provideData() = SyncData(
	onlinePlayers = Bukkit.getOnlinePlayers().map { it.name },
	listShips = ActiveStarships.all().map {
		var worldName = it.serverLevel.world.key.toString().substringAfterLast(":")
			.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

		if (worldName == "Overworld") {
			worldName = it.serverLevel.world.name
				.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
		}

		Ship(
			(it as? ActivePlayerStarship)?.data?.let { PilotedStarships.getDisplayName(it) } ?: it.type.displayName,
			it.controller?.name,
			it.type.displayName,
			it.initialBlockCount,
			worldName
		)
	},

	currentStation = with(StationSieges.getStationsNow().first()) {
		Station(
			name = name,
			location = DBVec3i(x, -1, z),
			world = world,
			owner = nation?.let { NationCache[it].name },
			time = siegeHour
		)
	},

	allStations = Regions.getAllOf<RegionCapturableStation>().map {
		Station(
			name = it.name,
			location = DBVec3i(it.x, -1, it.z),
			world = it.world,
			owner = it.nation?.let { NationCache[it].name },
			time = it.siegeHour
		)
	},

	bounties = SLPlayer.all()
		.toList()
		.sortedByDescending { it.bounty }
		.map { player ->
			Bounty(
				target = player.lastKnownName,
				bounty = player.bounty
			)
		},

	tps = Bukkit.getTPS()[0]
)

fun sendApiEvent(id: SurvivalEvents, e: Any) {
	RedisActions.publish("survival-events", SurvivalEvent(id, e), SurvivalEvent::class.java)
}
