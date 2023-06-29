package net.starlegacy.feature.starship

import com.google.common.collect.Multimap
import com.google.common.collect.Multimaps
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import net.minecraft.world.level.block.state.BlockState
import net.horizonsend.ion.server.IonComponent
import net.starlegacy.listen
import net.starlegacy.util.PerWorld
import net.starlegacy.util.Tasks
import net.starlegacy.util.blockKeyX
import net.starlegacy.util.blockKeyY
import net.starlegacy.util.blockKeyZ
import net.starlegacy.util.blockplacement.BlockPlacement
import net.starlegacy.util.getNMSBlockDataSafe
import net.starlegacy.util.nms
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.data.BlockData
import org.bukkit.event.world.WorldLoadEvent
import org.bukkit.event.world.WorldSaveEvent
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set

object Hangars : IonComponent() {
	private val hangarData: PerWorld<Multimap<BlockData, Long>> = PerWorld(::load)

	override fun onEnable() {
		for (world in Bukkit.getWorlds()) {
			hangarData[world] // this will load it, not just get it, due to how the cache works
		}
		listen<WorldLoadEvent> { event ->
			hangarData[event.world]
		}
		listen<WorldSaveEvent> { event ->
			Tasks.async {
				save(event.world)
			}
		}
		regenerateHangars()
		Tasks.asyncRepeat(20L, 20L) {
			regenerateHangars()
		}
	}

	private fun regenerateHangars() {
		for ((world, map) in hangarData.cache.asMap()) {
			if (map.isEmpty) {
				continue
			}
			val restorations = LinkedList<Map.Entry<BlockData, Long>>()
			for (entry in map.entries()) {
				val (data, blockKey) = entry
				val x = blockKeyX(blockKey)
				val y = blockKeyY(blockKey)
				val z = blockKeyZ(blockKey)
				val currentData: BlockState = getNMSBlockDataSafe(world, x, y, z) ?: continue
				if (!currentData.bukkitMaterial.isAir) {
					continue
				}
				restorations += entry
			}
			if (restorations.isEmpty()) {
				continue
			}
			Tasks.sync {
				val queue = Long2ObjectOpenHashMap<BlockState>()
				for ((data, blockKey) in restorations) {
					if (!world.isChunkLoaded(blockKeyX(blockKey) shr 4, blockKeyZ(blockKey) shr 4)) {
						continue
					}
					val block = world.getBlockAtKey(blockKey)
					if (!block.type.isAir) {
						continue
					}
					queue[blockKey] = data.nms
					map.remove(data, blockKey)
				}
				BlockPlacement.placeImmediate(world, queue)
			}
		}
	}

	fun dissipateBlock(world: World, blockKey: Long) {
		val block = world.getBlockAtKey(blockKey)
		val data = block.blockData
		require(!data.material.isAir)
		block.setType(Material.AIR, false)
		hangarData[world][data].add(blockKey)
	}

	override fun onDisable() {
		saveAll()
	}

	private fun getFile(world: World) = File(world.worldFolder, "data/hangar_gates.dat")

	private fun load(world: World): Multimap<BlockData, Long> {
		val map = Multimaps.newSetMultimap<BlockData, Long>(ConcurrentHashMap()) { ConcurrentHashMap.newKeySet() }
		val file = getFile(world)
		if (!file.exists() || file.length() == 0L) {
			return map
		}
		try {
			log.info("Loading ${file.absolutePath}")
			DataInputStream(BufferedInputStream(FileInputStream(file))).use { dis ->
				val blockTypeCount = dis.readInt()
				repeat(blockTypeCount) {
					val blockData = Bukkit.createBlockData(dis.readUTF())
					val valueCount = dis.readInt()
					if (map.containsKey(blockData)) {
						dis.skip(valueCount * 8L)
						log.warn("Duplicate entry of type $blockData")
						return@repeat
					}
					repeat(valueCount) {
						map[blockData].add(dis.readLong())
					}
				}
			}
		} catch (e: Exception) {
			println("Failed to load hangar data for world ${world.name}")
			e.printStackTrace()
		}
		return map
	}

	private fun saveAll() {
		for (world: World in Bukkit.getWorlds()) {
			save(world)
		}
	}

	@Synchronized
	private fun save(world: World) {
		val data: Multimap<BlockData, Long> = hangarData[world]
		val file = getFile(world)
		val tmpFile = File(file.parent, file.name + "_tmp")
		if (tmpFile.exists()) {
			tmpFile.delete()
		}
		DataOutputStream(BufferedOutputStream(FileOutputStream(tmpFile))).use { dos ->
			val keys = data.keySet().toList()
			dos.writeInt(keys.size)
			for (blockData: BlockData in keys) {
				val values: Collection<Long> = data[blockData].toList()
				dos.writeUTF(blockData.asString)

				dos.writeInt(values.size)
				for (value: Long in values) {
					dos.writeLong(value)
				}
			}
		}
		tmpFile.renameTo(file)
	}
}
