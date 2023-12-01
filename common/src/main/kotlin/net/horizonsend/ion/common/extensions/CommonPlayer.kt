package net.horizonsend.ion.common.extensions

import net.kyori.adventure.text.Component
import java.util.UUID

interface CommonPlayer {
	val uniqueId: UUID

	val name: String

	fun getDisplayName(): Component
}
