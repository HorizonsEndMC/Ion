package net.starlegacy.feature.starship.factory

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import java.util.LinkedList
import java.util.Locale
import java.util.UUID
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set
import kotlin.math.roundToInt
import net.starlegacy.SLComponent
import net.starlegacy.database.schema.starships.Blueprint
import net.starlegacy.database.slPlayerId
import net.starlegacy.feature.economy.bazaar.Merchants
import net.starlegacy.feature.multiblock.Multiblocks
import net.starlegacy.feature.multiblock.misc.ShipFactoryMultiblock
import net.starlegacy.util.SignUtils
import net.starlegacy.util.Tasks
import net.starlegacy.util.blockKey
import net.starlegacy.util.getFacing
import net.starlegacy.util.getMoneyBalance
import net.starlegacy.util.isSign
import net.starlegacy.util.msg
import net.starlegacy.util.rightFace
import net.starlegacy.util.toBukkitBlockData
import net.starlegacy.util.toCreditsString
import net.starlegacy.util.withdrawMoney
import org.bukkit.block.Sign
import org.bukkit.block.data.BlockData
import org.bukkit.block.data.type.Slab
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.litote.kmongo.and
import org.litote.kmongo.eq
import org.litote.kmongo.findOne

object StarshipFactories : SLComponent() {
	override fun supportsVanilla(): Boolean {
		return true
	}

	override fun onEnable() {
		subscribe<PlayerInteractEvent> { event ->
			val block = event.clickedBlock
			val sign = block?.state as? Sign ?: return@subscribe
			if (Multiblocks[sign] !is ShipFactoryMultiblock) {
				return@subscribe
			}

			val leftClick = event.action == Action.LEFT_CLICK_BLOCK &&
				event.player.hasPermission("starlegacy.factory.print.credit")

			Tasks.async {
				process(event.player, sign, leftClick)
			}
		}
	}

	@Synchronized
	private fun process(player: Player, sign: Sign, creditPrint: Boolean) {
		val blueprintOwner = UUID.fromString(sign.getLine(1)).slPlayerId
		val blueprintName = sign.getLine(2)
		val blueprint = Blueprint.col.findOne(and(Blueprint::name eq blueprintName, Blueprint::owner eq blueprintOwner))
			?: return player msg "&cBlueprint not found"

		if (!blueprint.canAccess(player)) {
			player msg "&cYou don't have access to that blueprint"
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

			player msg "&aComplete!"
		}
	}

	private fun sendMissing(printer: StarshipFactoryPrinter, player: Player): Boolean {
		val missingItems = printer.missingItems
		val missingCredits = printer.missingCredits

		if (missingItems.isNotEmpty() || missingCredits > 0) {
			if (missingItems.isNotEmpty()) {
				val string = getPrintItemCountString(missingItems)
				player msg "&e&lMissing Materials &8:&b-&8:&r $string"
			}

			if (missingCredits > 0) {
				player msg "&e&lMissing Credits &8:&b-&8:&r ${missingCredits.toCreditsString()}"
			}

			return true
		}

		return false
	}

	private fun chargeMoney(printer: StarshipFactoryPrinter, player: Player) {
		val usedCredits = printer.usedCredits
		player.withdrawMoney(usedCredits)
		player msg "&7Charged &b${usedCredits.toCreditsString()}"
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
				list.add("&3$item&8: &b$count")
				continue
			}

			list.add("&c$item&8: &d$count")
		}

		return list.joinToString("&e, ").lowercase(Locale.getDefault())
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
