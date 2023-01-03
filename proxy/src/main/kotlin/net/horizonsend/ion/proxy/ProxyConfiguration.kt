package net.horizonsend.ion.proxy

import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class ProxyConfiguration(
	val discordBotToken: String = "",
	val motdFirstLine: String = "",
	val linkBypassRole: Long = 0,
	val discordServer: Long = 0,
	val globalChannel: Long = 0,
	val linkedRole: Long = 0,
	val roleMap: Map<String, Long> = mapOf("" to 0),
	val voteSites: List<VoteSite> = listOf(
		VoteSite("TESTNET", "Test Server List", "https://Test.net/HorizonsEnd"),
		VoteSite("TESTNET2", "Test Server List2", "https://Test2.net/HorizonsEnd")
	)
) {
	@ConfigSerializable
	data class VoteSite(
		val serviceName: String,
		val displayName: String,
		val displayAddress: String
	)
}
