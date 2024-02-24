package net.horizonsend.ion.proxy.commands.bungee

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Default
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.proxy.features.PlayerShuffle
import net.horizonsend.ion.proxy.wrappers.WrappedPlayer
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.connection.ProxiedPlayer

@CommandAlias("tutorialproxy|tproxy")
class BungeeTutorialCommand : BaseCommand() {
	@Default
	fun onTest(sender: ProxiedPlayer) {
		val wrapped = WrappedPlayer(sender)

		wrapped.information("Attempting to test")

		val proxy = ProxyServer.getInstance()

		PlayerShuffle.send(
			sender,
			proxy.getServerInfo("Creative")
		) {
			//TODO
//			proxy.pluginManager.dispatchCommand(sender, "warp tutorial")
			wrapped.success("success")
		}
	}
}
