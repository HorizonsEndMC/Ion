package net.horizonsend.ion.server.features.transport

import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.features.starship.event.build.StarshipBreakBlockEvent
import net.horizonsend.ion.server.features.starship.event.build.StarshipPlaceBlockEvent
import net.horizonsend.ion.server.features.transport.manager.TransportManager
import net.horizonsend.ion.server.features.transport.manager.extractors.ExtractorManager
import net.horizonsend.ion.server.features.transport.manager.extractors.ExtractorManager.Companion.isExtractorData
import net.horizonsend.ion.server.features.world.IonWorld.Companion.ion
import net.horizonsend.ion.server.features.world.chunk.IonChunk
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.getBlockDataSafe
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.data.BlockData
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPistonExtendEvent
import org.bukkit.event.block.BlockPistonRetractEvent
import org.bukkit.event.block.BlockPlaceEvent
import java.util.Timer
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.concurrent.fixedRateTimer

object NewTransport : IonServerComponent(runAfterTick = true /* Run after tick to wait on the full server startup. */) {
	private val transportManagers = ConcurrentHashMap.newKeySet<TransportManager<*>>()

	lateinit var monitorThread: Timer
	lateinit var executor: ExecutorService

	override fun onEnable() {
		executor = Executors.newFixedThreadPool(64, Tasks.namedThreadFactory("wire-transport"))

		val interval: Long = ConfigurationFiles.transportSettings().extractorConfiguration.extractorTickIntervalMS

		monitorThread = fixedRateTimer(name = "Extractor Tick", daemon = true, initialDelay = interval, period = interval) {
			transportManagers.forEach {
				try {
					it.tick()
				} catch (exception: Exception) {
					exception.printStackTrace()
				}
			}
		}

		Tasks.asyncRepeat(120L, 120L, ::saveExtractors)
	}

	override fun onDisable() {
		if (::monitorThread.isInitialized) monitorThread.cancel()
		if (::executor.isInitialized) executor.shutdown()
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

	fun addExtractor(world: World, x: Int, y: Int, z: Int) {
		getExtractorManager(world, x, z)?.registerExtractor(x, y, z)
	}

	fun removeExtractor(world: World, x: Int, y: Int, z: Int) {
		getExtractorManager(world, x, z)?.removeExtractor(x, y, z)
	}

	fun isExtractor(world: World, x: Int, y: Int, z: Int): Boolean {
		return getExtractorManager(world, x, z)?.isExtractorPresent(x, y, z) ?: false
	}

	fun ensureExtractor(world: World, x: Int, y: Int, z: Int) {
		val type = getBlockDataSafe(world, x, y, z) ?: return

		val isExtractorPresent = this@NewTransport.isExtractor(world, x, y, z)
		val isExtractor = isExtractorData(type)

		if (isExtractor && !isExtractorPresent) addExtractor(world, x, y, z)
		if (!isExtractor && isExtractorPresent) removeExtractor(world, x, y, z)
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

	@EventHandler(priority = EventPriority.MONITOR)
	fun onPlayerBlockPlace(event: BlockPlaceEvent) {
		val block = event.block
		handleBlockEvent(block.world, block.x, block.y, block.z, event.blockReplacedState.blockData, block.blockData)
	}

	@EventHandler(priority = EventPriority.MONITOR)
	fun onPlayerBlockBreak(event: BlockBreakEvent) {
		val block = event.block
		handleBlockEvent(block.world, block.x, block.y, block.z, block.blockData, Material.AIR.createBlockData())
	}

	@EventHandler(priority = EventPriority.MONITOR)
	fun onShipBlockPlace(event: StarshipPlaceBlockEvent) {
		val block = event.block
		handleBlockEvent(block.world, block.x, block.y, block.z, Material.AIR.createBlockData(), block.blockData)
	}

	@EventHandler(priority = EventPriority.MONITOR)
	fun onShipBlockBreak(event: StarshipBreakBlockEvent) {
		val block = event.block
		handleBlockEvent(block.world, block.x, block.y, block.z, event.block.blockData, Material.AIR.createBlockData())
	}

	@EventHandler(priority = EventPriority.MONITOR)
	fun handlePistonExtend(event: BlockPistonExtendEvent) {
		val piston = event.block
		Tasks.sync {
			for (block in event.blocks) {
				ensureExtractor(piston.world, piston.x, piston.y, piston.z)
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	fun handlePistonRetract(event: BlockPistonRetractEvent) {
		val piston = event.block
		Tasks.sync {
			for (block in event.blocks) {
				ensureExtractor(piston.world, piston.x, piston.y, piston.z)
			}
		}
	}

	fun saveExtractors() {
		for (world in Bukkit.getWorlds().map { it.ion }) {
			val chunks = world.getAllChunks().values.forEach { chunk ->
				chunk.transportNetwork.extractorManager.takeIf { it.needsSave }?.save()
			}
		}
	}
}
