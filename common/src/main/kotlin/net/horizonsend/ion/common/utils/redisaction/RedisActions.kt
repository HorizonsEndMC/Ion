package net.horizonsend.ion.common.utils.redisaction

import com.google.gson.*
import com.google.gson.reflect.TypeToken
import net.horizonsend.ion.common.CommonConfig
import net.horizonsend.ion.common.IonComponent
import net.horizonsend.ion.common.database.DBManager.jedisPool
import net.horizonsend.ion.common.database.Oid
import org.bson.types.ObjectId
import redis.clients.jedis.Jedis
import redis.clients.jedis.JedisPubSub
import java.lang.reflect.Type
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory

object RedisActions : IonComponent() {
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
	override fun onEnable() {
		channel = CommonConfig.redis.channel

		pubSub = PubSubListener
		subscriber = jedisPool.resource

		Thread({
			subscriber.subscribe(pubSub, channel)
		}, "StarLegacy Plugin Messaging").start()

		enabled = true
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

	private val executor = Executors.newSingleThreadExecutor(object : ThreadFactory {
		private var counter: Int = 0

		override fun newThread(r: Runnable): Thread {
			return Thread(r, "redis-action-publisher-${counter++}")
		}
	})

	fun <Data> publish(messageId: String, data: Data, type: Type) {
		val content = gson.toJson(data, type)
		val message = "$id:$messageId:$content"
		executor.execute {
			jedisPool.resource.use { it.publish(channel, message) }
		}
	}

	private object PubSubListener : JedisPubSub() {
		override fun onMessage(channel: String, message: String) {
			// to prevent weird things from happening, delay all update handling till initialization is complete
			// however, we still need to listen immediately so we don't miss any updates
			if (!enabled) return

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

			try {
				pluginMessage.castAndReceive(data)
			} catch (exception: Exception) {
				log.error("Error while executing redis action $message", exception)
			}
		}
	}

	override fun vanillaOnly(): Boolean {
		return true
	}
}
