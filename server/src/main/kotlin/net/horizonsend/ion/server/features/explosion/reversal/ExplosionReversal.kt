package net.horizonsend.ion.server.features.explosion.reversal

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.explosion.reversal.Regeneration.pulse
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.block.BlockState
import org.bukkit.block.Chest
import org.bukkit.block.DoubleChest
import org.bukkit.block.data.BlockData
import org.bukkit.event.EventHandler
import org.bukkit.event.block.BlockExplodeEvent
import org.bukkit.event.world.WorldSaveEvent
import org.bukkit.inventory.DoubleChestInventory
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import java.io.IOException
import java.util.LinkedList
import java.util.function.Consumer
import kotlin.math.abs
import kotlin.math.roundToLong


object ExplosionReversal : IonServerComponent() {
	private val settings get() = IonServer.configuration.explosionRegenConfig

	var worldData: WorldData? = null
		private set

	override fun onEnable() {
		initializeWorldData()
		scheduleRegen()
	}

	private fun initializeWorldData() {
		worldData = WorldData()
	}

	private fun scheduleRegen() = Tasks.syncRepeat(5L, 5L) {
		try {
			pulse()
		} catch (e: IOException) {
			e.printStackTrace()
		}
	}

	@EventHandler
	fun onWorldSave(event: WorldSaveEvent) {
		worldData!!.save(event.world)
	}

	override fun onDisable() {
		saveAll()
	}

	private fun saveAll() {
		Bukkit.getWorlds().forEach(Consumer { world: World? ->
			worldData!!.save(
				world!!
			)
		})
	}

	fun getExplodedTime(
		explosionX: Double, explosionY: Double, explosionZ: Double,
		blockX: Int, blockY: Int, blockZ: Int,
	): Long {
		val now = System.currentTimeMillis()
		val distance = abs(explosionX - blockX) + abs(explosionY - blockY) + abs(explosionZ - blockZ)
		val distanceDelayMs = settings!!.distanceDelay * 1000
		val cap = settings!!.getDistanceDelayCap()
		val offset: Long = (cap.coerceAtMost(cap - distance) * distanceDelayMs).roundToLong()
		return now + offset
	}

	fun onBlockExplode(event: BlockExplodeEvent) {
		val world = event.block.world
		val location: Location = event.block.location
		val blockList: MutableList<Block> = event.blockList()
		processExplosion(world, location, blockList)
	}

	private fun processExplosion(world: World, explosionLocation: Location, list: MutableList<Block>) {
		if (settings.getIgnoredWorlds().contains(world.name)) return
		if (list.isEmpty()) return

		val explodedBlockDataList: MutableList<ExplodedBlockData> = LinkedList()
		val eX: Double = explosionLocation.getX()
		val eY: Double = explosionLocation.getY()
		val eZ: Double = explosionLocation.getZ()
		val iterator: MutableIterator<Block> = list.iterator()

		while (iterator.hasNext()) {
			processBlock(explodedBlockDataList, eX, eY, eZ, iterator)
		}

		// if no blocks were handled by the plugin at all (for example, every block's type is ignored)
		if (explodedBlockDataList.isEmpty()) return

		worldData?.addAll(world, explodedBlockDataList)
	}

	private fun processBlock(
		explodedBlockDataList: MutableList<ExplodedBlockData>, eX: Double, eY: Double, eZ: Double,
		iterator: MutableIterator<Block>,
	) {
		val block: Block = iterator.next()
		val blockData: BlockData = block.blockData
		if (ignoreMaterial(blockData.material)) {
			return
		}
		val x: Int = block.getX()
		val y: Int = block.getY()
		val z: Int = block.getZ()
		val explodedTime: Long = getExplodedTime(eX, eY, eZ, x, y, z)

		@Nullable val tileEntity: ByteArray = NMSUtils.getTileEntity(block)

		if (tileEntity != null) {
			processTileEntity(explodedBlockDataList, block, explodedTime)
		}

		val explodedBlockData = ExplodedBlockData(x, y, z, explodedTime, blockData, tileEntity)
		explodedBlockDataList.add(explodedBlockData)

		// break the block manually
		iterator.remove()
		block.setType(Material.AIR, false)
	}

	private fun ignoreMaterial(material: Material): Boolean {
		val includedMaterials: Set<Material> = settings.getIncludedMaterials()

		return material === Material.AIR
			|| settings.getIgnoredMaterials().contains(material)
			|| (includedMaterials.isNotEmpty() && !includedMaterials.contains(material))
	}

	private fun processTileEntity(
		explodedBlockDataList: MutableList<ExplodedBlockData>,
		block: Block,
		explodedTime: Long,
	) {
		val state: BlockState = block.state
		if (state is InventoryHolder) {
			val inventory: Inventory = (state as InventoryHolder).inventory
			// Double chests are weird so you have to get the state (as a holder)'s inventory's holder to cast to DoubleChest
			val inventoryHolder: InventoryHolder? = inventory.holder
			if (inventoryHolder is DoubleChest) {
				val isRight = (block.getBlockData() as Chest).getType() === Chest.Type.RIGHT
				processDoubleChest(explodedBlockDataList, isRight, inventoryHolder, explodedTime)
			}
			inventory.clear()
		}
	}

	private fun processDoubleChest(
		explodedBlockDataList: MutableList<ExplodedBlockData>, isRight: Boolean,
		doubleChest: DoubleChest, explodedTime: Long,
	) {
		val inventory = doubleChest.inventory as DoubleChestInventory
		val otherInventory: Inventory = if (isRight) inventory.rightSide else inventory.leftSide
		val otherInventoryLocation: Location = otherInventory.location!!

		val other: Block = otherInventoryLocation.block

		val otherX: Int = other.x
		val otherY: Int = other.y
		val otherZ: Int = other.z

		val otherBlockData: BlockData = other.blockData
		val otherTile: ByteArray = NMSUtils.getTileEntity(other)

		explodedBlockDataList.add(ExplodedBlockData(otherX, otherY, otherZ, explodedTime, otherBlockData, otherTile))
		otherInventory.clear()
		other.setType(Material.AIR, false)
	}
}
