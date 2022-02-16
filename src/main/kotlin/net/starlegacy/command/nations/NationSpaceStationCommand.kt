package net.starlegacy.command.nations

import co.aikar.commands.InvalidCommandArgument
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.Description
import co.aikar.commands.annotation.Optional
import co.aikar.commands.annotation.Subcommand
import kotlin.math.roundToInt
import net.starlegacy.command.SLCommand
import net.starlegacy.database.Oid
import net.starlegacy.database.schema.misc.SLPlayerId
import net.starlegacy.database.schema.nations.CapturableStation
import net.starlegacy.database.schema.nations.Nation
import net.starlegacy.database.schema.nations.SpaceStation
import net.starlegacy.database.slPlayerId
import net.starlegacy.database.uuid
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
import net.starlegacy.util.msg
import net.starlegacy.util.squared
import net.starlegacy.util.toCreditsString
import org.bukkit.World
import org.bukkit.entity.Player
import org.litote.kmongo.addToSet
import org.litote.kmongo.eq
import org.litote.kmongo.pull

@CommandAlias("nationspacestation|nspacestation|nstation")
object NationSpaceStationCommand : SLCommand() {
	private fun validateName(name: String, stationId: Oid<CapturableStation>?) {
		if (!name.isAlphanumeric()) {
			throw InvalidCommandArgument("Name must be alphanumeric")
		}

		if (name.length < 3) {
			throw InvalidCommandArgument("Name cannot be less than 3 characters")
		}

		if (name.length > 40) {
			throw InvalidCommandArgument("Name cannot be more than 40 characters")
		}

		if (SpaceStation.all().any { it.name.equals(name, ignoreCase = true) && it._id != stationId }) {
			throw InvalidCommandArgument("A space station named $name already exists")
		}
	}

	private fun checkDimensions(world: World, x: Int, z: Int, radius: Int, id: Oid<SpaceStation>?) {
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

		// Check conflicts with other stations
		// (use the database directly, in order to avoid people making
		// another one in the same location before the cache updates)
		for (other in SpaceStation.all()) {
			if (other._id == id) {
				continue
			}

			val minDistance = other.radius + radius
			val distance = distance(x, y, z, other.x, y, other.z)

			failIf(distance < minDistance) {
				"This claim would be too close to the space station ${other.name}"
			}
		}

		// Check conflicts with capturable stations
		for (station in Regions.getAllOf<RegionCapturableStation>().filter { it.bukkitWorld == world }) {
			val minDistance = NATIONS_BALANCE.capturableStation.radius + radius
			val distance = distance(x, y, z, station.x, y, station.z)

			failIf(distance < minDistance) {
				"This claim would be too close to the capturable station ${station.name}"
			}
		}

		// asteroid field world
		if (world.name == "Andromeda10d") {
			val distance = distance(x, y, z, 5000, 128, 5000)
			failIf(distance <= 5000) { "Cannot claim within 5000 blocks of the Blackular System" }
		}
	}

	private fun calculateCost(oldRadius: Int, newRadius: Int): Int {
		/*  A_1 = pi * r^2
				A_2 = pi * r_f^2
				dA = A_2-A_1
				dA = pi * r_f^2 - pi * r^2
				dA = pi * (r_f^2 - r^2) */
		val deltaArea = Math.PI * (newRadius.squared() - oldRadius.squared())
		return (deltaArea * NATIONS_BALANCE.nation.costPerSpaceStationBlock).roundToInt()
	}

	@Subcommand("create")
	@Description("Claim this area of space as a space station")
	fun onCreate(sender: Player, name: String, radius: Int, @Optional cost: Int?) = asyncCommand(sender) {
		failIf(!sender.hasPermission("nations.spacestation.create")) {
			"You can't create space stations here!"
		}

		failIf(!SpaceWorlds.contains(sender.world)) { "You can only create a space station in space" }

		val nation: Oid<Nation> = requireNationIn(sender)
		requireNationLeader(sender, nation)

		validateName(name, stationId = null)

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

		SpaceStation.create(nation, name, world.name, x, z, radius)
		VAULT_ECO.withdrawPlayer(sender, realCost.toDouble())
		Notify.all("&d${getNationName(nation)} &7established space station &b$name")
	}

	private fun requireStation(nation: Oid<Nation>, name: String) = SpaceStation.find(SpaceStation::nation eq nation)
		.firstOrNull { it.name.equals(name, true) }
		?: fail { "Your nation doesn't own a station named $name" }

	private fun requireManagementContext(sender: Player, name: String): Pair<Oid<Nation>, SpaceStation> {
		val nation: Oid<Nation> = requireNationIn(sender)
		val spaceStation: SpaceStation = requireStation(nation, name)
		if (!spaceStation.managers.contains(sender.slPlayerId)) {
			requireNationLeader(sender, nation)
		}
		return nation to spaceStation
	}

	@Subcommand("abandon")
	@Description("Delete a space station")
	fun onAbandon(sender: Player, station: String) = asyncCommand(sender) {
		val (nation, spaceStation) = requireManagementContext(sender, station)
		requireNationLeader(sender, nation) // also require that they're the leader
		SpaceStation.delete(spaceStation._id)
		Notify.all("&d${getNationName(nation)} &7abandoned space station &b$station")
	}

	@Subcommand("resize")
	@Description("Resize the station")
	fun onResize(sender: Player, station: String, newRadius: Int, @Optional cost: Int?) {
		val (nation, spaceStation) = requireManagementContext(sender, station)
		requireNationLeader(sender, nation) // also require that they're the leader
		val stationName = spaceStation.name

		val location = sender.location
		val world = location.world
		val x = location.blockX
		val z = location.blockZ
		checkDimensions(world, x, z, newRadius, spaceStation._id)

		val realCost = calculateCost(spaceStation.radius, newRadius)
		requireMoney(sender, realCost, "create a space station")

		failIf(cost != realCost) {
			"You must acknowledge the cost of resizing a space station to resize one. " +
				"The cost is ${realCost.toCreditsString()}. Run the command: " +
				"/nstation resize $name $newRadius $realCost"
		}

		SpaceStation.updateById(spaceStation._id, org.litote.kmongo.setValue(SpaceStation::radius, newRadius))
		VAULT_ECO.withdrawPlayer(sender, realCost.toDouble())
		sender msg "&7Resized &b$stationName&7 to &b$newRadius"
	}

	@Subcommand("set trustlevel")
	@CommandCompletion("MANUAL|NATION|ALLY")
	@Description("Change the setting for who automatically can build in the station")
	fun onSetTrustLevel(sender: Player, station: String, trustLevel: SpaceStation.TrustLevel) {
		val (_, spaceStation) = requireManagementContext(sender, station)
		val stationName = spaceStation.name
		failIf(spaceStation.trustLevel == trustLevel) { "$stationName's trust level is already $trustLevel" }
		SpaceStation.updateById(spaceStation._id, org.litote.kmongo.setValue(SpaceStation::trustLevel, trustLevel))
		sender msg "&7Set trust level of &b$stationName&7 to &b$trustLevel"
	}

	@Subcommand("manager add")
	fun onManagerAdd(sender: Player, station: String, player: String) {
		val (nation, spaceStation) = requireManagementContext(sender, station)
		requireNationLeader(sender, nation) // also require that they're the leader
		val stationName = spaceStation.name
		val playerId: SLPlayerId = resolveOfflinePlayer(player).slPlayerId
		val playerName: String = getPlayerName(playerId)

		failIf(spaceStation.managers.contains(playerId)) {
			"$playerName is already a manager of $stationName"
		}

		SpaceStation.updateById(spaceStation._id, addToSet(SpaceStation::managers, playerId))
		sender msg "&7Made &b$playerName&7 a manager of &b$stationName"
		Notify.player(playerId.uuid, "&7You were made a manager of station &b$stationName&7 by &b${sender.name}")
	}

	@Subcommand("manager list")
	fun onManagerList(sender: Player, station: String) {
		val (nation, spaceStation) = requireManagementContext(sender, station)
		requireNationLeader(sender, nation) // also require that they're the leader
		val stationName: String = spaceStation.name
		val managers: String = spaceStation.managers.map(::getPlayerName).sorted().joinToString()
		sender msg "&7Managers in $stationName:&c $managers"
	}

	@Subcommand("manager remove")
	@Description("Revoke a player's manager status at the station")
	fun onManagerRemove(sender: Player, station: String, player: String) = asyncCommand(sender) {
		val (nation, spaceStation) = requireManagementContext(sender, station)
		requireNationLeader(sender, nation) // also require that they're the leader
		val stationName = spaceStation.name
		val playerId: SLPlayerId = resolveOfflinePlayer(player).slPlayerId
		val playerName: String = getPlayerName(playerId)

		failIf(!spaceStation.managers.contains(playerId)) {
			"$playerName is not a manager of $stationName"
		}

		SpaceStation.updateById(spaceStation._id, pull(SpaceStation::managers, playerId))
		sender msg "&7Removed &b$playerName&7 as a manager of &b$stationName"
		Notify.player(playerId.uuid, "&7You were removed as a manager of &b$stationName&7 by &b${sender.name}")
	}

	@Subcommand("trusted list")
	fun onTrustedList(sender: Player, station: String) {
		val (_, spaceStation) = requireManagementContext(sender, station)
		val stationName: String = spaceStation.name
		val trustedPlayers: String = spaceStation.trustedPlayers.map(::getPlayerName).sorted().joinToString()
		sender msg "&7Trusted players in $stationName:&b $trustedPlayers"
		val trustedNations: String = spaceStation.trustedNations.map(::getNationName).sorted().joinToString()
		sender msg "&7Trusted nations in $stationName:&b $trustedNations"
	}

	@Subcommand("trusted add player")
	@Description("Give a player build access to the station")
	fun onTrustedAddPlayer(sender: Player, station: String, player: String) = asyncCommand(sender) {
		val (_, spaceStation) = requireManagementContext(sender, station)
		val stationName = spaceStation.name
		val playerId: SLPlayerId = resolveOfflinePlayer(player).slPlayerId
		val playerName: String = getPlayerName(playerId)

		failIf(spaceStation.trustedPlayers.contains(playerId)) {
			"$playerName is already trusted in $stationName"
		}

		SpaceStation.updateById(spaceStation._id, addToSet(SpaceStation::trustedPlayers, playerId))
		sender msg "&7Added &b$playerName&7 to &b$stationName"
		Notify.player(playerId.uuid, "&7You were added to station &b$stationName&7 by &b${sender.name}")
	}

	@Subcommand("trusted remove player")
	@Description("Revoke a player's build access to the station")
	fun onTrustedRemovePlayer(sender: Player, station: String, player: String) = asyncCommand(sender) {
		val (_, spaceStation) = requireManagementContext(sender, station)
		val stationName = spaceStation.name
		val playerId: SLPlayerId = resolveOfflinePlayer(player).slPlayerId
		val playerName: String = getPlayerName(playerId)

		failIf(!spaceStation.trustedPlayers.contains(playerId)) {
			"$playerName is not trusted in $stationName"
		}

		SpaceStation.updateById(spaceStation._id, pull(SpaceStation::trustedPlayers, playerId))
		sender msg "&7Removed &b$playerName&7 from &b$stationName"
		Notify.player(playerId.uuid, "&7You were removed from station &b$stationName&7 by &b${sender.name}")
	}

	@Subcommand("trusted add nation")
	@Description("Give a nation build access to the station")
	fun onTrustedAddNation(sender: Player, station: String, nation: String) = asyncCommand(sender) {
		val (_, spaceStation) = requireManagementContext(sender, station)
		val stationName = spaceStation.name
		val nationId: Oid<Nation> = resolveNation(nation)
		val nationName: String = getNationName(nationId)

		failIf(spaceStation.trustedNations.contains(nationId)) {
			"$nationName is already a trusted nation in $stationName"
		}

		SpaceStation.updateById(spaceStation._id, addToSet(SpaceStation::trustedNations, nationId))
		sender msg "&7Added nation &b$nationName&7 to &b$stationName"
		Notify.nation(nationId, "&7Your nation was added to station &b$stationName&7 by &b${sender.name}")
	}

	@Subcommand("trusted remove nation")
	@Description("Revoke a nation's build access to the station")
	fun onTrustedRemoveNation(sender: Player, station: String, nation: String) = asyncCommand(sender) {
		val (_, spaceStation) = requireManagementContext(sender, station)
		val stationName = spaceStation.name
		val nationId: Oid<Nation> = resolveNation(nation)
		val nationName: String = getNationName(nationId)

		failIf(!spaceStation.trustedNations.contains(nationId)) {
			"$nationName is not a trusted nation in $stationName"
		}

		SpaceStation.updateById(spaceStation._id, pull(SpaceStation::trustedNations, nationId))
		sender msg "&7Removed nation &b$nationName&7 from &b$stationName"
		Notify.nation(nationId, "&7Your nation was removed from station &b$stationName&7 by &b${sender.name}")
	}
}
