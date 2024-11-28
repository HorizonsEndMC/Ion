package net.horizonsend.ion.server.features.starship.factory

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import net.horizonsend.ion.common.database.schema.starships.Blueprint
import net.horizonsend.ion.common.database.slPlayerId
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.common.utils.miscellaneous.toCreditsString
import net.horizonsend.ion.common.utils.text.formatPaginatedMenu
import net.horizonsend.ion.common.utils.text.join
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.common.utils.text.toComponent
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.economy.bazaar.Merchants
import net.horizonsend.ion.server.features.multiblock.type.misc.ShipFactoryMultiblock
import net.horizonsend.ion.server.features.player.CombatTimer
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.blockKey
import net.horizonsend.ion.server.miscellaneous.utils.canAccess
import net.horizonsend.ion.server.miscellaneous.utils.getFacing
import net.horizonsend.ion.server.miscellaneous.utils.getMoneyBalance
import net.horizonsend.ion.server.miscellaneous.utils.isSign
import net.horizonsend.ion.server.miscellaneous.utils.loadClipboard
import net.horizonsend.ion.server.miscellaneous.utils.rightFace
import net.horizonsend.ion.server.miscellaneous.utils.toBukkitBlockData
import net.horizonsend.ion.server.miscellaneous.utils.withdrawMoney
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.AQUA
import net.kyori.adventure.text.format.NamedTextColor.DARK_AQUA
import net.kyori.adventure.text.format.NamedTextColor.DARK_GRAY
import net.kyori.adventure.text.format.NamedTextColor.LIGHT_PURPLE
import net.kyori.adventure.text.format.NamedTextColor.RED
import net.kyori.adventure.text.format.NamedTextColor.WHITE
import net.kyori.adventure.text.format.NamedTextColor.YELLOW
import net.starlegacy.javautil.SignUtils
import org.apache.commons.collections4.map.PassiveExpiringMap
import org.bukkit.block.Sign
import org.bukkit.block.data.BlockData
import org.bukkit.block.data.type.Slab
import org.bukkit.entity.Player
import org.litote.kmongo.and
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import java.util.Collections.synchronizedMap
import java.util.LinkedList
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set
import kotlin.math.roundToInt

object StarshipFactories : IonServerComponent() {
	val missingMaterialsCache = synchronizedMap(PassiveExpiringMap<UUID, Map<PrintItem, Int>>(TimeUnit.MINUTES.toMillis(5L)))

	@Synchronized
	fun process(player: Player, sign: Sign, creditPrint: Boolean) {
		val blueprintOwner = UUID.fromString(sign.getLine(1)).slPlayerId
		val blueprintName = sign.getLine(2)
		val blueprint = Blueprint.col.findOne(and(Blueprint::name eq blueprintName, Blueprint::owner eq blueprintOwner))
			?: return player.userError("Blueprint not found")

		if (!blueprint.canAccess(player)) {
			player.userError("You don't have access to that blueprint")
			return
		}

		if (CombatTimer.isPvpCombatTagged(player)) {
			player.userError("Cannot activate Ship Factories while in combat")
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
		val signs = Long2ObjectOpenHashMap<SignUtils.SignData>()

		val targetX = sign.x + direction.modX * 3 + sideDirection.modX
		val targetY = sign.y
		val targetZ = sign.z + direction.modZ * 3 + sideDirection.modZ

		for (pos in schematic.region) {
			val baseBlock = schematic.getFullBlock(pos)
			val data = baseBlock.toImmutableState().toBukkitBlockData()
			if (data.material.isAir) {
				continue
			}

			val key = blockKey(pos.x() + offsetX + targetX, pos.y() + offsetY + targetY, pos.z() + offsetZ + targetZ)

			blocks[key] = data

			if (data.material.isSign) {
				signs[key] = SignUtils.readSignData(baseBlock.nbt)
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

		if (missingItems.isNotEmpty()) {
			missingMaterialsCache[player.uniqueId] = missingItems

			val sorted = missingItems.entries.toList().sortedBy { it.value }

			player.userError("Missing Materials: ")

			player.sendMessage(formatPaginatedMenu(
				entries = sorted.size,
				command = "/shipfactory listmissing",
				currentPage = 1,
			) { index ->
				val (item, count) = sorted[index]

				return@formatPaginatedMenu ofChildren(
					item.toComponent(color = RED),
					text(": ", DARK_GRAY),
					text(count, WHITE)
				)
			})

			player.userError("Use <italic><underlined><click:run_command:/shipfactory listmissing all>/shipfactory listmissing all</click></italic> to list all missing materials in one message.")

			return true
		}

		return false
	}

	private fun chargeMoney(printer: StarshipFactoryPrinter, player: Player) {
		val usedCredits = printer.usedCredits
		player.withdrawMoney(usedCredits)
		player.information("Charged ${usedCredits.toCreditsString()}")
	}

	fun getPrintItemCountString(map: Map<PrintItem, Int>): Component {
		val list = LinkedList<Component>()
		var color = false // used to make it alternate colors

		val entriesByValue = map.entries.toList().sortedBy { it.value }

		for ((item, count) in entriesByValue) {
			if (count == 0) {
				continue
			}

			color = !color

			if (color) {
				list.add(ofChildren(item.toComponent(color = DARK_AQUA), text(": ", DARK_GRAY), text(count, AQUA)))
				continue
			}

			list.add(ofChildren(item.toComponent(color = RED), text(": ", DARK_GRAY), text(count, LIGHT_PURPLE)))
		}

		return list.join(separator = text(", ", YELLOW))
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
