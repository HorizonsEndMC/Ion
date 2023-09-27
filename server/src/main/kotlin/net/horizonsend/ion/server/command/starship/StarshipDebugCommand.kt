package net.horizonsend.ion.server.command.starship

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.server.features.starship.DeactivatedPlayerStarships
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.starship.active.ai.AISpawningManager.handleSpawn
import net.horizonsend.ion.server.features.starship.control.controllers.ai.StarfighterCombatController
import net.horizonsend.ion.server.features.starship.control.controllers.ai.util.PathfindingController
import net.horizonsend.ion.server.features.starship.control.movement.AIPathfinding
import net.horizonsend.ion.server.features.starship.movement.StarshipTeleportation
import net.horizonsend.ion.server.miscellaneous.utils.CARDINAL_BLOCK_FACES
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.text
import net.horizonsend.ion.server.miscellaneous.utils.title
import org.bukkit.Location
import org.bukkit.World
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
	@Subcommand("triggerSpawn")
	fun triggerSpawn(sender: Player) {
		handleSpawn()
	}

	@Suppress("Unused")
	@Subcommand("debugReason")
	fun debugReason(sender: Player) {
		val (x, y, z) = Vec3i(sender.location)

		val ship = ActiveStarships.allControlledStarships().minBy { it.centerOfMass.distance(x, y, z) }
		val controller = ship.controller as? StarfighterCombatController

		val nodes = controller?.navigationEngine?.trackedSections

		val secX = x.shr(4)
		val secY = (y - sender.world.minHeight).shr(4)
		val secZ = z.shr(4)

		val currentNode = nodes?.firstOrNull { it.location == Vec3i(secX, secY, secZ) }

		sender.title(currentNode?.reason?.text() ?: "Node not tracked.".text(), "".text())
	}

//	@Suppress("Unused")
//	@Subcommand("testAStar")
//	fun testAStar(sender: Player, searchDistance: Int, x: Int, y: Int, z: Int) = Tasks.async {
//		val wrapper = wrapperB(
//			sender,
//			searchDistance
//		)
//
//		wrapper.adjustPosition(true)
//		val nodes = wrapper.getNavigationPoints(Vec3i(x, y, z)).map { it.center }
//
//		for (node in nodes) {
//			highlightBlock(sender, node, 60L)
//		}
//	}

	class wrapperB(
		val player: Player,
		override var chunkSearchRadius: Int
	): PathfindingController {
		override val trackedSections: MutableSet<AIPathfinding.SectionNode> = mutableSetOf()

		override fun getWorld(): World = player.world

		override fun getCenter(): Location = player.location
		override fun getCenterVec3i(): Vec3i = Vec3i(getCenter())
	}
}
