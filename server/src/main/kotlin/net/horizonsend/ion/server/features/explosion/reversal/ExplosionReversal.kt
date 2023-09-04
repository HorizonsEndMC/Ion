package net.horizonsend.ion.server.features.explosion.reversal

import com.google.common.io.ByteStreams
import it.unimi.dsi.fastutil.objects.ObjectLists
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.explosion.reversal.Regeneration.pulse
import net.horizonsend.ion.server.features.starship.event.explosion.StarshipCauseExplosionEvent
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtAccounter
import net.minecraft.nbt.NbtIo
import net.minecraft.world.level.block.entity.BlockEntity
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.block.BlockState
import org.bukkit.block.DoubleChest
import org.bukkit.block.data.BlockData
import org.bukkit.block.data.type.Chest
import org.bukkit.craftbukkit.v1_19_R3.CraftWorld
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.BlockExplodeEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.world.WorldSaveEvent
import org.bukkit.inventory.DoubleChestInventory
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import java.io.IOException
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
		} catch (exception: IOException) {
			exception.printStackTrace()
		}
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

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	fun onWorldSave(event: WorldSaveEvent) {
		worldData!!.save(event.world)
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	fun onBlockExplode(event: BlockExplodeEvent) {
		val world = event.block.world
		val location: Location = event.block.location
		val blockList: MutableList<Block> = event.blockList()

		processExplosion(world, location, blockList)
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	fun onEntityExplode(event: EntityExplodeEvent) {
		val world = event.entity.world
		val location = event.location
		val blockList = event.blockList()

		processExplosion(world, location, blockList)
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	fun onCustomExplosion(event: StarshipCauseExplosionEvent) {
		val world = event.explosion.world
		val location: Location = event.explosion.location()
		val blockList: MutableSet<Block> = event.blocks

		processExplosion(world, location, blockList)
	}

	private fun getExplodedTime(
		explosionX: Double, explosionY: Double, explosionZ: Double,
		blockX: Int, blockY: Int, blockZ: Int,
	): Long {
		val now = System.currentTimeMillis()

		val distance = abs(explosionX - blockX) + abs(explosionY - blockY) + abs(explosionZ - blockZ)
		val distanceDelayMs = settings.distanceDelay * 1000
		val cap = settings.distanceDelayCap.toLong()

		val offset: Long = (cap.coerceAtMost(cap - distance.roundToLong()) * distanceDelayMs).roundToLong()

		return now + offset
	}

	private fun processExplosion(world: World, explosionLocation: Location, list: MutableCollection<Block>) {
		if (settings.ignoredWorlds.contains(world.name)) return
		if (list.isEmpty()) return

		val explodedBlockDataList: MutableList<ExplodedBlockData> = ObjectLists.emptyList()
		val eX: Double = explosionLocation.x
		val eY: Double = explosionLocation.y
		val eZ: Double = explosionLocation.z
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
		println(3.1)
		val block: Block = iterator.next()
		val blockData: BlockData = block.blockData

		val x: Int = block.x
		val y: Int = block.y
		val z: Int = block.z

		val explodedTime: Long = getExplodedTime(eX, eY, eZ, x, y, z)

		val tileEntity: ByteArray? = getTileEntity(block)

		if (tileEntity != null) {
			processTileEntity(explodedBlockDataList, block, explodedTime)
		}

		val explodedBlockData = ExplodedBlockData(x, y, z, explodedTime, blockData, tileEntity)
		explodedBlockDataList.add(explodedBlockData)

		// break the block manually
		iterator.remove()
		block.setType(Material.AIR, false)
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
				val isRight = (block.blockData as Chest).type === Chest.Type.RIGHT

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
		val otherTile: ByteArray? = getTileEntity(other)

		explodedBlockDataList.add(ExplodedBlockData(otherX, otherY, otherZ, explodedTime, otherBlockData, otherTile))
		otherInventory.clear()
		other.setType(Material.AIR, false)
	}

	fun getTileEntity(block: Block): ByteArray? {
		val worldServer = (block.world as CraftWorld).handle
		val blockPosition = BlockPos(block.x, block.y, block.z)
		val tileEntity = worldServer.getBlockEntity(blockPosition) ?: return null

		val nbt: CompoundTag = tileEntity.saveWithFullMetadata()

		return serialize(nbt)
	}

	fun setTileEntity(block: Block, bytes: ByteArray?) {
		val worldServer = (block.world as CraftWorld).handle
		val blockPosition = BlockPos(block.x, block.y, block.z)

		val nbt = try {
			deserialize(bytes ?: return)
		} catch (e: Exception) {
			IonServer.logger.warning("Error placing tile entity during explosion regeneration: $e")
			return
		}

		val blockData: net.minecraft.world.level.block.state.BlockState = worldServer.getBlockState(blockPosition)
		val tileEntity = BlockEntity.loadStatic(blockPosition, blockData, nbt) ?: return IonServer.logger.warning("Error loading tile entity during explosion regeneration")

		worldServer.removeBlockEntity(blockPosition)
		worldServer.setBlockEntity(tileEntity)
	}

	private fun serialize(nbt: CompoundTag): ByteArray? {
		val output = ByteStreams.newDataOutput()
		NbtIo.write(nbt, output)
		return output.toByteArray()
	}

	private fun deserialize(bytes: ByteArray): CompoundTag {
		val input = ByteStreams.newDataInput(bytes)
		val readLimiter = NbtAccounter(bytes.size * 10L)
		return NbtIo.read(input, readLimiter)
	}
}
