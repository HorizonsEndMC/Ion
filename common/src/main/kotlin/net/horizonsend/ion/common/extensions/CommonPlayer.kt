package net.horizonsend.ion.common.extensions

import net.horizonsend.ion.common.utils.text.miniMessage
import net.kyori.adventure.text.Component
import net.luckperms.api.model.user.User
import java.util.UUID

/** Represents all the common information between different implementations of Players */
interface CommonPlayer {
	val uniqueId: UUID

	val name: String

	fun getDisplayName(): Component

	fun getPrefix(): Component? = getMetaData().prefix?.let { miniMessage.deserialize(it) }

	fun getSuffix(): Component? = getMetaData().suffix?.let { miniMessage.deserialize(it) }

	fun getMetaData() = getUser().cachedData.metaData

	fun getUser(): User
}
