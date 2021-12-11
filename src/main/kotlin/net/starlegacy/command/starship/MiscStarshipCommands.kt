package net.starlegacy.command.starship

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Optional
import co.aikar.commands.bukkit.contexts.OnlinePlayer
import net.starlegacy.command.SLCommand
import net.starlegacy.database.schema.starships.Blueprint
import net.starlegacy.feature.space.Space
import net.starlegacy.feature.space.SpaceWorlds
import net.starlegacy.feature.starship.DeactivatedPlayerStarships
import net.starlegacy.feature.starship.PilotedStarships
import net.starlegacy.feature.starship.StarshipDestruction
import net.starlegacy.feature.starship.active.ActivePlayerStarship
import net.starlegacy.feature.starship.active.ActiveStarships
import net.starlegacy.feature.starship.control.StarshipControl
import net.starlegacy.feature.starship.control.StarshipCruising
import net.starlegacy.feature.starship.hyperspace.Hyperspace
import net.starlegacy.feature.starship.hyperspace.HyperspaceBeacons
import net.starlegacy.feature.starship.subsystem.HyperdriveSubsystem
import net.starlegacy.feature.starship.subsystem.NavCompSubsystem
import net.starlegacy.feature.starship.subsystem.weapon.interfaces.AutoWeaponSubsystem
import net.starlegacy.redis
import net.starlegacy.util.Vec3i
import net.starlegacy.util.action
import net.starlegacy.util.distance
import net.starlegacy.util.msg
import net.starlegacy.util.normalize
import net.starlegacy.util.randomInt
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import java.util.concurrent.ThreadLocalRandom
import kotlin.collections.set
import kotlin.math.ln
import kotlin.math.roundToInt

object MiscStarshipCommands : SLCommand() {
    @CommandAlias("release")
    fun onRelease(sender: Player) {
        DeactivatedPlayerStarships.deactivateAsync(getStarshipPiloting(sender)) {
            sender msg "&bReleased starship"
        }
    }

    @CommandAlias("unpilot")
    fun onUnpilot(sender: Player) {
        val starship = getStarshipPiloting(sender)
        PilotedStarships.unpilot(starship)
        sender msg "&bUnpiloted ship, but left it activated"
    }

    @CommandAlias("stopriding")
    fun onStopRiding(sender: Player) {
        val starship = getStarshipRiding(sender)

        failIf(starship is ActivePlayerStarship && starship.pilot == sender) {
            "You can't stop riding if you're the pilot. Use /release or /unpilot."
        }

        starship.removePassenger(sender.uniqueId)
        sender action "&eStopped riding ship"
    }

    @CommandAlias("loadship")
    @CommandPermission("starships.loadship")
    @CommandCompletion("@players")
    fun onLoadShip(sender: Player, player: String, world: String) = asyncCommand(sender) {
        val uuid = resolveOfflinePlayer(player)
        redis {
            val key = "starships.lastpiloted.$uuid.${world.toLowerCase()}"

            failIf(!exists(key)) { "$player doesn't have a ship saved for /loadship in $world" }

            val schematic = Blueprint.parseData(get(key))

            val pilotLoc = Vec3i(0, 0, 0)

            BlueprintCommand.checkObstruction(sender, schematic, pilotLoc)

            BlueprintCommand.loadSchematic(sender, schematic, pilotLoc)
        }
    }

    @CommandAlias("jump")
    fun onJump(sender: Player, xCoordinate: String, zCoordinate: String) {
        val starship: ActivePlayerStarship = getStarshipPiloting(sender)

        val navComp: NavCompSubsystem = Hyperspace.findNavComp(starship) ?: fail { "Intact nav computer not found!" }
        val maxRange: Int = (navComp.multiblock.baseRange * starship.data.type.hyperspaceRangeMultiplier).roundToInt()

        val x = parseNumber(xCoordinate, starship.centerOfMass.x)
        val z = parseNumber(zCoordinate, starship.centerOfMass.z)

        tryJump(starship, x, z, maxRange, sender)
    }

    private fun parseNumber(string: String, originCoord: Int): Int = when {
        string == "~" -> originCoord

        string.startsWith("~") -> parseNumber(string.removePrefix("~"), 0) + originCoord

        else -> string.toIntOrNull() ?: fail { "&cInvalid X or Z coordinate! Must be a number." }
    }

    @CommandAlias("jump")
    fun onJump(sender: Player, planet: String) {
        val starship: ActivePlayerStarship = getStarshipPiloting(sender)

        val navComp: NavCompSubsystem = Hyperspace.findNavComp(starship) ?: fail { "Intact nav computer not found!" }
        val maxRange: Int = (navComp.multiblock.baseRange * starship.data.type.hyperspaceRangeMultiplier).roundToInt()

        val cachedPlanet = Space.getPlanet(planet)

        if (cachedPlanet == null) {
            sender msg "&cUnknown planet $planet."
            return
        }

        if (cachedPlanet.spaceWorld != sender.world) {
            sender msg "&c$planet is not in this space sector."
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

        val world = starship.world
        failIf(!SpaceWorlds.contains(world)) {
            "Not a space world!"
        }

        failIf(Hyperspace.getHyperspaceWorld(world) == null) {
            "Hyperspace is not charted in this sector"
        }

        failIf(!world.worldBorder.isInside(Location(world, x.toDouble(), 128.0, z.toDouble()))) {
            "Coords are out of world border."
        }

        var x1: Int = x
        var z1: Int = z

        val origin: Vector = starship.centerOfMass.toVector()
        val distance: Double = distance(origin.x, 0.0, origin.z, x1.toDouble(), 0.0, z1.toDouble())

        if (distance > maxRange) {
            val (normalizedX, _, normalizedZ) = normalize(x1 - origin.x, 0.0, z1 - origin.z)
            x1 = (normalizedX * maxRange + origin.x).roundToInt()
            z1 = (normalizedZ * maxRange + origin.z).roundToInt()

            sender msg "&eWarning: You attempted to jump $distance blocks, " +
                    "but your navigation computer only supports jumping up to $maxRange blocks! " +
                    "Automatically shortening jump. New Coordinates: $x1, $z1"
        }

        val offset = ln(distance).toInt()

        // don't let it be perfectly accurate
        x1 += randomInt(-offset, offset)
        z1 += randomInt(-offset, offset)

        Hyperspace.beginJumpWarmup(starship, hyperdrive, x1, z1, true)
    }

    @CommandAlias("settarget|starget|st")
    fun onSetTarget(sender: Player, set: String, @Optional player: OnlinePlayer?) {
        val starship = getStarshipRiding(sender)
        val weaponSet = set.toLowerCase()
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
            sender msg "&7Unset target of &b$weaponSet"
        } else {
            starship.autoTurretTargets[weaponSet] = player.getPlayer().uniqueId
            sender msg "&7Set target of &b$weaponSet&7 to ${player.getPlayer().name}"
        }
    }

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

    @CommandAlias("usebeacon")
    fun onUseBeacon(sender: Player) {
        val ship = getStarshipRiding(sender) as? ActivePlayerStarship ?: return
        val beacon = ship.nearbyBeacon
        if (beacon != null) {
            val other = HyperspaceBeacons[beacon.pairId] ?: fail { "Beacon pair is missing!" }
            tryJump(ship, other.x, other.z, Int.MAX_VALUE, sender)
            ship.nearbyBeacon = null
        }
    }

    @CommandAlias("nukeship")
    @CommandPermission("starships.nukeship")
    fun onNukeShip(sender: Player) {
        val ship = getStarshipRiding(sender) as? ActivePlayerStarship ?: return
        StarshipDestruction.vanish(ship)
    }

    @CommandAlias("directcontrol|dc")
    fun onDirectControl(sender: Player) {
        val starship = getStarshipPiloting(sender)
        failIf(!starship.isDirectControlEnabled && !StarshipControl.isHoldingController(sender)) {
            "You need to hold a starship controller to enable direct control"
        }
        starship.setDirectControlEnabled(!starship.isDirectControlEnabled)
    }

    @CommandAlias("cruise")
    fun onCruise(sender: Player) {
        val ship = getStarshipPiloting(sender)
        if (!StarshipCruising.isCruising(ship)) {
            StarshipCruising.startCruising(sender, ship)
        } else {
            StarshipCruising.stopCruising(sender, ship)
        }
    }

    @CommandAlias("cruisespeed|csp|speedlimit|cruisespeedlimit")
    fun onCruiseSpeed(sender: Player, speedLimit: Int) {
        val ship = getStarshipPiloting(sender)
        ship.speedLimit = speedLimit
        sender msg "&3Speed limit set to $speedLimit"
    }

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
        starship.sendMessage("&c${player.name} was ejected from the starship")
        player msg "&cYou were ejected from the starship"
    }

    @CommandAlias("listships")
    @CommandPermission("starships.listships")
    fun onListShips(sender: Player) {
        var totalShips = 0
        var totalBlocks = 0

        for (starship in ActiveStarships.all()) {
            val pilot: Player? = (starship as? ActivePlayerStarship)?.pilot
            totalShips++
            val size: Int = starship.blockCount
            totalBlocks += size
            val typeName = starship.type.displayName
            val pilotName = pilot?.name ?: "none"
            val worldName = starship.world.name
            sender msg "$typeName piloted by $pilotName with block count $size in world $worldName"
        }

        sender msg "&7Total Ships&8:&b $totalShips&8"
        sender msg "&7Total Blocks in all ships&8:&b $totalBlocks"
    }
}
