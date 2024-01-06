package net.horizonsend.ion.proxy

import kotlinx.serialization.Serializable

@Serializable
data class ProxyConfiguration(
	val motdFirstLine: String = "",
)

