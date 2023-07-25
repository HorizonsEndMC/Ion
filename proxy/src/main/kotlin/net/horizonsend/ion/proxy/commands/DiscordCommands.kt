package net.horizonsend.ion.proxy.commands

import dev.minn.jda.ktx.events.onCommand
import dev.minn.jda.ktx.interactions.commands.slash
import dev.minn.jda.ktx.interactions.commands.updateCommands
import dev.minn.jda.ktx.messages.Embed
import net.horizonsend.ion.proxy.IonProxy
import net.horizonsend.ion.proxy.discord
import java.util.*

object DiscordCommands {
	fun setup() {
		discord?.updateCommands {
			slash("info", "Information on the server")
			slash("playerlist", "List the players")
		}?.queue()

		discord?.onCommand("info") {
			it.replyEmbeds(
				Embed {
					title = "Here are a few links of potential use:"
					description =
						"""
							[Survival Web Map](https://survival.horizonsend.net)
							[Creative Web Map](https://creative.horizonsend.net)
							[Discord Server](https://discord.gg/RPvgQsGzKM)
							[Resource Pack](https://github.com/HorizonsEndMC/ResourcePack/releases/latest)
							[Wiki](https://wiki.horizonsend.net)
							[Server Rules](https://wiki.horizonsend.net/rules)
							[Voting Sites](https://wiki.horizonsend.net/en/vote)
							""".trimIndent()
				}
			).setEphemeral(true).queue()
		}

		discord?.onCommand("playerlist") {
			it.replyEmbeds(
				Embed {
					val servers = IonProxy.proxy.allServers.filterNot { it.playersConnected.isEmpty() }
					for (server in servers) {
						field {
							name =
								"${server.serverInfo.name.replaceFirstChar { it.titlecase(Locale.getDefault()) }}'s players"
							value = server.playersConnected.joinToString("\n") { it.username.replace("_", "\\_") }
						}
					}
				}
			).setEphemeral(true).queue()
		}
	}
}
