package net.horizonsend.ion.server.command.starship

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.features.explosion.Explosion.Companion.explode
import net.horizonsend.ion.server.features.starship.DeactivatedPlayerStarships
import net.horizonsend.ion.server.features.starship.PilotedStarships
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.starship.controllers.ai.AIControllers
import net.horizonsend.ion.server.features.starship.movement.StarshipTeleportation
import net.horizonsend.ion.server.miscellaneous.utils.CARDINAL_BLOCK_FACES
import org.bukkit.Location
import org.bukkit.Material
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
	fun onReleaseAll(sender: Player) {
		ActiveStarships.allControlledStarships().forEach { DeactivatedPlayerStarships.deactivateNow(it) }
	}

	@Suppress("Unused")
	@Subcommand("explosion")
	fun explosion(sender: Player) {
		val starship = PilotedStarships[sender] ?: return sender.userError("You are not piloting a starship")
		val controller = starship.controller ?: return sender.userError("NO CONTROLLER")

		sender.world.explode(
			sender.location,
			10f,
			controller,
			useFire = true,
			applyPhysics = false,
			fireType = Material.SOUL_FIRE
		)
	}

	@Suppress("Unused")
	@Subcommand("ai")
	fun onAI(sender: Player) {
		val starship = PilotedStarships[sender] ?: return sender.userError("You are not piloting a starship")

		starship.controller = AIControllers.dumbAI(starship)
		starship.clearPassengers()
		sender.success("success")
	}
}
