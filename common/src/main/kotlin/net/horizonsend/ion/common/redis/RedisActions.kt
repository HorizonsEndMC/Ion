package net.horizonsend.ion.common.redis

import com.google.gson.reflect.TypeToken
import net.horizonsend.ion.common.utils.configuration.CommonConfig
import net.horizonsend.ion.common.IonComponent
import net.horizonsend.ion.common.database.DBManager.jedisPool
import net.horizonsend.ion.common.ServerType
import net.horizonsend.ion.common.redis.serialization.RedisSerialization
import redis.clients.jedis.Jedis
import redis.clients.jedis.JedisPubSub
import java.lang.reflect.Type
import java.util.UUID
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory

object RedisActions : IonComponent() {
	var ignoreUnknownMessages: Boolean = false

	var enabled = false

	private lateinit var channel: String
	private lateinit var pubSub: JedisPubSub
	private lateinit var subscriber: Jedis
	private lateinit var thread: Thread

	override fun onEnable() {
		connect()

		thread = Thread({ subscriber.subscribe(pubSub, channel) }, "Ion Plugin Messaging")
		thread.start()

		enabled = true
	}

	private fun connect() {
		channel = CommonConfig.redis.channel

		pubSub = IonPubSubListener
		subscriber = jedisPool.resource

		log.info("Connected to Redis with channel $channel, pubSub: $pubSub, subscriber: $subscriber")
	}

	private val executor = Executors.newSingleThreadExecutor(object : ThreadFactory {
		private var counter: Int = 0

		override fun newThread(r: Runnable): Thread {
			return Thread(r, "redis-action-publisher-${counter++}")
		}
	})

	override fun onDisable() {
		enabled = false

		pubSub.unsubscribe() // stop listening for messages
		subscriber.close() // close the subscriber instance
		idActionMap.clear() // clear the plugin message map

		thread.interrupt() // Stop async services
		executor.shutdown() // Stop async services
	}

	// Actions
	private val idActionMap = mutableMapOf<String, RedisAction<*>>()

	inline fun <reified T, B> register(id: String, runSync: Boolean, noinline function: (T) -> B): RedisAction<T> {
		val action = object : RedisAction<T>(id, object : TypeToken<T>() {}.type, runSync) {
			override fun onReceive(data: T) {
				function.invoke(data)
			}
		}
		register(action)
		return action
	}

	fun <T : RedisAction<*>> register(message: T) {
		log.info("Registered Redis Action ${message.id}")
		check(!idActionMap.containsKey(message.id)) { "Duplicate message ${message.id}" }
		idActionMap[message.id] = message
	}

	private val serverId = UUID.randomUUID()

	val wrapperType: Type = object : TypeToken<RedisMessageWrapper>() {}.type

	fun <Data> publishMessage(
		actionId: String,
		content: Data,
		type: Type,
		targetServers: List<ServerType> = listOf(ServerType.SURVIVAL, ServerType.CREATIVE, ServerType.PROXY, ServerType.DISCORD_BOT)
	) : UUID {
		val messageUuid = UUID.randomUUID()
		val serializedContent = RedisSerialization.serialize(data = content, type = type)

		val wrapper = RedisMessageWrapper(
			actionId = actionId,
			messageId = messageUuid.toString(),
			serverId = serverId.toString(),
			message = serializedContent,
			targetServers = targetServers
		)

		val message = RedisSerialization.serialize(wrapper, wrapperType)

		executor.execute { jedisPool.resource.use { it.publish(channel, message) } }

		return messageUuid
	}

	private object IonPubSubListener : JedisPubSub() {
		override fun onMessage(channel: String, message: String) {
			try {
				// to prevent weird things from happening, delay all update handling till initialization is complete
				// however, we still need to listen immediately so we don't miss any updates
				if (!enabled) return

				val formatted = try {
					RedisSerialization.readTyped<RedisMessageWrapper>(message, wrapperType)
				} catch (e: Exception) {
					return log.warn("Could not deserialize redis message. Full contents: $message")
				}

				// Ignore messages not intended for this server
				if (!formatted.targetServers.contains(CommonConfig.common.serverType)) return

				// ignore if it came from the server it was sent from
				if (formatted.serverId == serverId.toString()) return

				val receiver = idActionMap[formatted.actionId]

				if (receiver == null) {
					if (!ignoreUnknownMessages) log.warn("Unknown redis action: ${formatted.actionId}, full contents: $formatted")

					return
				}

				val data = RedisSerialization.read(formatted.message, receiver.type)

				receiver.castAndReceive(data)
			} catch (e: Throwable) { log.warn("Exception in redis messaging: $e") }
		}
	}
}
