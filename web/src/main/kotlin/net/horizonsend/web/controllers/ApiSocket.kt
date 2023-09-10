package net.horizonsend.web.controllers

import io.jooby.ServerSentMessage
import io.jooby.kt.ServerSentHandler
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.runBlocking
import net.horizonsend.ion.common.IonComponent
import net.horizonsend.ion.common.datasync.SurvivalEvent
import net.horizonsend.ion.common.utils.redisaction.RedisActions

object SurvivalEvents : IonComponent() {
	override fun onEnable() {
		RedisActions.register("survival-events", runSync = false) { e: SurvivalEvent ->
			runBlocking { EventBus.publish(e) }
		}
	}
}

suspend fun handleSse(e: ServerSentHandler) = with(e) {
	EventBus.subscribe<SurvivalEvent> {
		sse.send(it)
	}
}

object EventBus {
	private val _events = MutableSharedFlow<Any>()
	val events = _events.asSharedFlow()

	suspend fun publish(event: Any) {
		_events.emit(event)
	}

	suspend inline fun <reified T> subscribe(crossinline onEvent: suspend (T) -> Unit) {
		events.filterIsInstance<T>()
			.collectLatest { event ->
				onEvent(event)
			}
	}
}
