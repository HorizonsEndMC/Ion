package net.starlegacy.command.nations

import co.aikar.commands.InvalidCommandArgument
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.Description
import co.aikar.commands.annotation.Optional
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.server.database.DbObject
import net.horizonsend.ion.server.database.schema.nations.Nation
import net.horizonsend.ion.server.features.HyperspaceBeaconManager
import net.kyori.adventure.text.minimessage.MiniMessage
import net.starlegacy.command.SLCommand
import net.horizonsend.ion.server.database.Oid
import net.horizonsend.ion.server.database.schema.misc.SLPlayerId
import net.horizonsend.ion.server.database.schema.nations.Settlement
import net.horizonsend.ion.server.database.schema.nations.spacestation.NationSpaceStation
import net.horizonsend.ion.server.database.schema.nations.spacestation.PlayerSpaceStation
import net.horizonsend.ion.server.database.schema.nations.spacestation.SettlementSpaceStation
import net.horizonsend.ion.server.database.schema.nations.spacestation.SpaceStationCompanion
import net.horizonsend.ion.server.database.slPlayerId
import net.horizonsend.ion.server.database.uuid
import net.horizonsend.ion.server.features.spacestations.CachedSpaceStation
import net.horizonsend.ion.server.features.spacestations.CachedSpaceStation.Companion.calculateCost
import net.horizonsend.ion.server.features.spacestations.SpaceStations
import net.starlegacy.cache.nations.NationCache
import net.starlegacy.cache.nations.SettlementCache
import net.starlegacy.feature.nations.NATIONS_BALANCE
import net.starlegacy.feature.nations.region.Regions
import net.starlegacy.feature.nations.region.types.RegionCapturableStation
import net.starlegacy.feature.space.CachedPlanet
import net.starlegacy.feature.space.CachedStar
import net.starlegacy.feature.space.Space
import net.starlegacy.feature.space.SpaceWorlds
import net.starlegacy.util.Notify
import net.starlegacy.util.VAULT_ECO
import net.starlegacy.util.distance
import net.starlegacy.util.isAlphanumeric
import net.starlegacy.util.toCreditsString
import org.bukkit.World
import org.bukkit.entity.Player
import org.litote.kmongo.Id

@CommandAlias("nationspacestation|nspacestation|nstation|sstation|station|spacestation")
object SpaceStationCommand : SLCommand() {
	val disallowedWorlds = listOf("horizon", "trench", "au0821")

	private fun validateName(name: String) {
		if (!name.isAlphanumeric()) {
			throw InvalidCommandArgument("Name must be alphanumeric")
		}

		if (name.length < 3) {
			throw InvalidCommandArgument("Name cannot be less than 3 characters")
		}

		if (name.length > 40) {
			throw InvalidCommandArgument("Name cannot be more than 40 characters")
		}

		if (NationSpaceStation.all().any { it.name.equals(name, ignoreCase = true) }) {
			throw InvalidCommandArgument("A space station named $name already exists")
		}
	}

	private fun checkDimensions(world: World, x: Int, z: Int, radius: Int, cachedStation: CachedSpaceStation<*, *, *>?) {
		failIf(radius !in 15..10_000) { "Radius must be at least 15 and at most 10,000 blocks" }

		val y = 128 // we don't care about comparing height here

		// Check conflicts with planet orbits
		for (planet: CachedPlanet in Space.getPlanets().filter { it.spaceWorld == world }) {
			val padding = 130
			val minDistance = planet.orbitDistance - padding - radius
			val maxDistance = planet.orbitDistance + padding + radius
			val distance = distance(x, y, z, planet.sun.location.x, y, planet.sun.location.z).toInt()

			failIf(distance in minDistance..maxDistance) {
				"This claim would be in the way of ${planet.name}'s orbit"
			}
		}

		// Check conflict with stars
		for (star: CachedStar in Space.getStars().filter { it.spaceWorld == world }) {
			val minDistance = 256
			val distance = distance(x, y, z, star.location.x, y, star.location.z)

			failIf(distance < minDistance) {
				"This claim would be too close to the star ${star.name}"
			}
		}

		// Check conflict with beacons
		HyperspaceBeaconManager.beaconWorlds[world]?.let { beacons ->
			for (beacon in beacons) {
				val minDistance = 3000
				val distance = distance(x, y, z, beacon.spaceLocation.x, y, beacon.spaceLocation.z)

				failIf(distance < minDistance) {
					"This claim would be too close to the hyperspace beacon ${beacon.name}"
				}
			}
		}

		// Check conflicts with other stations
		// (use the database directly, in order to avoid people making
		// another one in the same location before the cache updates)
		for (other in SpaceStations.all()) {
			if (other.databaseId == cachedStation?.databaseId) continue

			val minDistance = other.radius + radius
			val distance = distance(x, y, z, other.x, y, other.z)

			failIf(distance < minDistance) {
				"This claim would be too close to the space station ${other.name}"
			}
		}

		// Check conflicts with capturable stations
		for (station in Regions.getAllOf<RegionCapturableStation>().filter { it.bukkitWorld == world }) {
			val minDistance = maxOf((NATIONS_BALANCE.capturableStation.radius + radius), 2500)
			val distance = distance(x, y, z, station.x, y, station.z)

			failIf(distance < minDistance) {
				"This claim would be too close to the capturable station ${station.name}"
			}
		}
	}

	@Subcommand("create nation")
	@Suppress("unused")
	fun createNation(sender: Player, name: String, radius: Int, @Optional cost: Int?) {
		val nation: Oid<Nation> = requireNationIn(sender)
		requireNationPermission(sender, nation, SpaceStations.SpaceStationPermission.CREATE_STATION.nation)

		create(sender, name, radius, cost, nation, NationSpaceStation.Companion)
	}

	@Subcommand("create settlement")
	@Suppress("unused")
	fun createSettlement(sender: Player, name: String, radius: Int, @Optional cost: Int?) {
		val nation: Oid<Settlement> = requireSettlementIn(sender)
		requireSettlementPermission(sender, nation, SpaceStations.SpaceStationPermission.CREATE_STATION.settlement)

		create(sender, name, radius, cost, nation, SettlementSpaceStation.Companion)
	}

	@Subcommand("create personal")
	@Suppress("unused")
	fun createPersonal(sender: Player, name: String, radius: Int, @Optional cost: Int?) {
		create(sender, name, radius, cost, sender.slPlayerId, PlayerSpaceStation.Companion)
	}

	// Check settlement / nation permissions in their own version
	fun <Owner: DbObject>create(
		sender: Player,
		name: String,
		radius: Int,
		@Optional cost: Int?,
		owner: Id<Owner>,
		companion: SpaceStationCompanion<Owner, *>)
	{
		failIf(!sender.hasPermission("nations.spacestation.create")) {
			"You can't create space stations here!"
		}

		failIf(!SpaceWorlds.contains(sender.world)) { "You can only create a space station in space" }
		failIf(
			disallowedWorlds.contains(sender.world.name.lowercase())
		) { "Space stations cannot be created in systems with no security" }

		validateName(name)

		val location = sender.location
		val world = location.world
		val x = location.blockX
		val z = location.blockZ
		checkDimensions(world, x, z, radius, null)

		val realCost = calculateCost(0, radius)
		requireMoney(sender, realCost, "create a space station")

		failIf(cost != realCost) {
			"You must acknowledge the cost of creating a space station to create one. " +
				"The cost is ${realCost.toCreditsString()}. Run the command: " +
				"/nstation $name $radius $realCost"
		}

		companion.create(
			owner,
			name,
			world.name,
			x,
			z,
			radius,
			SpaceStations.TrustLevel.MANUAL
		)
	}

	private fun requireStation(player: SLPlayerId, name: String): CachedSpaceStation<*, *, *> {
		val station = SpaceStations.all().firstOrNull {
			it.name.equals(name, ignoreCase = true)
		} ?: fail { "Space Station $name doesn't exist!" }

		if (!station.hasOwnershipContext(player)) fail { "Your ${station.ownershipType} doesn't own $name" }

		return station
	}

	private fun requirePermission(
		player: SLPlayerId,
		station: CachedSpaceStation<*, *, *>,
		permission: SpaceStations.SpaceStationPermission
	) {
		if (!station.hasPermission(player, permission)) fail { "You don't have permission $permission" }
	}

	@Subcommand("abandon")
	@Description("Delete a space station")
	@Suppress("unused")
	fun onAbandon(sender: Player, station: String) = asyncCommand(sender) {
		val cachedStation = requireStation(sender.slPlayerId, station)
		requirePermission(sender.slPlayerId, cachedStation, SpaceStations.SpaceStationPermission.DELETE_STATION)

		cachedStation.abandon()

		Notify.all(
			MiniMessage.miniMessage().deserialize(
				"<gray>${cachedStation.ownershipType} <light_purple>${cachedStation.ownerName} <gray>abandoned space station <aqua>$station"
			)
		)
	}

	@Subcommand("resize")
	@Description("Resize the station")
	@Suppress("unused")
	fun onResize(sender: Player, station: String, newRadius: Int, @Optional cost: Int?) {
		val spaceStation = requireStation(sender.slPlayerId, station)
		requirePermission(sender.slPlayerId, spaceStation, SpaceStations.SpaceStationPermission.MANAGE_STATION)
		val stationName = spaceStation.name

		val location = sender.location
		val world = location.world
		val x = location.blockX
		val z = location.blockZ
		checkDimensions(world, x, z, newRadius, spaceStation)

		val realCost = calculateCost(spaceStation.radius, newRadius)
		requireMoney(sender, realCost, "create a space station")

		failIf(cost != realCost) {
			"You must acknowledge the cost of resizing a space station to resize one. " +
				"The cost is ${realCost.toCreditsString()}. Run the command: " +
				"/nstation resize $name $newRadius $realCost"
		}

		spaceStation.changeRadius(newRadius)
		spaceStation.invalidate()

		VAULT_ECO.withdrawPlayer(sender, realCost.toDouble())
		sender.sendRichMessage("<gray>7Resized <aqua>$stationName<gray> to <aqua>$newRadius")
	}

	@Subcommand("set trustlevel")
	@CommandCompletion("MANUAL|NATION|ALLY")
	@Description("Change the setting for who automatically can build in the station")
	@Suppress("unused")
	fun onSetTrustLevel(sender: Player, station: String, trustLevel: SpaceStations.TrustLevel) {
		val spaceStation = requireStation(sender.slPlayerId, station)
		val stationName = spaceStation.name
		failIf(spaceStation.trustLevel == trustLevel) { "$stationName's trust level is already $trustLevel" }

		spaceStation.changeTrustLevel(trustLevel)
		spaceStation.invalidate()

		sender.sendRichMessage("<gray>Set trust level of <aqua>$stationName<gray> to <aqua>$trustLevel")
	}

	@Subcommand("trusted list")
	@Suppress("unused")
	fun onTrustedList(sender: Player, station: String) {
		val spaceStation = requireStation(sender.slPlayerId, station)
		val stationName: String = spaceStation.name

		val trustedPlayers: String = spaceStation.trustedPlayers.map(::getPlayerName).sorted().joinToString()
		sender.sendRichMessage("<Gold>Trusted players in $stationName: <aqua>$trustedPlayers")

		val trustedSettlements: String = spaceStation.trustedSettlements.map(::getSettlementName).sorted().joinToString()
		sender.sendRichMessage("<Gold>Trusted settlements in $stationName: <aqua>$trustedSettlements")

		val trustedNations: String = spaceStation.trustedNations.map(::getNationName).sorted().joinToString()
		sender.sendRichMessage("<Gold>Trusted nations in $stationName: <aqua>$trustedNations")
	}

	@Subcommand("trusted add player")
	@Description("Give a player build access to the station")
	@Suppress("unused")
	fun onTrustedAddPlayer(sender: Player, station: String, player: String) = asyncCommand(sender) {
		val spaceStation = requireStation(sender.slPlayerId, station)
		val stationName = spaceStation.name

		val playerId: SLPlayerId = resolveOfflinePlayer(player).slPlayerId
		val playerName: String = getPlayerName(playerId)

		requirePermission(sender.slPlayerId, spaceStation, SpaceStations.SpaceStationPermission.MANAGE_STATION)

		failIf(spaceStation.trustedPlayers.contains(playerId)) {
			"$playerName is already trusted in $stationName"
		}

		spaceStation.trustPlayer(playerId)
		spaceStation.invalidate()

		sender.sendRichMessage("<gray> Added <aqua>$playerName<gray> to <aqua>$stationName")
		Notify.player(playerId.uuid, MiniMessage.miniMessage().deserialize("<gray>You were added to station <aqua>$stationName<gray> by <aqua>${sender.name}"))
	}

	@Subcommand("trusted add settlement")
	@Description("Give a settlement build access to the station")
	@Suppress("unused")
	fun onTrustedAddSettlement(sender: Player, station: String, settlement: String) = asyncCommand(sender) {
		val spaceStation = requireStation(sender.slPlayerId, station)
		val stationName = spaceStation.name

		val settlementId: Oid<Settlement> = SettlementCache.getByName(settlement) ?: fail {
			"Settlement $settlement not found"
		}

		requirePermission(sender.slPlayerId, spaceStation, SpaceStations.SpaceStationPermission.MANAGE_STATION)

		failIf(spaceStation.trustedSettlements.contains(settlementId)) {
			"$settlement is already trusted in $stationName"
		}

		spaceStation.trustSettlement(settlementId)
		spaceStation.invalidate()

		sender.sendRichMessage("<gray> Added <aqua>$settlement<gray> to <aqua>$stationName")
		Notify.settlement(settlementId, MiniMessage.miniMessage().deserialize("<gray>Your settlement was added to station <aqua>$stationName<gray> by <aqua>${sender.name}"))
	}

	@Subcommand("trusted add nation")
	@Description("Give a nation build access to the station")
	@Suppress("unused")
	fun onTrustedAddNation(sender: Player, station: String, nation: String) = asyncCommand(sender) {
		val spaceStation = requireStation(sender.slPlayerId, station)
		val stationName = spaceStation.name

		val nationId: Oid<Nation> = NationCache.getByName(nation) ?: fail {
			"Settlement $nation not found"
		}

		requirePermission(sender.slPlayerId, spaceStation, SpaceStations.SpaceStationPermission.MANAGE_STATION)

		failIf(spaceStation.trustedNations.contains(nationId)) {
			"$nation is already trusted in $stationName"
		}

		spaceStation.trustNation(nationId)
		spaceStation.invalidate()

		sender.sendRichMessage("<gray> Added <aqua>$nation<gray> to <aqua>$stationName")
		Notify.nation(nationId, MiniMessage.miniMessage().deserialize("<gray>Your settlement was added to station <aqua>$stationName<gray> by <aqua>${sender.name}"))
	}

	@Subcommand("trusted remove player")
	@Description("Revoke a player's build access to the station")
	@Suppress("unused")
	fun onTrustedRemovePlayer(sender: Player, station: String, player: String) = asyncCommand(sender) {
		val spaceStation = requireStation(sender.slPlayerId, station)
		val stationName = spaceStation.name

		val playerId: SLPlayerId = resolveOfflinePlayer(player).slPlayerId
		val playerName: String = getPlayerName(playerId)

		requirePermission(sender.slPlayerId, spaceStation, SpaceStations.SpaceStationPermission.MANAGE_STATION)

		failIf(spaceStation.trustedPlayers.contains(playerId)) {
			"$playerName is already trusted in $stationName"
		}

		spaceStation.unTrustPlayer(playerId)
		spaceStation.invalidate()

		sender.sendRichMessage("<gray> Removed <aqua>$playerName<gray> from <aqua>$stationName")
		Notify.player(playerId.uuid, MiniMessage.miniMessage().deserialize("<gray>You were removed from station <aqua>$stationName<gray> by <aqua>${sender.name}"))
	}


	@Subcommand("trusted add settlement")
	@Description("Give a settlement build access to the station")
	@Suppress("unused")
	fun onTrustedRemoveSettlement(sender: Player, station: String, settlement: String) = asyncCommand(sender) {
		val spaceStation = requireStation(sender.slPlayerId, station)
		val stationName = spaceStation.name

		val settlementId: Oid<Settlement> = SettlementCache.getByName(settlement) ?: fail {
			"Settlement $settlement not found"
		}

		requirePermission(sender.slPlayerId, spaceStation, SpaceStations.SpaceStationPermission.MANAGE_STATION)

		failIf(!spaceStation.trustedSettlements.contains(settlementId)) {
			"$settlement isn't trusted in $stationName"
		}

		spaceStation.unTrustSettlement(settlementId)
		spaceStation.invalidate()

		sender.sendRichMessage("<gray> Removed <aqua>$settlement<gray> from <aqua>$stationName")
		Notify.settlement(settlementId, MiniMessage.miniMessage().deserialize("<gray>Your settlement was removed from station <aqua>$stationName<gray> by <aqua>${sender.name}"))
	}

	@Subcommand("trusted add nation")
	@Description("Give a nation build access to the station")
	@Suppress("unused")
	fun onTrustedRemoveNation(sender: Player, station: String, nation: String) = asyncCommand(sender) {
		val spaceStation = requireStation(sender.slPlayerId, station)
		val stationName = spaceStation.name

		val nationId: Oid<Nation> = NationCache.getByName(nation) ?: fail {
			"Nation $nation not found"
		}

		requirePermission(sender.slPlayerId, spaceStation, SpaceStations.SpaceStationPermission.MANAGE_STATION)

		failIf(!spaceStation.trustedNations.contains(nationId)) {
			"$nation isn't trusted in $stationName"
		}

		spaceStation.unTrustNation(nationId)
		spaceStation.invalidate()

		sender.sendRichMessage("<gray> Added <aqua>$nation<gray> to <aqua>$stationName")
		Notify.nation(nationId, MiniMessage.miniMessage().deserialize("<gray>Your nation was removed from station <aqua>$stationName<gray> by <aqua>${sender.name}"))
	}

	@Subcommand("set name")
	@Description("Rename the station")
	@Suppress("unused")
	fun onRename(sender: Player, station: String, newName: String) = asyncCommand(sender) {
		val spaceStation = requireStation(sender.slPlayerId, station)

		validateName(newName)

		spaceStation.rename(newName)
		spaceStation.invalidate()
	}
}

