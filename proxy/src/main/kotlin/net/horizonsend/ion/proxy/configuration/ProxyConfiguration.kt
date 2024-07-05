package net.horizonsend.ion.proxy.configuration

import kotlinx.serialization.Serializable

@Serializable
data class ProxyConfiguration(
	val motdFirstLine: String = "",
)

