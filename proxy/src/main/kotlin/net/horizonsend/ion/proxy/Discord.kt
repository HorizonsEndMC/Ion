package net.horizonsend.ion.proxy

import dev.minn.jda.ktx.jdabuilder.light
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity.playing
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.ChunkingFilter
import net.dv8tion.jda.api.utils.MemberCachePolicy
import net.dv8tion.jda.api.utils.cache.CacheFlag
import net.horizonsend.ion.proxy.commands.DiscordCommands
import java.util.concurrent.TimeUnit

val discord by lazy {
	runCatching {
		light(IonProxy.configuration.discordBotToken, enableCoroutines = true) {
			setEnabledIntents(GatewayIntent.GUILD_MEMBERS)
			setMemberCachePolicy(MemberCachePolicy.ALL)
			setChunkingFilter(ChunkingFilter.ALL)
			disableCache(CacheFlag.entries)
			setEnableShutdownHook(false)
		}
	}.getOrElse {
		IonProxy.logger.warn("discord error $it")
		null
	}
}

fun discord() {
	DiscordCommands.setup()
	IonProxy.proxy.scheduler.buildTask(IonProxy) {
		discord?.presence?.setPresence(OnlineStatus.ONLINE, playing("with ${IonProxy.proxy.playerCount} players!"))
	}.repeat(5, TimeUnit.SECONDS)
}
