package net.horizonsend.ion.server.generation

import net.horizonsend.ion.server.generation.configuration.Palette
import org.apache.commons.lang.SerializationUtils
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataType
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.io.ObjectInputStream
import java.io.Serializable

data class Asteroid(
	val x: Int,
	val y: Int,
	val z: Int,
	val palette: Palette,
	val size: Double,
	val octaves: Int
) : Serializable

data class Asteroids(val asteroids: List<Asteroid>) : Serializable

class AsteroidsDataType : PersistentDataType<ByteArray, Asteroids> {
	override fun getPrimitiveType(): Class<ByteArray> {
		return ByteArray::class.java
	}

	override fun getComplexType(): Class<Asteroids> {
		return Asteroids::class.java
	}

	override fun fromPrimitive(primitive: ByteArray, context: PersistentDataAdapterContext): Asteroids {
		val inputStream: InputStream = ByteArrayInputStream(primitive)
		val objectInputStream = ObjectInputStream(inputStream)

		return objectInputStream.readObject() as Asteroids
	}

	override fun toPrimitive(complex: Asteroids, context: PersistentDataAdapterContext): ByteArray {
		return SerializationUtils.serialize(complex)
	}
}

data class PlacedOre(
	val material: String,
	val blobSize: Int,
	val x: Int,
	val y: Int,
	val z: Int
) : Serializable

data class PlacedOres(
	val ores: List<PlacedOre>
) : Serializable

class PlacedOresDataType() : PersistentDataType<ByteArray, PlacedOres> {
	override fun getPrimitiveType(): Class<ByteArray> {
		return ByteArray::class.java
	}

	override fun getComplexType(): Class<PlacedOres> {
		return PlacedOres::class.java
	}

	override fun fromPrimitive(primitive: ByteArray, context: PersistentDataAdapterContext): PlacedOres {
		val inputStream: InputStream = ByteArrayInputStream(primitive)
		val objectInputStream = ObjectInputStream(inputStream)

		return objectInputStream.readObject() as PlacedOres
	}

	override fun toPrimitive(complex: PlacedOres, context: PersistentDataAdapterContext): ByteArray {
		return SerializationUtils.serialize(complex)
	}
}
