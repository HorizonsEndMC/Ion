package net.horizonsend.ion.server.features.transport

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.features.custom.blocks.CustomBlocks
import net.horizonsend.ion.server.features.custom.blocks.filter.CustomFilterBlock
import net.horizonsend.ion.server.features.starship.event.build.StarshipBreakBlockEvent
import net.horizonsend.ion.server.features.starship.event.build.StarshipPlaceBlockEvent
import net.horizonsend.ion.server.features.transport.filters.manager.FilterCache
import net.horizonsend.ion.server.features.transport.manager.TransportManager
import net.horizonsend.ion.server.features.transport.manager.extractors.ExtractorManager
import net.horizonsend.ion.server.features.transport.manager.extractors.ExtractorManager.Companion.isExtractorData
import net.horizonsend.ion.server.features.world.chunk.IonChunk
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import net.horizonsend.ion.server.miscellaneous.utils.getBlockDataSafe
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.data.BlockData
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockFromToEvent
import org.bukkit.event.block.BlockPistonExtendEvent
import org.bukkit.event.block.BlockPistonRetractEvent
import org.bukkit.event.block.BlockPlaceEvent
import java.util.Timer
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.RejectedExecutionException
import kotlin.concurrent.fixedRateTimer

object NewTransport : IonServerComponent(runAfterTick = true /* Run after tick to wait on the full server startup. */) {
	var enabled: Boolean = false; private set
	private val transportManagers = ConcurrentHashMap.newKeySet<TransportManager<*>>()

	private lateinit var timer: Timer
	private lateinit var executor: ExecutorService
	private lateinit var monitor: Thread

	private var taskTimeoutMillis = ConfigurationFiles.transportSettings().taskTimeout.toDuration().toMillis()
	var executingPool: ConcurrentHashMap.KeySetView<TransportTask, Boolean> = ConcurrentHashMap.newKeySet(ConfigurationFiles.transportSettings().transportThreadCount); private set

	fun reload() {
		val configuration = ConfigurationFiles.transportSettings()

		taskTimeoutMillis = configuration.taskTimeout.toDuration().toMillis()
		executingPool = ConcurrentHashMap.newKeySet(configuration.transportThreadCount)

		executor = Executors.newFixedThreadPool(configuration.transportThreadCount, Tasks.namedThreadFactory("wire-transport"))

		val interval: Long = configuration.extractorConfiguration.extractorTickIntervalMS
		timer = fixedRateTimer(name = "Extractor Tick", daemon = true, initialDelay = interval, period = interval) { tickExtractors() }

		monitor = TransportMonitorThread()
		monitor.start()
	}


	override fun onEnable() {
		enabled = true
		reload()

		Tasks.asyncRepeat(120L, 120L, ::saveExtractors)
	}

	override fun onDisable() {
		enabled = false
		if (::timer.isInitialized) timer.cancel()
		if (::executor.isInitialized) executor.shutdown()

		saveExtractors()
	}

	fun runTask(task: () -> Unit) {
		if (!IonServer.isEnabled) return
		val wrapped = TransportTask(task, taskTimeoutMillis, log)

		try {
			executor.execute(wrapped)
		} catch (_: RejectedExecutionException) {}
	}

	private fun monitorTasks() {

	}

	private fun tickExtractors() {
		if (!enabled) return
		transportManagers.forEach {
			try {
				it.tick()
			} catch (exception: Exception) {
				exception.printStackTrace()
			}
		}
	}

	fun registerTransportManager(manager: TransportManager<*>) {
		transportManagers.add(manager)
	}

	fun removeTransportManager(manager: TransportManager<*>) {
		transportManagers.remove(manager)
	}

	fun invalidateCache(world: World, x: Int, y: Int, z: Int) {
		val chunk = IonChunk.getFromWorldCoordinates(world, x, z) ?: return
		chunk.transportNetwork.invalidateCache(x, y, z)
	}

	private fun getExtractorManager(world: World, x: Int, z: Int): ExtractorManager? {
		return IonChunk.getFromWorldCoordinates(world, x, z)?.transportNetwork?.extractorManager
	}

	private fun getFilterCache(world: World, x: Int, z: Int): FilterCache? {
		return IonChunk.getFromWorldCoordinates(world, x, z)?.transportNetwork?.filterCache
	}

	fun addExtractor(world: World, x: Int, y: Int, z: Int) {
		getExtractorManager(world, x, z)?.registerExtractor(x, y, z)
	}

	fun removeExtractor(world: World, x: Int, y: Int, z: Int) {
		getExtractorManager(world, x, z)?.removeExtractor(x, y, z)
	}

	fun isExtractor(world: World, x: Int, y: Int, z: Int): Boolean {
		return getExtractorManager(world, x, z)?.isExtractorPresent(x, y, z) ?: false
	}

	private fun ensureExtractor(world: World, x: Int, y: Int, z: Int) {
		val type = getBlockDataSafe(world, x, y, z) ?: return

		val isExtractorPresent = this@NewTransport.isExtractor(world, x, y, z)
		val isExtractor = isExtractorData(type)

		if (isExtractor && !isExtractorPresent) addExtractor(world, x, y, z)
		if (!isExtractor && isExtractorPresent) removeExtractor(world, x, y, z)
	}

	private fun removeFilter(world: World, x: Int, y: Int, z: Int) {
		getFilterCache(world, x, z)?.removeFilter(toBlockKey(x, y, z))
	}

	private fun isFilter(world: World, x: Int, y: Int, z: Int): Boolean {
		return getFilterCache(world, x, z)?.isFilterPresent(toBlockKey(x, y, z)) ?: false
	}

	private fun ensureFilter(world: World, x: Int, y: Int, z: Int) = Tasks.sync {
		val data = getBlockDataSafe(world, x, y, z) ?: return@sync
		val customBlock = CustomBlocks.getByBlockData(data)

		if (customBlock is CustomFilterBlock<*, *>) {
			if (!isFilter(world, x, y, z)) {
				getFilterCache(world, x, z)?.registerFilter(toBlockKey(x, y, z), customBlock)
			}

			return@sync
		}

		removeFilter(world, x, y, z)
	}

	fun handleBlockEvent(world: World, x: Int, y: Int, z: Int, previousData: BlockData, newData: BlockData) = Tasks.async {
		invalidateCache(world, x, y, z)

		if (isExtractorData(previousData) && !isExtractorData(newData)) {
			removeExtractor(world, x, y, z)
			return@async
		}

		if (isExtractorData(newData)) {
			addExtractor(world, x, y, z)
			return@async
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	fun onPlayerBlockPlace(event: BlockPlaceEvent) {
		val block = event.block
		handleBlockEvent(block.world, block.x, block.y, block.z, event.blockReplacedState.blockData, block.blockData)
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	fun onPlayerBlockBreak(event: BlockBreakEvent) {
		val block = event.block
		handleBlockEvent(block.world, block.x, block.y, block.z, block.blockData, Material.AIR.createBlockData())
		ensureFilter(block.world, block.x, block.y, block.z)
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	fun onShipBlockPlace(event: StarshipPlaceBlockEvent) {
		val block = event.block
		handleBlockEvent(block.world, block.x, block.y, block.z, Material.AIR.createBlockData(), block.blockData)
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	fun onShipBlockBreak(event: StarshipBreakBlockEvent) {
		val block = event.block
		handleBlockEvent(block.world, block.x, block.y, block.z, event.block.blockData, Material.AIR.createBlockData())
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	fun handlePistonExtend(event: BlockPistonExtendEvent) {
		Tasks.asyncDelay(3L) {
			val piston = event.block
			invalidateCache(piston.world, piston.x, piston.y, piston.z)

			for (block in event.blocks) {
				ensureExtractor(block.world, block.x, block.y, block.z)
				ensureFilter(block.world, block.x, block.y, block.z)
				invalidateCache(block.world, block.x, block.y, block.z)

				val relative = block.getRelative(event.direction)
				ensureExtractor(block.world, relative.x, relative.y, relative.z)
				ensureFilter(block.world, block.x, block.y, block.z)
				invalidateCache(block.world, relative.x, relative.y, relative.z)
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	fun handlePistonRetract(event: BlockPistonRetractEvent) {
		Tasks.asyncDelay(3L) {
			val piston = event.block
			invalidateCache(piston.world, piston.x, piston.y, piston.z)

			for (block in event.blocks) {
				ensureExtractor(block.world, block.x, block.y, block.z)
				ensureFilter(block.world, block.x, block.y, block.z)
				invalidateCache(block.world, block.x, block.y, block.z)

				val relative = block.getRelative(event.direction)
				ensureExtractor(block.world, relative.x, relative.y, relative.z)
				ensureFilter(block.world, block.x, block.y, block.z)
				invalidateCache(block.world, relative.x, relative.y, relative.z)
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	fun handleWaterFlow(event: BlockFromToEvent) {
		invalidateCache(event.block.world, event.block.x, event.block.y, event.block.z)
	}

	fun saveExtractors() {
		transportManagers.forEach {
			it.extractorManager.save()
		}
	}
}
