package net.starlegacy.command.starship

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Optional
import co.aikar.commands.bukkit.contexts.OnlinePlayer
import net.horizonsend.ion.server.legacy.feedback.FeedbackType
import net.horizonsend.ion.server.legacy.feedback.FeedbackType.ALERT
import net.horizonsend.ion.server.legacy.feedback.FeedbackType.INFORMATION
import net.horizonsend.ion.server.legacy.feedback.FeedbackType.SUCCESS
import net.horizonsend.ion.server.legacy.feedback.FeedbackType.USER_ERROR
import net.horizonsend.ion.server.legacy.feedback.sendFeedbackActionMessage
import net.horizonsend.ion.server.legacy.feedback.sendFeedbackMessage
import net.kyori.adventure.text.minimessage.MiniMessage
import net.starlegacy.cache.nations.PlayerCache
import net.starlegacy.command.SLCommand
import net.starlegacy.database.schema.nations.NationRelation
import net.starlegacy.database.schema.starships.Blueprint
import net.starlegacy.feature.space.Space
import net.starlegacy.feature.space.SpaceWorlds
import net.starlegacy.feature.starship.DeactivatedPlayerStarships
import net.starlegacy.feature.starship.PilotedStarships
import net.starlegacy.feature.starship.PilotedStarships.getDisplayName
import net.starlegacy.feature.starship.StarshipDestruction
import net.starlegacy.feature.starship.StarshipType
import net.starlegacy.feature.starship.active.ActivePlayerStarship
import net.starlegacy.feature.starship.active.ActiveStarships
import net.starlegacy.feature.starship.control.StarshipControl
import net.starlegacy.feature.starship.control.StarshipCruising
import net.starlegacy.feature.starship.hyperspace.Hyperspace
import net.starlegacy.feature.starship.hyperspace.MassShadows
import net.starlegacy.feature.starship.subsystem.HyperdriveSubsystem
import net.starlegacy.feature.starship.subsystem.NavCompSubsystem
import net.starlegacy.feature.starship.subsystem.weapon.interfaces.AutoWeaponSubsystem
import net.starlegacy.redis
import net.starlegacy.util.Vec3i
import net.starlegacy.util.distance
import net.starlegacy.util.normalize
import net.starlegacy.util.randomInt
import net.starlegacy.util.toVector
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import org.litote.kmongo.eq
import java.util.Locale
import java.util.concurrent.ThreadLocalRandom
import kotlin.collections.set
import kotlin.math.ln
import kotlin.math.roundToInt

object MiscStarshipCommands : SLCommand() {
	@Suppress("unused")
	@CommandAlias("release")
	fun onRelease(sender: Player) {
		DeactivatedPlayerStarships.deactivateAsync(getStarshipPiloting(sender)) {
			sender.sendFeedbackMessage(SUCCESS, "Released starship")
		}
	}

	@Suppress("unused")
	@CommandAlias("unpilot")
	fun onUnpilot(sender: Player) {
		val starship = getStarshipPiloting(sender)
		PilotedStarships.unpilot(starship, true)
		sender.sendFeedbackMessage(INFORMATION, "Unpiloted ship, but left it activated")
	}

	@Suppress("unused")
	@CommandAlias("stopriding")
	fun onStopRiding(sender: Player) {
		val starship = getStarshipRiding(sender)

		failIf(starship is ActivePlayerStarship && starship.pilot == sender) {
			"You can't stop riding if you're the pilot. Use /release or /unpilot."
		}

		starship.removePassenger(sender.uniqueId)
		sender.sendFeedbackMessage(SUCCESS, "Stopped riding ship")
	}

	@Suppress("unused")
	@CommandAlias("loadship")
	@CommandPermission("starships.loadship")
	@CommandCompletion("@players")
	fun onLoadShip(sender: Player, player: String, world: String) = asyncCommand(sender) {
		val uuid = resolveOfflinePlayer(player)
		redis {
			val key = "starships.lastpiloted.$uuid.${world.lowercase(Locale.getDefault())}"

			failIf(!exists(key)) { "$player doesn't have a ship saved for /loadship in $world" }

			val schematic = Blueprint.parseData(get(key))

			val pilotLoc = Vec3i(0, 0, 0)

			BlueprintCommand.checkObstruction(sender, schematic, pilotLoc)

			BlueprintCommand.loadSchematic(sender, schematic, pilotLoc)
		}
	}

	@Suppress("Unused")
	@CommandAlias("jump")
	fun onJump(sender: Player) {
		sender.sendFeedbackMessage(USER_ERROR, "/jump <planet> or /jump <x> <z>")
	}

	@Suppress("unused")
	@CommandAlias("jump")
	@CommandCompletion("x|z")
	fun onJump(sender: Player, xCoordinate: String, zCoordinate: String) {
		val starship: ActivePlayerStarship = getStarshipPiloting(sender)

		val navComp: NavCompSubsystem = Hyperspace.findNavComp(starship) ?: fail { "Intact nav computer not found!" }
		val maxRange: Int =
			(navComp.multiblock.baseRange * starship.data.starshipType.hyperspaceRangeMultiplier).roundToInt()

		val x = parseNumber(xCoordinate, starship.centerOfMass.x)
		val z = parseNumber(zCoordinate, starship.centerOfMass.z)

		tryJump(starship, x, z, maxRange, sender)
	}

	private fun parseNumber(string: String, originCoord: Int): Int = when {
		string == "~" -> originCoord

		string.startsWith("~") -> parseNumber(string.removePrefix("~"), 0) + originCoord

		else -> string.toIntOrNull() ?: fail { "&cInvalid X or Z coordinate! Must be a number." }
	}

	@Suppress("unused")
	@CommandAlias("jump")
	@CommandCompletion("@planets")
	fun onJump(sender: Player, planet: String) {
		val starship: ActivePlayerStarship = getStarshipPiloting(sender)

		val navComp: NavCompSubsystem = Hyperspace.findNavComp(starship) ?: fail { "Intact nav computer not found!" }
		val maxRange: Int =
			(navComp.multiblock.baseRange * starship.data.starshipType.hyperspaceRangeMultiplier).roundToInt()

		val cachedPlanet = Space.getPlanet(planet)

		if (cachedPlanet == null) {
			sender.sendFeedbackMessage(USER_ERROR, "Unknown planet $planet.")
			return
		}

		if (cachedPlanet.spaceWorld != sender.world) {
			sender.sendFeedbackMessage(USER_ERROR, "$planet is not in this space sector.")
			return
		}

		val x = cachedPlanet.location.x
		val z = cachedPlanet.location.z

		tryJump(starship, x, z, maxRange, sender)
	}

	private fun tryJump(starship: ActivePlayerStarship, x: Int, z: Int, maxRange: Int, sender: Player) {
		val hyperdrive: HyperdriveSubsystem = Hyperspace.findHyperdrive(starship) ?: fail {
			"Intact hyperdrive not found"
		}

		failIf(!hyperdrive.hasFuel()) {
			"Insufficient chetherite, need ${Hyperspace.HYPERMATTER_AMOUNT} in each hopper"
		}

		val world = starship.serverLevel.world
		failIf(!SpaceWorlds.contains(world)) {
			"Not a space world!"
		}

		failIf(Hyperspace.getHyperspaceWorld(world) == null) {
			"Hyperspace is not charted in this sector"
		}

		failIf(!world.worldBorder.isInside(Location(world, x.toDouble(), 128.0, z.toDouble()))) {
			"Coords are out of world border."
		}

		if (MassShadows.find(
				starship.serverLevel.world,
				starship.centerOfMass.x.toDouble(),
				starship.centerOfMass.z.toDouble()
			) != null
		) {
			sender.sendFeedbackMessage(USER_ERROR, "You're within a MassShadow, jump cancelled.")
			return
		}

		if (starship.cruiseData.velocity.lengthSquared() != 0.0) {
			sender.sendFeedbackMessage(USER_ERROR, "Starship is cruising, jump aborted, try again when it fully stops.")
			StarshipCruising.stopCruising(sender, starship)
			return
		}

		var x1: Int = x
		var z1: Int = z

		val origin: Vector = starship.centerOfMass.toVector()
		val distance: Double = distance(origin.x, 0.0, origin.z, x1.toDouble(), 0.0, z1.toDouble())

		if (distance > maxRange) {
			val (normalizedX, _, normalizedZ) = normalize(x1 - origin.x, 0.0, z1 - origin.z)
			x1 = (normalizedX * maxRange + origin.x).roundToInt()
			z1 = (normalizedZ * maxRange + origin.z).roundToInt()

			sender.sendFeedbackMessage(
				USER_ERROR,
				"Warning: You attempted to jump ${distance.toInt()} blocks, " +
					"but your navigation computer only supports jumping up to $maxRange blocks! " +
					"Automatically shortening jump. New Coordinates: $x1, $z1"
			)
		}

		sender.sendFeedbackMessage(SUCCESS, "Initiating Hyperspace Jump to approximately ({0}, {1})", x1, z1)

		val offset = ln(distance).toInt()

		// don't let it be perfectly accurate
		x1 += randomInt(-offset, offset)
		z1 += randomInt(-offset, offset)

		Hyperspace.beginJumpWarmup(starship, hyperdrive, x1, z1, true)
	}

	@Suppress("unused")
	@CommandAlias("settarget|starget|st")
	fun onSetTarget(sender: Player, set: String, @Optional player: OnlinePlayer?) {
		val starship = getStarshipRiding(sender)
		val weaponSet = set.lowercase(Locale.getDefault())
		failIf(!starship.weaponSets.containsKey(weaponSet)) {
			"No such weapon set $weaponSet"
		}
		failIf(!starship.weaponSets[weaponSet].any { it is AutoWeaponSubsystem }) {
			"No auto turrets on weapon set $weaponSet"
		}
		if (player == null) {
			failIf(starship.autoTurretTargets.remove(weaponSet) == null) {
				"No target set for $weaponSet"
			}

			sender.sendFeedbackMessage(INFORMATION, "Unset target of <aqua>$weaponSet")
		} else {
			starship.autoTurretTargets[weaponSet] = player.getPlayer().uniqueId

			sender.sendFeedbackMessage(
				INFORMATION,
				"Set target of <aqua>$weaponSet</aqua> to <white>${player.getPlayer().name}"
			)
		}
	}

	@Suppress("unused")
	@CommandAlias("unsettarget|ustarget|ust|unstarget")
	fun onUnSetTarget(sender: Player, set: String, weapon: String) {
		val starship = getStarshipRiding(sender)
		val weaponSet = weapon.lowercase(Locale.getDefault())
		if (set == "unset") {
			for (shipWeapon in starship.weapons) {
				if (shipWeapon.toString().lowercase().contains(weaponSet)) {
					starship.autoTurretTargets.forEach { _ ->
						starship.autoTurretTargets.forEach {
							if (it.key == set) {
								starship.autoTurretTargets.remove(it.key, it.value)
							}
						}
					}
					sender.sendFeedbackActionMessage(INFORMATION, "Unset targets of weaponssets containing {0}", weaponSet)
				}
			}
		}
	}

	@Suppress("unused")
	@CommandAlias("usa|unsetall|unsa")
	fun onUnSetAll(sender: Player) {
		val starship = getStarshipRiding(sender)
		if (starship.autoTurretTargets.isEmpty()) {
			sender.sendFeedbackActionMessage(USER_ERROR, "Error no weaponsets with autoturret targets found")
			return
		}
		starship.autoTurretTargets.forEach { starship.autoTurretTargets.remove(it.key, it.value) }
		sender.sendFeedbackActionMessage(INFORMATION, "Unset target for all weaponsets.")
	}

	@Suppress("unused")
	@CommandAlias("powerdivision|powerd|pdivision|pd|powermode|pm")
	fun onPowerDivision(sender: Player, shield: Int, weapon: Int, thruster: Int) {
		val sum = shield + weapon + thruster
		val shieldPct = (shield.toDouble() / sum * 100.0).toInt()
		val weaponPct = (weapon.toDouble() / sum * 100.0).toInt()
		val thrusterPct = (thruster.toDouble() / sum * 100.0).toInt()

		failIf(arrayOf(shieldPct, weaponPct, thrusterPct).any { it !in 10..50 }) {
			"Power mode $shieldPct $weaponPct $thrusterPct is not allowed! None can be less than 10% or greater than 50%."
		}

		getStarshipRiding(sender).updatePower(sender, shieldPct, weaponPct, thrusterPct)
	}

	@Suppress("unused")
	@CommandAlias("nukeship")
	@CommandPermission("starships.nukeship")
	fun onNukeShip(sender: Player) {
		val ship = getStarshipRiding(sender) as? ActivePlayerStarship ?: return
		StarshipDestruction.vanish(ship)
	}

	@Suppress("unused")
	@CommandAlias("directcontrol|dc")
	fun onDirectControl(sender: Player) {
		val starship = getStarshipPiloting(sender)
		failIf(!starship.isDirectControlEnabled && !StarshipControl.isHoldingController(sender)) {
			"You need to hold a starship controller to enable direct control"
		}
		if (starship.initialBlockCount > StarshipType.CORVETTE.maxSize) {
			sender.sendFeedbackMessage(
				FeedbackType.SERVER_ERROR,
				"Only ships of size {0} or less can use direct control, this is mostly a performance thing, and will probably change in the future.",
				StarshipType.CORVETTE.maxSize
			)
			return
		}
		starship.setDirectControlEnabled(!starship.isDirectControlEnabled)
	}

	@Suppress("unused")
	@CommandAlias("cruise")
	fun onCruise(sender: Player) {
		val ship = getStarshipPiloting(sender)
		if (!StarshipCruising.isCruising(ship)) {
			StarshipCruising.startCruising(sender, ship)
		} else {
			StarshipCruising.stopCruising(sender, ship)
		}
	}

	@Suppress("unused")
	@CommandAlias("cruisespeed|csp|speedlimit|cruisespeedlimit")
	fun onCruiseSpeed(sender: Player, speedLimit: Int) {
		val ship = getStarshipPiloting(sender)
		ship.speedLimit = speedLimit

		sender.sendFeedbackMessage(INFORMATION, "Speed limit set to $speedLimit")
	}

	@Suppress("unused")
	@CommandAlias("eject")
	fun onEject(sender: Player, who: OnlinePlayer) {
		val starship = getStarshipPiloting(sender)
		val player = who.player
		failIf(sender == player) { "Can't eject yourself" }
		val inHitbox = starship.isWithinHitbox(player)
		starship.removePassenger(player.uniqueId)
		failIf(!inHitbox) { "${player.name} is not riding!" }
		val location = player.location
		val x = ThreadLocalRandom.current().nextDouble(-1.0, 1.0)
		var y = ThreadLocalRandom.current().nextDouble(-1.0, 1.0)
		val z = ThreadLocalRandom.current().nextDouble(-1.0, 1.0)
		while (starship.isWithinHitbox(location)) {
			location.add(x, y, z)
			if (location.y < 5 || location.y > 250) {
				y *= -1
			}
		}
		player.teleport(location)

		starship.onlinePassengers.forEach { passenger ->
			passenger.sendFeedbackMessage(
				INFORMATION,
				"${player.name} was ejected from the starship"
			)
		}

		player.sendFeedbackMessage(ALERT, "You were ejected from the starship")
	}

	@Suppress("unused")
	@CommandAlias("listships")
	@CommandPermission("starships.listships")
	fun onListShips(sender: Player) {
		var totalShips = 0
		var totalBlocks = 0

		for (starship in ActiveStarships.all()) {
			val pilot: Player? = (starship as? ActivePlayerStarship)?.pilot
			totalShips++

			val size: Int = starship.initialBlockCount
			totalBlocks += size

			val name = (starship as? ActivePlayerStarship)?.data?.let { getDisplayName(it) } ?: starship.type.formatted
			val hoverName = MiniMessage.miniMessage().deserialize(starship.type.formatted).asHoverEvent()

			val pilotName = pilot?.name ?: "none"

			val pilotNationID = pilot?.let { PlayerCache[pilot].nation }

			val senderNationID = PlayerCache[sender.player!!].nation

			val pilotNationRelation = senderNationID?.let {
				pilotNationID?.let {
					NationRelation.col.find(NationRelation::nation eq senderNationID)
						.firstOrNull { it.other == pilotNationID }
				}
			}

			val pilotRelationColor = pilotNationRelation?.actual?.textStyle

			val formattedName =
				pilotRelationColor?.let { "<$pilotRelationColor>$pilotName</$pilotRelationColor>" } ?: pilotName

			var worldName = starship.serverLevel.world.key.toString().substringAfterLast(":")
				.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

			if (worldName == "Overworld") {
				worldName = starship.serverLevel.world.name
					.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
			}

			val message = MiniMessage.miniMessage().deserialize(
				"$name<reset> piloted by $formattedName of size $size in $worldName"
			).hoverEvent(hoverName)

			sender.sendMessage(message)
		}

		sender.sendRichMessage("<gray>Total Ships<dark_gray>:<aqua> $totalShips")
		sender.sendRichMessage("<gray>Total Blocks in all ships<dark_gray>:<aqua> $totalBlocks")
	}

	@Suppress("unused")
	@CommandAlias("usebeacon")
	fun onUseBeacon(sender: Player) {
		val ship = getStarshipRiding(sender) as? ActivePlayerStarship ?: return

		if (ship.beacon != null) {
			val other = ship.beacon!!.destination
			tryJump(ship, other.x, other.z, Int.MAX_VALUE, sender)
			ship.beacon = null
		} else {
			sender.sendFeedbackMessage(USER_ERROR, "Starship is not near beacon!")
		}
	}
}
