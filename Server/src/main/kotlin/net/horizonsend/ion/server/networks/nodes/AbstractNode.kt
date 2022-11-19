package net.horizonsend.ion.server.networks.nodes

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonIdentityInfo
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonManagedReference
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.ObjectIdGenerators
import net.horizonsend.ion.server.IonChunk
import net.horizonsend.ion.server.networks.Validatable
import net.horizonsend.ion.server.networks.connections.AbstractConnection
import net.horizonsend.ion.server.networks.removalQueue
import net.horizonsend.ion.server.networks.tickId
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.Block
import kotlin.reflect.full.companionObjectInstance

@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator::class, property = "key")
@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, include = JsonTypeInfo.As.PROPERTY, property = "type")
abstract class AbstractNode : Validatable() {
	@JsonIgnore val companion = this::class.companionObjectInstance as AbstractNodeCompanion<*>

	@JsonBackReference("IonChunk") lateinit var ionChunk: IonChunk

	private lateinit var _position: BlockPos
	private var _key: Long = 0

	@get:JsonIgnore
	var position: BlockPos
		get() = _position
		set(value) {
			_position = value
			_key = _position.asLong()
		}

	var key: Long
		get() {
			if (!this::_position.isInitialized) throw UninitializedPropertyAccessException()
			return _key
		}
		set(value) {
			_key = value
			_position = BlockPos.of(value)
		}

	inline val x @JsonIgnore get() = position.x
	inline val y @JsonIgnore get() = position.y
	inline val z @JsonIgnore get() = position.z

	final override fun checkIsLoaded(): Boolean = ionChunk.isLoaded

	override fun checkIsValid(): Boolean {
		val block = ionChunk.ionWorld.serverLevel.getBlockIfLoaded(position)

		return companion.nodeBlock == block
	}

	@Deprecated("Function should only be called by the node remover.")
	final override fun remove() {
		for (connection in connections) removalQueue.add(connection)
		ionChunk.removeNode(this)
	}

	@JsonManagedReference("Node") val connections: MutableList<AbstractConnection> = mutableListOf()

	protected open fun canStepFrom(lastNode: AbstractNode, lastConnection: AbstractConnection) = true
	protected open fun canStepTo(nextNode: AbstractNode, nextConnection: AbstractConnection) = true

	private var lastTickId: Int = Int.MIN_VALUE

	inline fun <reified T : AbstractNode> typedPathfind(crossinline callback: (T) -> Boolean) = pathfind {
		if (it is T) callback(it) else true
	}

	fun pathfind(callback: (AbstractNode) -> Boolean) {
		step(tickId++, null, null, callback)
	}

	private fun step(
		tickId: Int,
		lastNode: AbstractNode?,
		lastConnection: AbstractConnection?,
		callback: (AbstractNode) -> Boolean
	): Boolean {
		// Check node has not been visited this tick
		if (lastTickId == tickId) return false
		lastTickId = tickId

		// Check node will allow stepping
		if (lastNode != null && lastConnection != null && !canStepFrom(lastNode, lastConnection)) return false

		// Check node is still valid
		if (!isValid) return false

		if (callback(this)) return true

		for (nextConnection in connections) {
			if (!nextConnection.isValid) continue // Check connection is valid
			if (nextConnection == lastConnection) continue // Check we are not going back where we came

			val nextNode = nextConnection.other(this)

			if (!canStepTo(nextNode, nextConnection)) continue // Check this node will allow transfer

			if (nextNode.step(tickId, this, nextConnection, callback)) return true
		}

		return false
	}

	abstract class AbstractNodeCompanion<T: AbstractNode>(
		val nodeBlock: Block
	) {
		protected abstract fun construct(): T

		fun build(ionChunk: IonChunk, blockPos: BlockPos): T {
			val node = construct()
			node.ionChunk = ionChunk
			node.position = blockPos
			return node
		}
	}
}