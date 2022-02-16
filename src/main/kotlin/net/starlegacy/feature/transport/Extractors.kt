package net.starlegacy.feature.transport

import com.google.gson.Gson
import java.io.File
import java.nio.file.Files
import java.util.Timer
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.set
import kotlin.concurrent.fixedRateTimer
import net.starlegacy.SLComponent
import net.starlegacy.feature.transport.pipe.Pipes
import net.starlegacy.feature.transport.pipe.filter.FilterItemData
import net.starlegacy.feature.transport.pipe.filter.Filters
import net.starlegacy.util.ADJACENT_BLOCK_FACES
import net.starlegacy.util.Tasks
import net.starlegacy.util.Vec3i
import net.starlegacy.util.coordinates
import net.starlegacy.util.getBlockDataSafe
import net.starlegacy.util.getBlockTypeSafe
import net.starlegacy.util.getStateIfLoaded
import net.starlegacy.util.gzip
import net.starlegacy.util.randomEntry
import net.starlegacy.util.randomFloat
import net.starlegacy.util.timing
import net.starlegacy.util.ungzip
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.BlockState
import org.bukkit.block.data.BlockData
import org.bukkit.block.data.type.DaylightDetector
import org.bukkit.craftbukkit.v1_18_R1.inventory.CraftInventory
import org.bukkit.event.EventPriority
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.world.WorldLoadEvent
import org.bukkit.event.world.WorldUnloadEvent
import org.bukkit.inventory.InventoryHolder

object Extractors : SLComponent() {
	private const val extractorTicksPerSecond = 1.0

	val EXTRACTOR_BLOCK = Material.CRAFTING_TABLE

	private lateinit var timer: Timer

	private val worldDataMap = ConcurrentHashMap<World, MutableSet<Vec3i>>()

	val BUSY_PIPE_EXTRACTORS: MutableSet<Vec3i> = ConcurrentHashMap.newKeySet()

	override fun onEnable() {
		plugin.server.worlds.forEach(Extractors::loadExtractors)

		Tasks.asyncRepeat(20 * 2, 20 * 2) {
			worldDataMap.keys.forEach { saveExtractors(it) }
		}

		plugin.listen<BlockPlaceEvent>(priority = EventPriority.MONITOR, ignoreCancelled = true) { event ->
			val block: Block = event.block

			if (block.type == EXTRACTOR_BLOCK) {
				add(block.world, block.coordinates)
			}
		}

		plugin.listen<BlockBreakEvent>(priority = EventPriority.MONITOR, ignoreCancelled = true) { event ->
			val block: Block = event.block

			remove(block.world, block.coordinates)
		}

		plugin.listen<WorldLoadEvent> { event -> loadExtractors(event.world) }

		plugin.listen<WorldUnloadEvent> { event -> unloadExtractors(event.world) }

		val interval: Long = (1000 / extractorTicksPerSecond).toLong()

		timer = fixedRateTimer(name = "Extractor Tick", daemon = true, initialDelay = interval, period = interval) {
			try {
				worldDataMap.keys.forEach(Extractors::tickExtractors)
			} catch (exception: Exception) {
				exception.printStackTrace()
			}
		}
	}

	override fun onDisable() {
		plugin.server.worlds.forEach(Extractors::unloadExtractors)
		timer.cancel()
	}

	//region Loading and saving
	private fun getDataFile(world: World): File = File(world.worldFolder, "data/starlegacy/extractors.json.gz")
		.apply { parentFile.mkdirs() }

	data class WorldDataStorage(val values: List<Vec3i>)

	private fun loadExtractors(world: World) {
		val file: File = getDataFile(world)

		val worldData: WorldDataStorage

		val exists: Boolean = file.exists()

		worldData = if (exists) {
			try {
				Gson().fromJson(ungzip(Files.readAllBytes(file.toPath())), WorldDataStorage::class.java)
			} catch (e: Exception) {
				e.printStackTrace()
				file.delete()
				return loadExtractors(world)
			}
		} else {
			WorldDataStorage(listOf())
		}

		worldDataMap[world] = ConcurrentHashMap.newKeySet<Vec3i>().apply { addAll(worldData.values) }

		if (exists) {
			log.debug("Loaded extractors for world ${world.name}")
		} else {
			saveExtractors(world)
			log.info("Created extractors file for word ${world.name}")
		}
	}

	private fun saveExtractors(world: World) {
		val file = getDataFile(world)

		val values: Set<Vec3i> = worldDataMap[world] ?: error("No data for world ${world.name}, but tried to save!")

		val worldData = WorldDataStorage(values.toList())

		val bytes = gzip(Gson().toJson(worldData))
		val oldFile = File(file.parent, file.name + ".old")
		file.renameTo(oldFile)
		Files.write(file.toPath(), bytes)
		oldFile.delete()

		log.debug("Saved extractor data for world ${world.name}")
	}

	private fun unloadExtractors(world: World) {
		saveExtractors(world)
		worldDataMap.remove(world)
	}
	//endregion

	private fun tickExtractors(world: World) {
		val extractorLocations: Set<Vec3i> = worldDataMap[world] ?: return

		// used to ensure solar panels are processed only at day
		val isDay: Boolean = world.environment != World.Environment.NORMAL || world.time < 12300 || world.time > 23850

		extractorLoop@ for (extractorLocation: Vec3i in extractorLocations) {
			val (x: Int, y: Int, z: Int) = extractorLocation

			// note on continuing for null values: if it is null that means
			// the chunk is unloaded or something, so we ignore the extractor

			// eventually, make this auto remove when it's been invalid for a long time. for now, just ignore it
			// this also ensures the chunk is loaded
			val extractorMaterial: Material? = getBlockTypeSafe(world, x, y, z)

			if (extractorMaterial != EXTRACTOR_BLOCK) {
				continue
			}

			val computers: MutableList<Vec3i> = mutableListOf() // list of note block power machine computers
			val wires: MutableList<BlockFace> = mutableListOf() // list of end rod wires
			val inventories: MutableList<Vec3i> = mutableListOf() // list of inventories to extract from to pipes
			val pipes: MutableList<BlockFace> = mutableListOf() // list of pipes to extract to

			var solarSensor: BlockData? = null

			// check adjacent blocks & fill the lists with them
			adjacentCheck@
			for (face: BlockFace in ADJACENT_BLOCK_FACES) {
				val adjacentX: Int = x + face.modX
				val adjacentY: Int = y + face.modY
				val adjacentZ: Int = z + face.modZ

				val adjacentType: Material = getBlockTypeSafe(world, adjacentX, adjacentY, adjacentZ)
					?: continue

				when {
					isExtractableInventory(adjacentType) -> {
						inventories.add(Vec3i(adjacentX, adjacentY, adjacentZ))
					}

					Pipes.isAnyPipe(adjacentType) -> {
						pipes.add(face)
					}

					adjacentType == Wires.INPUT_COMPUTER_BLOCK -> {
						computers.add(Vec3i(adjacentX, adjacentY, adjacentZ))
					}

					Wires.isAnyWire(adjacentType) -> {
						wires.add(face)
					}

					isDay && adjacentType == Material.DIAMOND_BLOCK && face == BlockFace.UP -> {
						val sensor: BlockData = getBlockDataSafe(world, adjacentX, adjacentY + 1, adjacentZ)
							?: continue@extractorLoop

						if (sensor.material == Material.DAYLIGHT_DETECTOR) {
							solarSensor = sensor
						}
					}
				}
			}

			// if there is an inventory and a pipe and it was not already busy, handle it
			if (inventories.isNotEmpty() && pipes.isNotEmpty() && BUSY_PIPE_EXTRACTORS.add(extractorLocation)) {
				handlePipe(world, extractorLocation, inventories, pipes)
			}

			if (computers.isNotEmpty() && wires.isNotEmpty()) {
				handleWire(world, x, y, z, computers, wires)
			}

			if (solarSensor != null) {
				handleSolarPanel(world, x, y, z, wires, solarSensor)
			}
		}
	}

	private fun isExtractableInventory(adjacentType: Material): Boolean {
		return Pipes.isPipedInventory(adjacentType) ||
			adjacentType == Material.HOPPER
	}

	private val pipeTiming = timing("Extractor Pipe Launching")

	// TODO: Make this mostly async
	private fun handlePipe(
		world: World, extractorLocation: Vec3i,
		inventoryLocations: List<Vec3i>, pipeLocations: List<BlockFace>
	): Unit = Tasks.syncTimed(pipeTiming) {
		var cancelled = true

		try {
			val (x, y, z) = extractorLocation

			val filteredItemMap = mutableMapOf<Vec3i, Set<FilterItemData>>()

			// filter it to only locations that are still loaded,
			// still inventory blocks, and have items in their inventory
			val inventories: List<Vec3i> = inventoryLocations.filter { invPos ->
				val (invX: Int, invY: Int, invZ: Int) = invPos
				val state: BlockState = getStateIfLoaded(world, invX, invY, invZ)
					?: return@filter false // ignore it if the chunk isn't loaded

				if (!isExtractableInventory(state.type)) {
					return@filter false
				}

				val inventory = (state as InventoryHolder).inventory
				val filtered = Filters.getItemData(inventory)
				filteredItemMap[invPos] = filtered
				// check that at least one item is not null
				return@filter filtered.any()
			}

			if (inventories.isEmpty()) {
				return@syncTimed
			}

			// filter it to only locations that are still loaded and pipe blocks
			val pipes = pipeLocations.filter { face ->
				val type: Material = getBlockTypeSafe(world, x + face.modX, y + face.modY, z + face.modZ)
					?: return@filter false

				return@filter Pipes.isAnyPipe(type)
			}

			if (pipes.isEmpty()) {
				return@syncTimed
			}

			val pipe: BlockFace = pipes.randomEntry()
			val inventory: Vec3i = inventories.randomEntry()

			val filteredItems = filteredItemMap.getValue(inventory)
			Pipes.startPipeChain(world, x, y, z, pipe, inventory, extractorLocation, filteredItems)
			cancelled = false
		} finally {
			if (cancelled) {
				BUSY_PIPE_EXTRACTORS.remove(extractorLocation)
			}
		}
	}

	private fun isInventoryNotEmpty(state: BlockState): Boolean {
		val inventory = (state as InventoryHolder).inventory
		val cb = (inventory as CraftInventory)
		val nms = cb.inventory
		return nms.contents.any { it != null && !it.isEmpty }
	}

	private fun handleWire(world: World, x: Int, y: Int, z: Int, computers: List<Vec3i>, wires: List<BlockFace>) {
		val wire: BlockFace = wires.randomEntry()
		val computer: Vec3i = computers.randomEntry()

		Wires.startWireChain(world, x, y, z, wire, computer)
	}

	private const val SOLAR_PANEL_CHANCE = 0.05

	private fun handleSolarPanel(world: World, x: Int, y: Int, z: Int, wires: List<BlockFace>, sensor: BlockData) {
		if (wires.isEmpty()) {
			return
		}

		if (randomFloat() > (SOLAR_PANEL_CHANCE / extractorTicksPerSecond)) {
			return
		}

		sensor as DaylightDetector
		val inverted: Boolean = sensor.isInverted
		val power: Int = sensor.power

		// make it so it works in the day only, whether it be a night sensor or a day sensor,
		// and also only if it has sky light. tldr; based on the power
		if ((power < 4 && !inverted) || (power > 2 && inverted)) {
			return
		}

		Wires.startWireChain(world, x, y, z, wires.randomEntry(), null)
	}

	private fun getExtractorSet(world: World): MutableSet<Vec3i> {
		return worldDataMap[world] ?: error("No extractors loaded for ${world.name}")
	}

	fun add(world: World, coordinates: Vec3i): Boolean {
		return getExtractorSet(world).add(coordinates)
	}

	fun remove(world: World, coordinates: Vec3i): Boolean {
		return getExtractorSet(world).remove(coordinates)
	}

	fun contains(world: World, coordinates: Vec3i): Boolean {
		return getExtractorSet(world).contains(coordinates)
	}
}
