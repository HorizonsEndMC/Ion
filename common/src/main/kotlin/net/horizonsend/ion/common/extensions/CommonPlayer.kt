package net.horizonsend.ion.common.extensions

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.empty
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacyAmpersand
import net.luckperms.api.model.user.User
import java.util.UUID

/** Represents all the common information between different implementations of Players */
interface CommonPlayer {
	val uniqueId: UUID

	val name: String

	fun getDisplayName(): Component

	fun getPrefix(): Component = getMetaData().prefix?.let { legacyAmpersand().deserialize(it) } ?: empty()

	fun getSuffix(): Component = getMetaData().suffix?.let { legacyAmpersand().deserialize(it) } ?: empty()

	fun getMetaData() = getUser().cachedData.metaData

	fun getUser(): User
}
