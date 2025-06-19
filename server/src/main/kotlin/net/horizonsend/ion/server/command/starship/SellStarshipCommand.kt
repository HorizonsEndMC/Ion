package net.horizonsend.ion.server.command.starship

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Optional
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.schema.starships.Blueprint
import net.horizonsend.ion.common.database.schema.starships.PlayerSoldShip
import net.horizonsend.ion.common.extensions.serverError
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.utils.miscellaneous.roundToHundredth
import net.horizonsend.ion.common.utils.text.GsonComponentString
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_MEDIUM_GRAY
import net.horizonsend.ion.common.utils.text.gson
import net.horizonsend.ion.common.utils.text.miniMessage
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.common.utils.text.toCreditComponent
import net.horizonsend.ion.common.utils.text.wrap
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.nations.region.Regions
import net.horizonsend.ion.server.features.nations.region.types.RegionTopLevel
import net.horizonsend.ion.server.features.npcs.database.UniversalNPCs
import net.horizonsend.ion.server.features.npcs.database.type.UniversalNPCTypes
import net.horizonsend.ion.server.features.starship.StarshipSchematic
import net.horizonsend.ion.server.features.starship.StarshipType
import net.horizonsend.ion.server.features.starship.destruction.StarshipDestruction
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.createData
import net.horizonsend.ion.server.miscellaneous.utils.slPlayerId
import net.horizonsend.ion.server.miscellaneous.utils.withdrawMoney
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.litote.kmongo.and
import org.litote.kmongo.eq
import org.litote.kmongo.ne
import kotlin.math.roundToInt

@CommandAlias("starshipsell")
object SellStarshipCommand : SLCommand() {
	@Default
	fun onSellStarship(sender: Player, className: String, shipName: String, price: Double, @Optional description: String?, @Optional priceConfirm: Double?) = asyncCommand(sender) {
		requireEconomyEnabled()
		requireNotInCombat(sender)
		val starship = getStarshipPiloting(sender)

		val listingTax = price * 0.10
		// Do a fuzzy comparison to avoid floating point inaccuracy
		failIf(priceConfirm?.roundToInt() != listingTax.roundToInt()) {
			"Listing a ship requires a tax of 10% of the asking price. You must acknowledge the price to list the ship." +
				"Enter /starshipsell $className $shipName $price ${description ?: ""} ${listingTax.roundToHundredth()} to confirm."
		}
		requireMoney(sender, listingTax)

		// Verify all sold ships of the same class name are the same type
		failIf(PlayerSoldShip.any(and(PlayerSoldShip::owner eq sender.slPlayerId, PlayerSoldShip::className eq className, PlayerSoldShip::type ne starship.type.name))) {
			"All ships of the same class must be of the same type!"
		}

		// Start verifying territory
		val territoryTopLevel = Regions.find(sender.location).filter { it is RegionTopLevel }
		val territory = territoryTopLevel.firstOrNull() ?: fail { "You must be in a region to sell starships!" }
		// Find any NPCs in the region the player is trying to sell the ship.
		val npcs = UniversalNPCs.getAll(UniversalNPCTypes.PLAYER_SHIP_DEALER).filter { territory.contains(it.npc.storedLocation) }
		failIf(npcs.none { it.metaData.sellers.contains(sender.uniqueId) }) { "There are no NPCs selling your ships in this region!" }

		var pilotLoc = Vec3i(sender.location)
		failIf(!starship.isWithinHitbox(pilotLoc.x, pilotLoc.y, pilotLoc.z, 1)) { "Must be inside the ship." }

		// Do the most intensive task at the end so compute isn't wasted if anything else fails
		val (clipboard, blockCount) = Tasks.getSyncBlocking { StarshipSchematic.createSchematic(starship) to starship.blocks.size }
		val clipboardData = Blueprint.createData(clipboard)

		pilotLoc = Vec3i(pilotLoc.x - clipboard.origin.x(), pilotLoc.y - clipboard.origin.y(), pilotLoc.z - clipboard.origin.z())

		Tasks.sync {
			StarshipDestruction.vanish(starship, false) { vanishResult ->
				if (vanishResult) {
					sender.sendMessage(template(Component.text("{0} has been withdrawn from your account.", HE_MEDIUM_GRAY), listingTax.toCreditComponent()))
					sender.withdrawMoney(listingTax)

					val parsedDescription = description?.let(miniMessage::deserialize)?.wrap(150)?.map(gson::serialize)
					createSoldShip(sender, territory.id, className, shipName, parsedDescription, price, pilotLoc, starship.type, blockCount, clipboardData)
				}
				else {
					sender.serverError("There was an error when processing the starship, it will not be marked for sale. Please contact staff for a refund if it was removed.")
				}
			}
		}
	}

	private fun createSoldShip(owner: Player, territory: Oid<*>, className: String, shipName: String, description: List<GsonComponentString>?, price: Double, pilotLocOffset: Vec3i, type: StarshipType, blockCount: Int, clipboardData: String) {
		PlayerSoldShip.create(
			owner = owner.slPlayerId,
			territory = territory,
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
