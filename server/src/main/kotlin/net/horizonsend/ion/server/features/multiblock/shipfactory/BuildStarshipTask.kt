package net.horizonsend.ion.server.features.multiblock.shipfactory

import com.sk89q.worldedit.extent.clipboard.Clipboard
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import it.unimi.dsi.fastutil.longs.LongIterator
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.miscellaneous.lines
import net.horizonsend.ion.server.miscellaneous.minecraft
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.minecraft.core.BlockPos
import net.starlegacy.database.schema.starships.Blueprint
import net.starlegacy.feature.transport.Extractors
import net.starlegacy.javautil.SignUtils
import net.starlegacy.util.VAULT_ECO
import net.starlegacy.util.Vec3i
import net.starlegacy.util.blockKeyX
import net.starlegacy.util.blockKeyY
import net.starlegacy.util.blockKeyZ
import net.starlegacy.util.component1
import net.starlegacy.util.component2
import net.starlegacy.util.component3
import net.starlegacy.util.getBlockDataSafe
import net.starlegacy.util.getFacing
import net.starlegacy.util.isSign
import net.starlegacy.util.nms
import net.starlegacy.util.readSignText
import net.starlegacy.util.rightFace
import net.starlegacy.util.toBukkitBlockData
import net.starlegacy.util.toLocation
import net.starlegacy.util.toVector
import org.bukkit.block.Sign
import org.bukkit.block.data.BlockData
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable
import kotlin.math.min

/**
 * This task is ran to increment progress on the blueprint
 * Progress is as an index of the bounding box
 *
 *
 *
 **/
class BuildStarshipTask(
	private val multiblockSign: Sign,
	private val taskRunner: Player,
	offsets: Vec3i,
	private val inventory: Inventory,
	blueprint: Blueprint,
	private val leaveOneOfEach: Boolean = false
) : BukkitRunnable() {
	/** The origin corner of the blueprint **/
	private val originX: Int
	private val originY: Int
	private val originZ: Int

	private var progress = 0L
	private val maxProgress: Long

	val clipboard: Clipboard

	private val blocks = Long2ObjectOpenHashMap<BlockData>()
	var blocksIterator: LongIterator
	private val signs = Long2ObjectOpenHashMap<Array<Component>>()

	val price = Blueprint.calculateBlueprintCost(blueprint)
	private var consumedMoney = 0.0

	private val availableItems = mutableMapOf<PrintItem, Int>() //TODO redo this system
	private val usedItems = mutableMapOf<PrintItem, Int>()
	val missingItems = mutableMapOf<PrintItem, Int>()

	val playerBalance get() = VAULT_ECO.getBalance(taskRunner)

	// Prepare the blueprint on initialization
	init {
		val schematic = blueprint.loadClipboard()
		clipboard = schematic

		val direction = multiblockSign.getFacing().oppositeFace
		val sideDirection = direction.rightFace

		val negativeX = if (direction.modX == 0) sideDirection.modX < 0 else direction.modX < 0
		val negativeZ = if (direction.modZ == 0) sideDirection.modZ < 0 else direction.modZ < 0

		originX = if (negativeX) schematic.region.minimumPoint.x else schematic.region.maximumPoint.x
		originY = schematic.region.minimumPoint.y
		originZ = if (negativeZ) schematic.region.minimumPoint.z else schematic.region.maximumPoint.z

		val (offsetX, offsetY, offsetZ) = offsets

		val targetX = multiblockSign.x + direction.modX * 3 + sideDirection.modX  + offsetX
		val targetY = multiblockSign.y + offsetY
		val targetZ = multiblockSign.z + direction.modZ * 3 + sideDirection.modZ  + offsetZ

		for (pos in schematic.region) {
			val baseBlock = schematic.getFullBlock(pos)
			val data = baseBlock.toImmutableState().toBukkitBlockData()

			if (data.material.isAir) continue

			val blockPos = BlockPos(
				pos.x + offsetX + targetX,
				pos.y + offsetY + targetY,
				pos.z + offsetZ + targetZ
			)

			// Store offset location as long.
			val key = blockPos.asLong()

			blocks[key] = data

			if (data.material.isSign) signs[key] = baseBlock.nbtData?.let { readSignText(blockPos, data.nms, it) }
		}

		blocksIterator = blocks.keys.iterator()

		maxProgress = BlockPos.asLong(
			if (negativeX) schematic.region.maximumPoint.x else schematic.region.minimumPoint.x,
			schematic.region.minimumPoint.y,
			if (negativeZ) schematic.region.maximumPoint.z else schematic.region.minimumPoint.z
		)

		calculateMaterialCounts()
	}

	/** Start the ship factory task **/
	fun start(delay: Long) {
		shipFactoryTasks.add(this)
		this.runTaskTimer(IonServer, 0, delay)
	}

	override fun run() {
		// Catch any scenario it was started without start()
		if (!shipFactoryTasks.contains(this)) shipFactoryTasks.add(this)

		if (!blocksIterator.hasNext()) return cancelFactory(text("Ship factory at ${Vec3i(multiblockSign.location)} has finished its task!", NamedTextColor.GREEN))

		val block = blocksIterator.nextLong()
		progress = block
		val blockData = blocks[progress]

		processBlock(blockData)
		multiblockSign.line(2)
		removeItem(blockData)
	}

	fun removeItem(item: BlockData) {
		val printItem = PrintItem[item] ?: return

		for (item: ItemStack? in inventory.contents) {
			if (item == null || item.type.isAir) continue

			if (printItem != PrintItem(item)) continue

			item.amount-- //TODO one stack at a time

			if (item.amount == 0) break
		}
	}

	private fun processBlock(blockData: BlockData) {
		val blockPos = BlockPos.of(progress)
		val (x, y, z) = blockPos

		println(Vec3i(blockPos.toLocation(multiblockSign.world)))
		val oldData = getBlockDataSafe(multiblockSign.world, x, y, z) ?: return

		if (!oldData.material.isAir) return
		if (!tryPrintBlock(blockData)) return

		multiblockSign.world.setBlockData(x, y, z, blockData)
		if (blockData.material == Extractors.EXTRACTOR_BLOCK) Extractors.add(multiblockSign.world, Vec3i(x, y, z))

		if (blockData.material.isSign) {
			val oldSign = multiblockSign.world.getBlockAt(x, y, z).state as Sign

			val lines = signs[progress]

			oldSign.lines(lines)
		}

		val price = ShipFactoryMaterialCosts.getPrice(blockData)
		//TODO withdraw money
		consumedMoney += price
	}

	private fun tryPrintBlock(data: BlockData): Boolean {
		val item = PrintItem[data] ?: return false
		val amount = StarshipFactories.getRequiredAmount(data)

		val count = getAvailable(item)

		if (count < amount) {
			incrementMissing(item, amount)
			return false
		}

		if (playerBalance < ShipFactoryMaterialCosts.getPrice(data)) return false

		decrementAvailable(item, count, amount)
		incrementUsed(item, amount)
		return true
	}

	private fun getAvailable(item: PrintItem) = availableItems.getOrDefault(item, 0)

	private fun incrementMissing(item: PrintItem, delta: Int) {
		missingItems[item] = missingItems.getOrDefault(item, 0) + delta
	}

	private fun decrementAvailable(item: PrintItem, old: Int, delta: Int) {
		// old is passed for efficiency
		availableItems[item] = old - delta
	}

	private fun incrementUsed(item: PrintItem, delta: Int) {
		usedItems[item] = usedItems.getOrDefault(item, 0) + delta
	}

	fun cancelFactory(message: Component) {
		taskRunner.sendMessage(message)
		cancel()
	}

	private fun calculateMaterialCounts() {
		countMaterials()
		if (leaveOneOfEach) ignoreOneItemOfEachMaterial()
	}

	private fun countMaterials() {
		for (item: ItemStack? in inventory.contents) {
			if (item == null || item.type.isAir) {
				continue
			}

			val printItem = PrintItem(item)

			val oldAmount = availableItems.getOrDefault(printItem, 0)
			availableItems[printItem] = oldAmount + item.amount
		}
	}

	private fun ignoreOneItemOfEachMaterial() {
		for ((material, count) in availableItems) {
			if (count > 0) {
				availableItems[material] = count - 1
			}
		}
	}

	companion object {
		/** Running ship factories **/
		val shipFactoryTasks = mutableListOf<BuildStarshipTask>()

		/** Iterate through the bounding box of the blueprint **/
		fun iterateBlocks() {  }

		/**
		 *  Find the first non-air block part of the ship
		 * 	Stored as long, provided from BlockPos key of offsets.
		 **/
	}
}
