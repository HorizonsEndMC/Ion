package net.horizonsend.ion.common.utils

import net.horizonsend.ion.common.IonComponent
import net.horizonsend.ion.common.utils.redis.RedisAction
import net.horizonsend.ion.common.utils.redis.RedisActions
import net.horizonsend.ion.common.utils.redis.RedisActions.register

object UnusedMessaging : IonComponent() {
	fun registerUnusedListener(id: String): RedisAction<*> = RedisActions.createAction<Any, Unit>(id, false) {}.register()

	override fun onEnable() {
		registerUnusedListener("chat-global")
		registerUnusedListener("nations-chat-msg-settlement")
		registerUnusedListener("nations-chat-msg-nation")
		registerUnusedListener("nations-chat-msg-ally")
		registerUnusedListener("chat-admin")
		registerUnusedListener("chat-pumpkin")
		registerUnusedListener("chat-staff")
		registerUnusedListener("chat-mod")
		registerUnusedListener("chat-dev")
		registerUnusedListener("chat-contentdesign")
		registerUnusedListener("chat-vip")
	}
}
