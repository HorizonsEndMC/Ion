package net.starlegacy.util.redisaction

import com.google.gson.GsonBuilder
import com.google.gson.InstanceCreator
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.google.gson.reflect.TypeToken
import net.horizonsend.ion.server.IonServer
import net.starlegacy.SETTINGS
import net.starlegacy.SLComponent
import net.starlegacy.database.Oid
import net.starlegacy.redisPool
import net.starlegacy.util.Tasks
import org.bson.types.ObjectId
import org.bukkit.event.EventHandler
import org.bukkit.event.server.PluginDisableEvent
import redis.clients.jedis.Jedis
import redis.clients.jedis.JedisPubSub
import java.lang.reflect.Type
import java.util.UUID
import java.util.concurrent.Executors

object RedisActions : SLComponent() {
	private val gson = GsonBuilder().registerTypeAdapter(
		Oid::class.java,
		object : JsonSerializer<Oid<*>>, JsonDeserializer<Oid<*>>, InstanceCreator<Oid<*>> {
			override fun serialize(src: Oid<*>, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
				return JsonPrimitive(src.toString())
			}

			@Throws(JsonParseException::class)
			override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Oid<*> {
				return Oid<Any>(json.asString)
			}

			override fun createInstance(type: Type): Oid<*> {
				return Oid<Any>(ObjectId())
			}
		}
	).create()

	inline fun <reified T, B> register(id: String, runSync: Boolean, noinline function: (T) -> B): RedisAction<T> {
		val action = object : RedisAction<T>(id, object : TypeToken<T>() {}.type, runSync) {
			override fun onReceive(data: T) {
				function.invoke(data)
			}
		}
		register(action)
		return action
	}

	private val map = mutableMapOf<String, RedisAction<*>>()

	private lateinit var channel: String

	private lateinit var pubSub: JedisPubSub
	private lateinit var subscriber: Jedis

	var enabled = false
	private var loaded = false

	override fun onEnable() {
		channel = SETTINGS.redis.channel

		pubSub = PubSubListener
		subscriber = redisPool.resource

		Thread({
			subscriber.subscribe(pubSub, channel)
		}, "StarLegacy Plugin Messaging").start()

		enabled = true

		Tasks.sync {
			loaded = true
		}
	}

	override fun onDisable() {
		enabled = false
		pubSub.unsubscribe() // stop listening for messages
		subscriber.close() // close the subscriber instance
		map.clear() // clear the plugin message map
	}

	fun <T : RedisAction<*>> register(message: T) {
		check(!map.containsKey(message.id)) { "Duplicate message ${message.id}" }
		map[message.id] = message
	}

	private val id = UUID.randomUUID()

	private val executor = Executors.newSingleThreadExecutor(Tasks.namedThreadFactory("redis-action-publishing"))

	internal fun <Data> publish(messageId: String, data: Data, type: Type) {
		val content = gson.toJson(data, type)
		val message = "$id:$messageId:$content"
		executor.execute {
			redisPool.resource.use { it.publish(channel, message) }
		}
	}

	@EventHandler
	private fun onPluginDisable(event: PluginDisableEvent) {
		map.values.removeIf { IonServer == event.plugin }
	}

	private object PubSubListener : JedisPubSub() {
		override fun onMessage(channel: String, message: String) {
			// to prevent weird things from happening, delay all update handling till initialization is complete
			// however, we still need to listen immediately so we don't miss any updates
			if (!loaded) {
				Tasks.sync {
					onMessage(channel, message)
				}
				return
			}

			enabled = true

			if (!enabled) {
				return
			}

			val split = message.split(":")

			if (split[0] == id.toString()) {
				return // ignore if it came from us
			}

			val messageId = split[1]
			val pluginMessage = map[messageId]

			if (pluginMessage == null) {
				log.warn("Unknown message $messageId. Full contents: $message")
				return
			}

			val content: String = message
				.removePrefix(split[0]).removePrefix(":")
				.removePrefix(split[1]).removePrefix(":")

			val data = try {
				gson.fromJson<Any>(content, pluginMessage.type)
			} catch (exception: Exception) {
				log.error("Error while reading redis message $message", exception)
				return
			}

			if (pluginMessage.runSync) {
				Tasks.sync {
					try {
						pluginMessage.castAndReceive(data)
					} catch (exception: Exception) {
						log.error("Error while executing redis action $message", exception)
					}
				}
			} else {
				try {
					pluginMessage.castAndReceive(data)
				} catch (exception: Exception) {
					log.error("Error while executing redis action $message", exception)
				}
			}
		}
	}

	override fun supportsVanilla(): Boolean {
		return true
	}
}
