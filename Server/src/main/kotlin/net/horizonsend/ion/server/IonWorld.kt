package net.horizonsend.ion.server

import com.fasterxml.jackson.annotation.JacksonInject
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonManagedReference
import com.fasterxml.jackson.annotation.OptBoolean
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.InjectableValues
import com.fasterxml.jackson.databind.ObjectMapper
import net.horizonsend.ion.server.IonServer.Companion.Ion
import net.horizonsend.ion.server.networks.removalQueue
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.storage.LevelResource
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists

class IonWorld constructor(
	@JsonIgnore
	@JacksonInject("serverLevel", useInput = OptBoolean.FALSE)
	val serverLevel: ServerLevel
) {
	@JsonManagedReference("IonWorld")
	val chunkKeyToIonChunk: MutableMap<Long, IonChunk> = mutableMapOf()

	operator fun get(chunkKey: Long): IonChunk = chunkKeyToIonChunk.getOrPut(chunkKey) { IonChunk(this, chunkKey) }

	fun save() {
		val networkJsonFile = serverLevel.convertable.getLevelPath(LevelResource.ROOT).resolve("ionWorld.json")

		networkJsonFile.deleteIfExists()

		ObjectMapper()
			.setDefaultPropertyInclusion(JsonInclude.Include.NON_EMPTY)
			.writeValue(networkJsonFile.toFile(), this)

		Ion.slF4JLogger.info("Saved IonWorld for ${serverLevel.dimension().location()}")
	}

	fun tick() {
		for ((_, ionChunk) in chunkKeyToIonChunk) ionChunk.tickNodes()
		for ((_, ionChunk) in chunkKeyToIonChunk) ionChunk.saveNodes()
	}

	companion object {
		private val ionWorlds = mutableMapOf<ServerLevel, IonWorld>()

		operator fun get(serverLevel: ServerLevel): IonWorld = ionWorlds[serverLevel] ?:
			throw IllegalStateException("IonWorld for ${serverLevel.dimension().location()} was not registered when it should have been.")

		fun register(serverLevel: ServerLevel): IonWorld {
			if (ionWorlds.contains(serverLevel)) throw IllegalStateException(
				"Attempted to register server level which is already registered!"
			)

			val path = serverLevel.convertable.getLevelPath(LevelResource.ROOT).resolve("ionWorld.json")

			val ionWorld: IonWorld =
				if (!path.exists()) IonWorld(serverLevel)
				else ObjectMapper()
					.setInjectableValues(InjectableValues.Std().addValue("serverLevel", serverLevel))
					.readValue(path.toFile(), object : TypeReference<IonWorld>() {})

			Ion.slF4JLogger.info("Registered IonWorld for ${ionWorld.serverLevel.dimension().location()}")

			ionWorlds[serverLevel] = ionWorld
			return ionWorld
		}

		fun unregister(serverLevel: ServerLevel) {
			val ionWorld = ionWorlds.remove(serverLevel) ?:
				throw IllegalStateException("IonWorld for ${serverLevel.dimension().location()} was not registered when it should have been")

			ionWorld.save()

			Ion.slF4JLogger.info("Unregistered IonWorld for ${ionWorld.serverLevel.dimension().location()}")
		}

		fun unregisterAll() {
			for (serverLevel in ionWorlds.keys.toTypedArray()) unregister(serverLevel)
		}

		fun saveAll() {
			for ((_, ionWorld) in ionWorlds) ionWorld.save()
		}

		fun tick() {
			val tickStartTime = System.nanoTime().toDouble() // TODO: Debug

			// Try catch initially added for debugging purposes but was left in to ensure that one world failing to tick
			// won't stop other worlds from ticking.
			for ((_, networkWorld) in ionWorlds) try {
				networkWorld.tick()
			} catch (exception: Exception) {
				Ion.slF4JLogger.error("Exception while ticking IonWorld for ${networkWorld.serverLevel.dimension().location()}", exception)
			}

			while (removalQueue.isNotEmpty()) {
				val validatable = removalQueue.poll()

				@Suppress("Deprecation")
				validatable.remove()
			}

			val tickTime = (System.nanoTime() - tickStartTime) / 1_000_000 // TODO: Debug
			println("Power Tick Duration: %5.2fms".format(tickTime)) // TODO: Debug
		}
	}
}