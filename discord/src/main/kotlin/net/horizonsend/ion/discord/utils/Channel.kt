package net.horizonsend.ion.discord.utils

import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.horizonsend.ion.common.utils.discord.Channel
import net.horizonsend.ion.discord.IonDiscordBot.exit

fun Channel.getChannel(guild: Guild): TextChannel {
	return guild.getTextChannelById(id) ?: exit("Could not find channel $name", NullPointerException())
}
