package net.horizonsend.ion.server.command.starship

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.features.starship.DeactivatedPlayerStarships
import net.horizonsend.ion.server.features.starship.PilotedStarships
import net.horizonsend.ion.server.features.starship.StarshipDealers
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.starship.ai.AIUtils
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIControllers
import net.horizonsend.ion.server.features.starship.movement.StarshipTeleportation
import net.horizonsend.ion.server.miscellaneous.utils.CARDINAL_BLOCK_FACES
import org.bukkit.Location
import org.bukkit.entity.Player

@CommandPermission("starlegacy.starshipdebug")
@CommandAlias("starshipdebug|sbug")
object StarshipDebugCommand : net.horizonsend.ion.server.command.SLCommand() {
	@Suppress("Unused")
	@Subcommand("teleport")
	fun onTeleport(sender: Player, x: Int, y: Int, z: Int) {
		val riding = getStarshipRiding(sender)
		StarshipTeleportation.teleportStarship(riding, Location(sender.world, x.toDouble(), y.toDouble(), z.toDouble()))
	}

	@Suppress("Unused")
	@Subcommand("thrusters")
	fun onThrusters(sender: Player) {
		val starship = getStarshipRiding(sender)
		for (dir in CARDINAL_BLOCK_FACES) {
			sender.sendRichMessage(starship.thrusterMap[dir].toString())
		}
	}

	@Suppress("Unused")
	@Subcommand("releaseall")
	fun onReleaseAll() {
		ActiveStarships.allControlledStarships().forEach { DeactivatedPlayerStarships.deactivateNow(it) }
	}

	@Suppress("Unused")
	@Subcommand("ai")
	fun onAI(sender: Player) {
		val starship = PilotedStarships[sender] ?: return sender.userError("You are not piloting a starship")

		starship.controller = AIControllers.dumbAI(starship)
		starship.clearPassengers()
		sender.success("success")
	}

	@Suppress("Unused")
	@Subcommand("loadAI")
	fun loadAI(sender: Player, name: String) {
		val (data, schematic) = StarshipDealers.schematicMap.filter { it.key.schematicName == name }.firstNotNullOfOrNull { it } ?: fail { "Sold ship $name not found!" }

		println(data)
		println(schematic)

		AIUtils.createFromClipboard(
			sender.location,
			schematic,
			data.shipType,
			data.displayName,
		) { ship ->
			AIControllers.dumbAI(ship)
		}
	}
}
