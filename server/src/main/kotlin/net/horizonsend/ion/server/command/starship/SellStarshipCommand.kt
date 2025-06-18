package net.horizonsend.ion.server.command.starship

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Optional
import net.horizonsend.ion.common.database.schema.starships.Blueprint
import net.horizonsend.ion.common.database.schema.starships.PlayerSoldShip
import net.horizonsend.ion.common.extensions.serverError
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.utils.text.GsonComponentString
import net.horizonsend.ion.common.utils.text.gson
import net.horizonsend.ion.common.utils.text.miniMessage
import net.horizonsend.ion.common.utils.text.wrap
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.starship.StarshipSchematic
import net.horizonsend.ion.server.features.starship.StarshipType
import net.horizonsend.ion.server.features.starship.destruction.StarshipDestruction
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.createData
import net.horizonsend.ion.server.miscellaneous.utils.slPlayerId
import org.bukkit.entity.Player

@CommandAlias("starshipsell")
object SellStarshipCommand : SLCommand() {
	@Default
	fun onSellStarship(sender: Player, className: String, shipName: String, price: Double, @Optional description: String?, ) = asyncCommand(sender) {
		requireNotInCombat(sender)
		val starship = getStarshipPiloting(sender)

		var pilotLoc = Vec3i(sender.location)
		failIf(!starship.isWithinHitbox(pilotLoc.x, pilotLoc.y, pilotLoc.z, 1)) { "Must be inside the ship." }

		val (clipboard, blockCount) = Tasks.getSyncBlocking { StarshipSchematic.createSchematic(starship) to starship.blocks.size }
		val clipboardData = Blueprint.createData(clipboard)

		pilotLoc = Vec3i(pilotLoc.x - clipboard.origin.x(), pilotLoc.y - clipboard.origin.y(), pilotLoc.z - clipboard.origin.z())

		Tasks.sync {
			StarshipDestruction.vanish(starship, false) { vanishResult ->
				if (vanishResult) {
					val parsedDescription = description?.let(miniMessage::deserialize)?.wrap(150)?.map(gson::serialize)
					createSoldShip(sender, className, shipName, parsedDescription, price, pilotLoc, starship.type, blockCount, clipboardData)
				}
				else {
					sender.serverError("There was an error when processing the starship, it will not be marked for sale. Please contact staff for a refund if it was removed.")
				}
			}
		}
	}

	private fun createSoldShip(owner: Player, className: String, shipName: String, description: List<GsonComponentString>?, price: Double, pilotLocOffset: Vec3i, type: StarshipType, blockCount: Int, clipboardData: String) {
		PlayerSoldShip.create(
			owner = owner.slPlayerId,
			className = className,
			name = shipName,
			description = description,
			price = price,
			type = type.name,
			pilotLoc = pilotLocOffset,
			size = blockCount,
			data = clipboardData,
		)

		owner.success("Ship marked for sale.")
	}
}
