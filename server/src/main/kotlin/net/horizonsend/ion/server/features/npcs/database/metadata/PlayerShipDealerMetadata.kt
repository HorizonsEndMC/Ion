package net.horizonsend.ion.server.features.npcs.database.metadata

import kotlinx.serialization.Serializable
import net.horizonsend.ion.common.redis.kserializers.ComponentKSerializer
import net.horizonsend.ion.common.redis.kserializers.UUIDSerializer
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_LIGHT_ORANGE
import net.kyori.adventure.text.Component
import java.util.UUID

@Serializable
data class PlayerShipDealerMetadata(
	val owner: @Serializable(with = UUIDSerializer::class) UUID?,
	val sellers: Set<@Serializable(with = UUIDSerializer::class) UUID> = setOf(),
	val name: @Serializable(with = ComponentKSerializer::class) Component = Component.text("Ship Dealer", HE_LIGHT_ORANGE)
) : UniversalNPCMetadata
