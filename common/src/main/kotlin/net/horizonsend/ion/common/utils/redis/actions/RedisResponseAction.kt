package net.horizonsend.ion.common.utils.redis.actions

import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import net.horizonsend.ion.common.utils.redis.RedisActions
import java.lang.reflect.Type
import java.util.concurrent.CompletableFuture

/**
 * A redis action that can be used for two-way communication between servers.
 *
 * Invoking this class will request a response, and reply with a future that will be completed once a response is received
 **/
abstract class RedisResponseAction<RequestData, ResponseData>(
	id: String,
	type: Type,
	runSync: Boolean
) : RedisPubAction<RequestData>(id, type, runSync), RedisListener<RequestData> {
	private val mailBox = mutableMapOf<String, CompletableFuture<ResponseData>>()

	private val responseId = "${id}_reply"
	private val responseType = object : TypeToken<ResponseData>() {}.type

	final override fun onReceive(data: RequestData) {
		val response = createReply(data)

		RedisActions.publish(responseId, response, responseType)
	}

	fun castAndReceiveResponse(data: Response) {
		val request = mailBox[data.requestId] ?: return
		@Suppress("UNCHECKED_CAST") request.complete(data.response as ResponseData)
	}

	abstract fun createReply(data: RequestData): ResponseData

	fun call(data: RequestData): Deferred<ResponseData> {
		onReceive(data)

		RedisActions.publish(id, data, type)

		return CompletableDeferred()
	}

	suspend fun request(data: RequestData): CompletableFuture<ResponseData> {
		val id = publish(data)
		val future = CompletableFuture<ResponseData>()

		mailBox[id.toString()] = future
		return future
	}

	class Response(val requestId: String, val response: Any)
}
