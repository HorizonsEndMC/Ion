package net.horizonsend.ion.server.command.admin

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.schema.starships.StarshipData
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.common.utils.text.toComponent
import net.horizonsend.ion.server.features.starship.DeactivatedPlayerStarships
import net.horizonsend.ion.server.features.starship.PilotedStarships
import net.horizonsend.ion.server.features.starship.StarshipComputers
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.bukkitWorld
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent

@CommandPermission("ion.removeghostship")
@CommandAlias("removeghostship")
object RemoveGhostShipCommand : net.horizonsend.ion.server.command.SLCommand() {
	@Default
	@Suppress("unused")
	fun onDeleteGhostShip(sender: Player) = asyncCommand(sender) {
		val selection = requireSelection(sender)

		val visited = mutableSetOf<Oid<out StarshipData>>()

		for (pos in selection) {
			val x = pos.x()
			val y = pos.y()
			val z = pos.z()

			val ship = DeactivatedPlayerStarships.getContaining(sender.world, x, y, z) ?: continue

			if (visited.contains(ship._id)) continue

			@Suppress("DEPRECATION") val block = ship.bukkitWorld().getBlockAtKey(ship.blockKey)
			if (block.type == StarshipComputers.COMPUTER_TYPE) {
				visited.add(ship._id)
				continue
			}

			Tasks.getSyncBlocking {
				val event = BlockBreakEvent(sender.world.getBlockAt(x, y, z), sender).callEvent()
				if (!event) return@getSyncBlocking

				val name = PilotedStarships.getDisplayName(ship)

				DeactivatedPlayerStarships.destroyAsync(ship) {
					sender.sendMessage(ofChildren("Removed ".toComponent(), name))
				}

				visited.add(ship._id)
			}
		}

		sender.information("Removed ${visited.size} ships.")
	}
}
