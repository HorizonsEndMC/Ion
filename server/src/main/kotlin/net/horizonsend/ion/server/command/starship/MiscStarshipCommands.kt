package net.horizonsend.ion.server.command.starship

import co.aikar.commands.PaperCommandManager
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Description
import co.aikar.commands.annotation.Optional
import co.aikar.commands.bukkit.contexts.OnlinePlayer
import net.horizonsend.ion.common.database.cache.nations.RelationCache
import net.horizonsend.ion.common.database.schema.starships.Blueprint
import net.horizonsend.ion.common.extensions.alert
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.informationTitle
import net.horizonsend.ion.common.extensions.serverError
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.successActionMessage
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.common.extensions.userErrorActionMessage
import net.horizonsend.ion.common.utils.configuration.redis
import net.horizonsend.ion.common.utils.miscellaneous.randomInt
import net.horizonsend.ion.common.utils.text.bracketed
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_DARK_GRAY
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_LIGHT_BLUE
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_LIGHT_GRAY
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_LIGHT_ORANGE
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_MEDIUM_GRAY
import net.horizonsend.ion.common.utils.text.formatLink
import net.horizonsend.ion.common.utils.text.lineBreakWithCenterText
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.configuration.ServerConfiguration.Pos
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.features.client.display.PlanetSpaceRendering
import net.horizonsend.ion.server.features.misc.HyperspaceBeaconManager
import net.horizonsend.ion.server.features.misc.NewPlayerProtection.hasProtection
import net.horizonsend.ion.server.features.multiblock.drills.DrillMultiblock
import net.horizonsend.ion.server.features.space.Space
import net.horizonsend.ion.server.features.space.SpaceWorlds
import net.horizonsend.ion.server.features.starship.AutoTurretTargeting
import net.horizonsend.ion.server.features.starship.DeactivatedPlayerStarships
import net.horizonsend.ion.server.features.starship.Interdiction
import net.horizonsend.ion.server.features.starship.Interdiction.toggleGravityWell
import net.horizonsend.ion.server.features.starship.PilotedStarships
import net.horizonsend.ion.server.features.starship.StarshipSchematic
import net.horizonsend.ion.server.features.starship.StarshipType
import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.features.starship.control.controllers.player.PlayerController
import net.horizonsend.ion.server.features.starship.control.movement.PlayerStarshipControl.isHoldingController
import net.horizonsend.ion.server.features.starship.control.movement.StarshipCruising
import net.horizonsend.ion.server.features.starship.control.signs.StarshipSigns
import net.horizonsend.ion.server.features.starship.destruction.StarshipDestruction
import net.horizonsend.ion.server.features.starship.hyperspace.Hyperspace
import net.horizonsend.ion.server.features.starship.hyperspace.MassShadows
import net.horizonsend.ion.server.features.starship.subsystem.HyperdriveSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.NavCompSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.AutoWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.secondary.ArsenalRocketStarshipWeaponSubsystem
import net.horizonsend.ion.server.features.waypoint.WaypointManager
import net.horizonsend.ion.server.miscellaneous.utils.*
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.newline
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.NamedTextColor.WHITE
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.block.Sign
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import java.util.Locale
import java.util.UUID
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.TimeUnit
import kotlin.collections.set
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.ln
import kotlin.math.roundToInt

object MiscStarshipCommands : net.horizonsend.ion.server.command.SLCommand() {
	override fun onEnable(manager: PaperCommandManager) {
		manager.commandCompletions.registerCompletion("hyperspaceGates") {
			IonServer.configuration.beacons.map { it.name.replace(" ", "_") }
		}
		manager.commandCompletions.registerCompletion("hyperspaceGatesInWorld") { e ->
			IonServer.configuration.beacons
				.filter { beacon -> beacon.spaceLocation.world == e.player.world.name }
				.map { it.name.replace(" ", "_") }
		}
		manager.commandCompletions.registerAsyncCompletion("autoTurretTargets") { context ->
			val all = mutableListOf<String>()

			ActiveStarships.getInWorld(context.player.world).mapTo(all) { it.identifier }
			all.addAll(IonServer.server.onlinePlayers.map { it.name })
			all.remove(context.player.name)
			all
		}
		manager.commandCompletions.registerAsyncCompletion("nodes") { context ->
			ActiveStarships.findByPilot(context.player)?.weaponSets?.keys()
		}
	}

	@Suppress("unused")
	@CommandAlias("release")
	fun onRelease(sender: Player) {
		PilotedStarships.tryRelease(PilotedStarships[sender] ?: return sender.userError("You are not piloting a starship"))
	}

	@Suppress("unused")
	@CommandAlias("unpilot")
	fun onUnpilot(sender: Player) {
		val starship = getStarshipPiloting(sender)
		PilotedStarships.unpilot(starship)
		sender.information("Unpiloted ship, but left it activated")
	}

	@Suppress("unused")
	@CommandAlias("stopriding")
	fun onStopRiding(sender: Player) {
		val starship = getStarshipRiding(sender)

		failIf(starship is ActiveControlledStarship && starship.playerPilot == sender) {
			"You can't stop riding if you're the pilot. Use /release or /unpilot."
		}

		starship.removePassenger(sender.uniqueId)
		sender.success("Stopped riding ship")
	}

	@Suppress("unused")
	@CommandAlias("loadship")
	@CommandPermission("starships.loadship")
	@CommandCompletion("@players")
	fun onLoadShip(sender: Player, player: String, world: World) = asyncCommand(sender) {
		val uuid = resolveOfflinePlayer(player)
		redis {
			val key = "starships.lastpiloted.$uuid.${world.name.lowercase(Locale.getDefault())}"

			failIf(!exists(key)) { "$player doesn't have a ship saved for /loadship in $world" }

			val schematic = Blueprint.parseData(get(key))

			val pilotLoc = Vec3i(0, 0, 0)

			BlueprintCommand.checkObstruction(sender.location, schematic, pilotLoc)

			BlueprintCommand.loadSchematic(sender.location, schematic, pilotLoc)
		}
	}

	@Suppress("Unused")
	@CommandAlias("jump")
	@Description("Jump to a set of coordinates, a hyperspace beacon, or a planet")
	fun onJump(sender: Player) {
		val selectedPlanetData = PlanetSpaceRendering.planetSelectorDataMap[sender.uniqueId]
		if (selectedPlanetData != null) {
			// player is looking at a planet in their HUD
			onJump(sender, selectedPlanetData.name, null)
		} else {
			sender.userError("Invalid destination. Type /jump while looking at a planet, or /jump <planet>, /jump <hyperspace gate> or /jump <x> <z>")
		}
	}

	@Suppress("unused")
	@CommandAlias("jump")
	@CommandCompletion("x|z")
	@Description("Jump to a set of coordinates, a hyperspace beacon, or a planet")
	fun onJump(sender: Player, xCoordinate: String, zCoordinate: String, @Optional hyperdriveTier: Int?) {
		val starship: ActiveControlledStarship = getStarshipPiloting(sender)

		val navComp: NavCompSubsystem = Hyperspace.findNavComp(starship) ?: fail { "Intact nav computer not found!" }
		val maxRange: Int =
			(navComp.multiblock.baseRange * starship.balancing.hyperspaceRangeMultiplier).roundToInt()

		val x = parseNumber(xCoordinate, starship.centerOfMass.x)
		val z = parseNumber(zCoordinate, starship.centerOfMass.z)

		tryJump(starship, x, z, starship.world, maxRange, sender, hyperdriveTier)
	}

	fun parseNumber(string: String, originCoord: Int): Int = when {
		string == "~" -> originCoord

		string.startsWith("~") -> parseNumber(string.removePrefix("~"), 0) + originCoord

		else -> string.toIntOrNull() ?: fail { "&cInvalid X or Z coordinate! Must be a number." }
	}

	@Suppress("unused")
	@CommandAlias("jump")
	@CommandCompletion("auto|@planetsInWorld|@hyperspaceGatesInWorld")
	@Description("Jump to a set of coordinates, a hyperspace beacon, or a planet")
	fun onJump(sender: Player, destination: String, @Optional hyperdriveTier: Int?) {
		val starship: ActiveControlledStarship = getStarshipPiloting(sender)

		val navComp: NavCompSubsystem = Hyperspace.findNavComp(starship) ?: fail { "Intact nav computer not found!" }
		val maxRange: Int =
			(navComp.multiblock.baseRange * starship.balancing.hyperspaceRangeMultiplier).roundToInt()

		if (Hyperspace.isWarmingUp(starship)) fail { "Starship is already warming up!" }

		if (destination == "auto") {
			val playerPath = WaypointManager.playerPaths[sender.uniqueId]
			if (playerPath.isNullOrEmpty()) fail { "Route not set" }
			if (starship.beacon != null &&
				starship.beacon!!.name == WaypointManager.getNextWaypoint(sender)!!.replace("_", " ")
				) {
				onUseBeacon(sender)
				return
			}
			val x = playerPath.first().edgeList.first().target.loc.x.toInt()
			val z = playerPath.first().edgeList.first().target.loc.z.toInt()
			tryJump(starship, x, z, starship.world, maxRange, sender, hyperdriveTier)
			return
		}

		val destinationPos = Space.getPlanet(destination)?.let {
			Pos(
				it.spaceWorldName,
				it.location.x,
				192,
				it.location.z
			)
		} ?: IonServer.configuration.beacons.firstOrNull {
			it.name.replace(" ", "_") == destination
		}?.spaceLocation

		if (destinationPos == null) {
			sender.userError("Unknown destination $destination.")
			return
		}

		if (destinationPos.bukkitWorld() != sender.world) {
			sender.sendRichMessage(
				"<red>$destination is not in this space sector. Add <yellow>$destination <red>to your navigation route? " +
						"<gold><italic><hover:show_text:'<gray>/route add $destination'>" +
						"<click:run_command:/route add $destination>[Click to add waypoint to route]</click>"
			)
			return
		}

		val x = destinationPos.x
		val z = destinationPos.z

		tryJump(starship, x, z, starship.world, maxRange, sender, hyperdriveTier)
	}

	private fun tryJump(
		starship: ActiveControlledStarship,
		x: Int,
		z: Int,
		destinationWorld: World,
		maxRange: Int,
		sender: Player,
		tier: Int?
	) {
		val hyperdrive: HyperdriveSubsystem = tier?.let { Hyperspace.findHyperdrive(starship, tier) }
			?: Hyperspace.findHyperdrive(starship) ?: fail {
				"Intact hyperdrive not found"
			}

		failIf(!hyperdrive.hasFuel()) {
			"Insufficient chetherite, need ${Hyperspace.HYPERMATTER_AMOUNT} in each hopper"
		}

		val currentWorld = starship.world
		failIf(!SpaceWorlds.contains(currentWorld)) {
			"Not a space world!"
		}

		failIf(!SpaceWorlds.contains(destinationWorld)) {
			"Not a space world!"
		}

		failIf(Hyperspace.getHyperspaceWorld(currentWorld) == null) {
			"Hyperspace is not charted in this sector"
		}

		failIf(!destinationWorld.worldBorder.isInside(Location(destinationWorld, x.toDouble(), 128.0, z.toDouble()))) {
			"Destination coordinates are outside the world order"
		}

		val massShadowInfo = MassShadows.find(
			starship.world,
			starship.centerOfMass.x.toDouble(),
			starship.centerOfMass.z.toDouble()
		)

		if (massShadowInfo != null) {
			val escapeVector = starship.centerOfMass.toVector().setY(128)
			escapeVector.subtract(Vector(massShadowInfo.x, 128, massShadowInfo.z)).rotateAroundY(PI / 2)
			escapeVector.normalize()

			// directionString differs from ContactsDisplay as this vector was rotated pi/2 radians
			// This is so that the vector's 0 direction points south
			var directionString = ""

			if (escapeVector.x != 0.0 && abs(escapeVector.x) > 0.4) {
				directionString += if (escapeVector.x > 0) "south" else "north"
			}

			if (escapeVector.z != 0.0 && abs(escapeVector.z) > 0.4) {
				directionString += if (escapeVector.z > 0) "west" else "east"
			}

			val message = text()
				.color(NamedTextColor.GRAY)
				.append(text("Starship is within a gravity well; jump aborted. Move away from the gravity well source:", NamedTextColor.RED))
				.append(newline())
				.append(text("Object: "))
				.append(massShadowInfo.description)
				.append(newline())
				.append(text("Location: "))
				.append(text("${massShadowInfo.x}, ${massShadowInfo.z}", WHITE))
				.append(newline())
				.append(text("Gravity well radius: "))
				.append(text(massShadowInfo.radius, WHITE))
				.append(newline())
				.append(text("Current distance from center: "))
				.append(text(massShadowInfo.distance, WHITE))
				.append(newline())
				.append(text("Cruise direction to escape: "))
				.append(text(directionString, NamedTextColor.GREEN))
				.append(text(" (${(atan2(escapeVector.z, escapeVector.x) * 180 / PI).toInt()})", WHITE))

			starship.sendMessage(message)
			return
		}

		if (starship.cruiseData.velocity.lengthSquared() != 0.0) {
			sender.userError("Starship is cruising; jump aborted. Try again when the starship fully stops moving.")
			StarshipCruising.stopCruising(starship.controller, starship)
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

			sender.userError(
				"Warning: You attempted to jump ${distance.toInt()} blocks, " +
					"but your navigation computer only supports jumping up to $maxRange blocks! " +
					"Automatically shortening jump. New Coordinates: $x1, $z1"
			)
		}

		sender.success("Initiating hyperspace jump to ${destinationWorld.name} ($x1, $z1)")

		val offset = ln(distance).toInt()

		// don't let it be perfectly accurate
		x1 += randomInt(-offset, offset)
		z1 += randomInt(-offset, offset)

		Hyperspace.beginJumpWarmup(starship, hyperdrive, x1, z1, destinationWorld, true)
	}

	@Suppress("unused")
	@CommandAlias("settarget|starget|st")
	@CommandCompletion("@nodes @autoTurretTargets @nothing")
	fun onSetTarget(sender: Player, set: String, @Optional target: String?) {
		val starship = getStarshipRiding(sender)
		val weaponSet = set.lowercase(Locale.getDefault())
		failIf(!starship.weaponSets.containsKey(weaponSet)) { "No such weapon set $weaponSet" }
		failIf(!starship.weaponSets[weaponSet].any { it is AutoWeaponSubsystem }) { "No auto turrets on weapon set $weaponSet" }

		if (target == null) {
			failIf(starship.autoTurretTargets.remove(weaponSet) == null) { "No target set for $weaponSet" }

			sender.information("Unset target of <aqua>$weaponSet")
			return
		}

		val formatted = if (target.contains(":".toRegex())) target.substringAfter(":") else target

		val targeted =
			Bukkit.getPlayer(formatted)?.let { AutoTurretTargeting.target(it) } ?:
			ActiveStarships[formatted]?.let { AutoTurretTargeting.target(it) } ?:
			fail { "Target $target could not be found!" }

		starship.autoTurretTargets[weaponSet] = targeted

		sender.information("Set target of <aqua>$weaponSet</aqua> to <white>${target}")
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
					sender.information("Unset targets of weaponssets containing $weaponSet")
				}
			}
		}
	}

	@Suppress("unused")
	@CommandAlias("usa|unsetall|unsa")
	fun onUnSetAll(sender: Player) {
		val starship = getStarshipRiding(sender)
		if (starship.autoTurretTargets.isEmpty()) {
			sender.userErrorActionMessage("Error no weaponsets with autoturret targets found")
			return
		}
		starship.autoTurretTargets.clear()
		sender.successActionMessage("Unset target for all weaponsets.")
	}

	@CommandAlias("weaponset|ws")
	@Suppress("unused")
	@CommandCompletion("@nodes")
	fun onWeaponset(sender: Player, set: String) {
		val starship = getStarshipPiloting(sender)

		if (!starship.weaponSets.containsKey(set)) {
			sender.userError("No nodes for $set")
			return
		}

		val current: UUID? = starship.weaponSetSelections.inverse().remove(set)

		if (current != null) {
			if (current == sender.uniqueId) {
				sender.success("Released weapon set <white>$set")
				return
			}

			StarshipSigns.informOfSteal(current, starship, sender, set)
		}

		starship.weaponSetSelections[sender.uniqueId] = set

		sender.success("Took control of weapon set <white>$set")
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

		getStarshipRiding(sender).updatePower(sender.name, shieldPct, weaponPct, thrusterPct)
	}

	@Suppress("unused")
	@CommandAlias("nukeship")
	@CommandPermission("starships.nukeship")
	fun onNukeShip(sender: Player) {
		val ship = getStarshipRiding(sender) as? ActiveControlledStarship ?: return
		StarshipDestruction.vanish(ship)
	}

	@Suppress("unused")
	@CommandAlias("directcontrol|dc")
	fun onDirectControl(sender: Player) {
		val starship = getStarshipPiloting(sender)
		failIf(!starship.isDirectControlEnabled && !isHoldingController(sender)) {
			"You need to hold a starship controller to enable direct control"
		}
		if (starship.initialBlockCount > StarshipType.DESTROYER.maxSize) {
			sender.serverError(
				"Only ships of size ${StarshipType.DESTROYER.maxSize} or less can use direct control, " +
					"this is mostly a performance thing, and will probably change in the future."
			)
			return
		}
		starship.setDirectControlEnabled(!starship.isDirectControlEnabled)
	}

	@Suppress("unused")
	@CommandAlias("cruise")
	fun onCruise(sender: Player) {
		val ship = getStarshipPiloting(sender)
		val controller = ActiveStarships.findByPilot(sender)?.controller ?: return

		if (!StarshipCruising.isCruising(ship)) {
			val dir = sender.location.direction.setY(0).normalize()

			StarshipCruising.startCruising(controller, ship, dir)
		} else {
			StarshipCruising.stopCruising(controller, ship)
		}
	}

	@Suppress("unused")
	@CommandAlias("cruisespeed|csp|speedlimit|cruisespeedlimit")
	fun onCruiseSpeed(sender: Player, speedLimit: Int) {
		val ship = getStarshipPiloting(sender)
		ship.speedLimit = speedLimit

		sender.information("Speed limit set to $speedLimit")
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
			passenger.information(
				"${player.name} was ejected from the starship"
			)
		}

		player.alert("You were ejected from the starship")
	}

	@Suppress("unused")
	@CommandAlias("listships")
	@CommandCompletion("ai|player|all")
	@CommandPermission("starships.listships")
	fun onListShips(sender: Player, @Optional option: String?) {
		var totalShips = 0
		var totalBlocks = 0

		sender.sendMessage(lineBreakWithCenterText(text("Active Starships", HE_LIGHT_ORANGE)))
		val senderNation = PlayerCache[sender].nationOid
		val starships = when (option) {
			"player" -> ActiveStarships.all().filter { it.controller is PlayerController }
			"ai" -> ActiveStarships.all().filter { it.controller is AIController }
			else -> ActiveStarships.all()
		}

		for (starship in starships) {
			val controller = starship.controller

			val pilot: Player? = starship.playerPilot
			totalShips++

			val size: Int = starship.initialBlockCount
			totalBlocks += size

			var worldName = starship.world.key.toString().substringAfterLast(":")
				.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

			if (worldName == "Overworld") {
				worldName = starship.world.name.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
			}

			val name: Component = when (controller) {
				is PlayerController -> {
					val player = controller.player
					val pilotNation = PlayerCache.getIfOnline(player)?.nationOid

					val color = if (pilotNation != null && senderNation != null) {
						RelationCache[senderNation, pilotNation].color
					} else WHITE

					controller.getPilotName().color(color)
				}

				else -> controller.getPilotName()
			}

			val line = template(
				"{0}{1} piloted by {2} {3} in {4}",
				color = HE_LIGHT_GRAY,
				paramColor = WHITE,
				useQuotesAroundObjects = true,
				if (pilot?.hasProtection() == true) text(" ★", NamedTextColor.GOLD) else Component.empty(),
				starship.getDisplayName(),
				name,
				bracketed(text(starship.initialBlockCount, WHITE)),
				worldName
			).hoverEvent(ofChildren(text("${starship.initialBlockCount} block "), starship.type.displayNameComponent))

			sender.sendMessage(line)
		}

		sender.sendMessage(net.horizonsend.ion.common.utils.text.lineBreak(47))
		sender.sendMessage(ofChildren(text("Total Ships", HE_MEDIUM_GRAY), text(": ", HE_DARK_GRAY), text(totalShips, HE_LIGHT_BLUE)))
		sender.sendMessage(ofChildren(text("Total Blocks in all ships", HE_MEDIUM_GRAY), text(": ", HE_DARK_GRAY), text(totalBlocks, HE_LIGHT_BLUE)))
	}

	@Suppress("unused")
	@CommandAlias("usebeacon")
	fun onUseBeacon(sender: Player) {
		val ship = getStarshipRiding(sender) as? ActiveControlledStarship ?: return
		HyperspaceBeaconManager.detectNearbyBeacons(ship, 0, 0)
		val beacon = ship.beacon

		if (beacon != null) {
			val other = beacon.exits?.randomOrNull() ?: beacon.destination
			tryJump(
				ship,
				other.x,
				other.z,
				other.bukkitWorld(),
				Int.MAX_VALUE,
				sender,
				null
			)
			ship.beacon = null
		} else {
			sender.userError("Starship is not near beacon!")
		}
	}

	@Suppress("unused")
	@CommandAlias("drills")
	@CommandCompletion("true|false")
	@Description("Enable or disable all drills on your active starship")
	fun onToggleDrills(sender: Player, enabled: Boolean) {
		val starship = getStarshipPiloting(sender)

		val signs = starship.drills.mapNotNull {
			val (x, y, z) = it.pos

			starship.world.getBlockAt(x, y, z).state as? Sign
		}

		val user = if (enabled) sender.name else null

		for (sign in signs) {
			DrillMultiblock.setUser(sign, user)
		}
	}

	@Suppress("unused")
	@CommandAlias("gravwell")
	@Description("Toggle all gravity wells on your starship")
	fun onToggleGravwell(sender: Player) {
		val starship = getStarshipPiloting(sender)
		Interdiction.findGravityWell(starship) ?: fail { "Intact gravity well not found!" }

		toggleGravityWell(starship)
	}

	@Suppress("unused")
	@CommandAlias("pilot")
	@Description("Try to pilot the ship you're standing on")
	fun onPilot(sender: Player) {
		val world = sender.world
		val (x, y, z) = Vec3i(sender.location)

		val starshipData = DeactivatedPlayerStarships.getContaining(world, x, y - 1, z)

		if (starshipData == null) {
			sender.userError("Could not find starship. Is it detected?")
			return
		}

		PilotedStarships.tryPilot(sender, starshipData)
	}

	val uploadCooldown = object : PerPlayerCooldown(60L, TimeUnit.SECONDS) {
		override fun cooldownRejected(player: UUID) {
			Bukkit.getPlayer(player)?.userError("You're doing that too often!")
		}
	}

	@Suppress("unused")
	@CommandAlias("starshipdownload")
	@Description("Download the ship you're currently piloting")
	fun onDownload(sender: Player) = asyncCommand(sender) {
		uploadCooldown.tryExec(sender) {
			val starship = getStarshipPiloting(sender)

			val schem = Tasks.getSyncBlocking { StarshipSchematic.createSchematic(starship) }

			schem.uploadAsync {
				if (it == null) {
					sender.serverError("There was an error uploading your schematic")
					return@uploadAsync
				}

				sender.sendMessage(Component.textOfChildren(
					text("Your schematic has been uploaded.", NamedTextColor.DARK_GREEN), newline(),
					formatLink("Click this link to download it", it.toString())
				))
			}
		}
	}
	@Suppress("unused")
	@CommandAlias("targetposition")
	@Description("Targets a currentPosition")
	fun onTargetPosition(sender: Player, x: Double, y: Double, z: Double){
		val starship = getStarshipPiloting(sender)
		if (!starship.weapons.any {it is ArsenalRocketStarshipWeaponSubsystem}) sender.userError("Error: No Arsenal Missiles found, position not targeted")

		starship.targetedPosition = Location(starship.world, x, y, z)
		sender.information("Targeted: $x, $y, $z with the ships Arsenal Missiles")
	}
}
