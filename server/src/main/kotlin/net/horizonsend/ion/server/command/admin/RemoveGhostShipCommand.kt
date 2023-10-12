package net.horizonsend.ion.server.command.admin

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import net.horizonsend.ion.server.features.starship.DeactivatedPlayerStarships
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import org.bukkit.entity.Player

@CommandPermission("ion.removeghostship")
@CommandAlias("removeghostship")
object RemoveGhostShipCommand : net.horizonsend.ion.server.command.SLCommand() {
	@Default
	@Suppress("unused")
	fun onDeleteGhostShip(sender: Player) {
		val (x, y, z) = Vec3i(sender.location)

		val ship = DeactivatedPlayerStarships.getLockedContaining(sender.world, x, y, z)

		ship?.let {
			it.companion().remove(it._id)
			DeactivatedPlayerStarships.removeState(ship)
		}
	}
}
