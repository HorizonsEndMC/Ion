package net.horizonsend.ion.server

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonManagedReference
import com.fasterxml.jackson.annotation.JsonProperty
import net.horizonsend.ion.server.networks.nodes.AbstractNode
import net.horizonsend.ion.server.networks.nodes.ComputerNode
import net.horizonsend.ion.server.networks.nodes.ExtractorNode
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.chunk.LevelChunk
import org.bukkit.Bukkit

class IonChunk(
	@JsonProperty("chunkKey")
	val chunkKey: Long,
) {
	constructor(ionWorld: IonWorld, chunkKey: Long) : this(chunkKey) { this.ionWorld = ionWorld }

	@JsonBackReference("IonWorld")
	lateinit var ionWorld: IonWorld private set

	@JsonIgnore val chunkX: Int
	@JsonIgnore val chunkZ: Int

	init {
		val chunkPos = ChunkPos(chunkKey)
		chunkX = chunkPos.x
		chunkZ = chunkPos.z
	}

	private var cachedLevelChunk: LevelChunk? = null
	private var lastCacheUpdate = Int.MIN_VALUE

	val levelChunk: LevelChunk? @JsonIgnore get() {
		if (lastCacheUpdate < Bukkit.getCurrentTick()) {
			cachedLevelChunk = ionWorld.serverLevel.getChunk(chunkX, chunkZ)
			lastCacheUpdate = Bukkit.getCurrentTick()
		}

		return cachedLevelChunk
	}

	@JsonManagedReference("IonChunk") val tickableNodes = mutableListOf<ExtractorNode>()
	@JsonManagedReference("IonChunk") val otherNodes = mutableListOf<AbstractNode>()
	@JsonManagedReference("IonChunk") val savableNodes = mutableListOf<ComputerNode>()

	val isEmpty @JsonIgnore get() = tickableNodes.isEmpty() && otherNodes.isEmpty() && savableNodes.isEmpty()

	fun addNode(node: AbstractNode) {
		when (node) {
			is ExtractorNode -> tickableNodes.add(node)
			is ComputerNode -> savableNodes.add(node)
			else -> otherNodes.add(node)
		}
	}

	fun removeNode(node: AbstractNode) {
		when (node) {
			is ExtractorNode -> tickableNodes.remove(node)
			is ComputerNode -> savableNodes.remove(node)
			else -> otherNodes.remove(node)
		}
	}

	fun iterateNodes(callback: (AbstractNode) -> Unit) {
		for (node in tickableNodes) callback(node)
		for (node in otherNodes) callback(node)
		for (node in savableNodes) callback(node)
	}

	fun tickNodes() {
		if (tickableNodes.isEmpty()) return
		for (node in tickableNodes) node.tick()
	}

	fun saveNodes() {
		for (node in savableNodes) node.save()
	}

	fun clear() {
		tickableNodes.clear()
		otherNodes.clear()
		savableNodes.clear()
	}
}