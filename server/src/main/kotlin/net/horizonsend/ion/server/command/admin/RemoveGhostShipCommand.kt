package net.horizonsend.ion.server.command.admin

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.common.utils.text.toComponent
import net.horizonsend.ion.server.features.starship.DeactivatedPlayerStarships
import net.horizonsend.ion.server.features.starship.PilotedStarships
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent

@CommandPermission("ion.removeghostship")
@CommandAlias("removeghostship")
object RemoveGhostShipCommand : net.horizonsend.ion.server.command.SLCommand() {
	@Default
	@Suppress("unused")
	fun onDeleteGhostShip(sender: Player) = asyncCommand(sender) {
		val selection = requireSelection(sender)

		for (pos in selection) {
			val x = pos.x
			val y = pos.y
			val z = pos.z

			val ship = DeactivatedPlayerStarships[sender.world, x, y, z] ?: continue

			Tasks.sync {
				val event = BlockBreakEvent(sender.world.getBlockAt(x, y, z), sender).callEvent()
				if (!event) return@sync

				val name = PilotedStarships.getDisplayName(ship)

				DeactivatedPlayerStarships.removeState(ship)
				ship.companion().remove(ship._id)

				sender.sendMessage(ofChildren("Removed ".toComponent(), name))
			}
		}
	}
}
