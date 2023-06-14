package net.starlegacy.feature.starship.factory

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.common.database.schema.starships.Blueprint
import net.horizonsend.ion.common.database.slPlayerId
import net.horizonsend.ion.server.miscellaneous.canAccess
import net.horizonsend.ion.server.miscellaneous.loadClipboard
import net.starlegacy.feature.economy.bazaar.Merchants
import net.horizonsend.ion.server.features.multiblock.shipfactory.ShipFactoryMultiblock
import net.starlegacy.javautil.SignUtils
import net.starlegacy.util.Tasks
import net.starlegacy.util.blockKey
import net.starlegacy.util.getFacing
import net.starlegacy.util.getMoneyBalance
import net.starlegacy.util.isSign
import net.starlegacy.util.rightFace
import net.starlegacy.util.toBukkitBlockData
import net.starlegacy.util.toCreditsString
import net.starlegacy.util.withdrawMoney
import org.bukkit.block.Sign
import org.bukkit.block.data.BlockData
import org.bukkit.block.data.type.Slab
import org.bukkit.entity.Player
import org.litote.kmongo.and
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import java.util.LinkedList
import java.util.Locale
import java.util.UUID
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set
import kotlin.math.roundToInt

object StarshipFactories : IonServerComponent() {
	override fun vanillaOnly(): Boolean {
		return true
	}

	override fun onEnable() {
//		listen<PlayerInteractEvent> { event ->
//			val block = event.clickedBlock
//			val sign = block?.state as? Sign ?: return@listen
//			if (Multiblocks[sign] !is ShipFactoryMultiblock) {
//				return@listen
//			}
//
//			val leftClick = event.action == Action.LEFT_CLICK_BLOCK &&
//				event.player.hasPermission("starlegacy.factory.print.credit")
//
//			Tasks.async {
//				process(event.player, sign, leftClick)
//			}
//		}
	}

	@Synchronized
	private fun process(player: Player, sign: Sign, creditPrint: Boolean) {
		val blueprintOwner = UUID.fromString(sign.getLine(1)).slPlayerId
		val blueprintName = sign.getLine(2)
		val blueprint = Blueprint.col.findOne(and(Blueprint::name eq blueprintName, Blueprint::owner eq blueprintOwner))
			?: return player.userError("Blueprint not found")

		if (!blueprint.canAccess(player)) {
			player.userError("You don't have access to that blueprint")
			return
		}

		val schematic = blueprint.loadClipboard()

		val direction = sign.getFacing().oppositeFace
		val sideDirection = direction.rightFace
		val negativeX = if (direction.modX == 0) sideDirection.modX < 0 else direction.modX < 0
		val negativeZ = if (direction.modZ == 0) sideDirection.modZ < 0 else direction.modZ < 0
		val x = if (negativeX) schematic.region.minimumPoint.x else schematic.region.maximumPoint.x
		val y = schematic.region.minimumPoint.y
		val z = if (negativeZ) schematic.region.minimumPoint.z else schematic.region.maximumPoint.z

		val offsetX = (x - schematic.region.center.x * 2).roundToInt()
		val offsetY = (-y.toDouble()).roundToInt()
		val offsetZ = (z - schematic.region.center.z * 2).roundToInt()

		val blocks = Long2ObjectOpenHashMap<BlockData>()
		val signs = Long2ObjectOpenHashMap<Array<String>>()

		val targetX = sign.x + direction.modX * 3 + sideDirection.modX
		val targetY = sign.y
		val targetZ = sign.z + direction.modZ * 3 + sideDirection.modZ

		for (pos in schematic.region) {
			val baseBlock = schematic.getFullBlock(pos)
			val data = baseBlock.toImmutableState().toBukkitBlockData()
			if (data.material.isAir) {
				continue
			}

			val key = blockKey(pos.x + offsetX + targetX, pos.y + offsetY + targetY, pos.z + offsetZ + targetZ)

			blocks[key] = data

			if (data.material.isSign) {
				signs[key] = SignUtils.fromCompoundTag(baseBlock.nbtData)
			}
		}

		Tasks.getSyncBlocking {
			val world = sign.world
			val inventory = ShipFactoryMultiblock.getStorage(sign)
			val availableCredits = player.getMoneyBalance()

			val printer = StarshipFactoryPrinter(world, inventory, blocks, signs, availableCredits)

			printer.print()

			chargeMoney(printer, player)

			if (sendMissing(printer, player)) {
				return@getSyncBlocking
			}

			player.success("Complete!")
		}
	}

	private fun sendMissing(printer: StarshipFactoryPrinter, player: Player): Boolean {
		val missingItems = printer.missingItems
		val missingCredits = printer.missingCredits

		if (missingItems.isNotEmpty() || missingCredits > 0) {
			if (missingItems.isNotEmpty()) {
				val string = getPrintItemCountString(missingItems)
				player.userError("Missing Materials: $string")
			}

			if (missingCredits > 0) {
				player.userError("Missing Credits: ${missingCredits.toCreditsString()}")
			}

			return true
		}

		return false
	}

	private fun chargeMoney(printer: StarshipFactoryPrinter, player: Player) {
		val usedCredits = printer.usedCredits
		player.withdrawMoney(usedCredits)
		player.information("Charged ${usedCredits.toCreditsString()}")
	}

	fun getPrintItemCountString(map: Map<PrintItem, Int>): String {
		val list = LinkedList<String>()
		var color = false // used to make it alternate colors

		val entriesByValue = map.entries.toList().sortedBy { it.value }

		for ((item, count) in entriesByValue) {
			if (count == 0) {
				continue
			}

			color = !color

			if (color) {
				list.add("<dark_aqua>$item<dark_gray>: <aqua>$count")
				continue
			}

			list.add("<red>$item<dark_gray>: <light_purple>$count")
		}

		return list.joinToString("<yellow>, ").lowercase(Locale.getDefault())
	}

	fun getRequiredAmount(data: BlockData): Int {
		if (data is Slab) {
			return if (data.type == Slab.Type.DOUBLE) 2 else 1
		}

		return 1
	}

	fun getPrice(data: BlockData): Double? {
		val printItem = PrintItem[data] ?: return null
		val merchantPrice = Merchants.getPrice(printItem.itemString) ?: return null
		return merchantPrice * 1.5
	}
}
