package net.horizonsend.ion.core.namereservations

import java.util.UUID
import kotlinx.serialization.Serializable

@Serializable
data class NameReservationData(
	val settlements: MutableMap<String, @Serializable(with = UUIDSerializer::class) UUID> = mutableMapOf(),
	val nations: MutableMap<String, @Serializable(with = UUIDSerializer::class) UUID> = mutableMapOf()
)