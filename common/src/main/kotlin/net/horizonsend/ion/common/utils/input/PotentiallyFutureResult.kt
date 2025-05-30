package net.horizonsend.ion.common.utils.input

import net.kyori.adventure.audience.Audience
import java.util.function.Consumer

interface PotentiallyFutureResult {
	fun sendReason(audience: Audience)

	fun get(): InputResult

	fun withResult(consumer: Consumer<InputResult>)
}
