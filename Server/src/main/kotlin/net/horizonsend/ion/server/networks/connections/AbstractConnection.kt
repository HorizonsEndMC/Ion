package net.horizonsend.ion.server.networks.connections

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonIdentityInfo
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.ObjectIdGenerators
import net.horizonsend.ion.server.networks.Validatable
import net.horizonsend.ion.server.networks.nodes.AbstractNode
import java.nio.ByteBuffer
import java.util.Base64
import kotlin.math.abs

@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator::class, property = "key")
@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, include = JsonTypeInfo.As.PROPERTY, property = "type")
abstract class AbstractConnection : Validatable() {
	final override fun checkIsLoaded(): Boolean = a.isLoaded && b.isLoaded

	override fun checkIsValid(): Boolean {
		return a.isValid && b.isValid && a.ionChunk.ionWorld == b.ionChunk.ionWorld
	}

	@JsonBackReference("Node")
	lateinit var a: AbstractNode

	lateinit var b: AbstractNode

	@Suppress("Unused")
	var key: String
		get() {
			val buffer = ByteBuffer.allocate(Long.SIZE_BYTES * 2)
			buffer.putLong(a.key)
			buffer.putLong(b.key)

			return Base64.getEncoder().encodeToString(buffer.array())
		}
		set(_) {}

	@Deprecated("Function should only be called by the node remover.")
	final override fun remove() {
		a.connections.remove(this)
		b.connections.remove(this)
	}

	fun other(node: AbstractNode): AbstractNode = if (a == node) b else a

	abstract class AbstractConnectionCompanion<T: AbstractConnection> {
		protected abstract fun construct(): T

		fun buildValidated(a: AbstractNode, b: AbstractNode): T? {
			val connection = construct()
			connection.a = a
			connection.b = b
			if (!connection.checkIsValid()) return null
			return connection
		}

		protected fun axisMatch(a: AbstractNode, b: AbstractNode): Boolean {
			return (if (a.x == b.x) 1 else 0) + (if (a.y == b.y) 1 else 0) + (if (a.z == b.z) 1 else 0) == 2
		}

		protected fun directContact(a: AbstractNode, b: AbstractNode): Boolean {
			return abs(a.x - b.x) + abs(a.y - b.y) + abs(a.z - b.z) == 1
		}
	}
}