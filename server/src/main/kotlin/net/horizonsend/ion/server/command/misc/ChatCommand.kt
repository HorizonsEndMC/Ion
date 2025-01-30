package net.horizonsend.ion.server.command.misc

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.common.utils.text.plainText
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.chat.ChannelSelections
import net.horizonsend.ion.server.features.chat.ChatChannel
import org.bukkit.entity.Player

object ChatCommand : SLCommand() {
    @CommandAlias("chat")
    @CommandCompletion("@chatChannel")
    fun onChat(sender: Player, channel: String) = asyncCommand(sender) {
        val chatChannel = ChatChannel.entries.firstOrNull { chatChannel -> chatChannel.displayName.plainText().equals(channel, true) }
        if (chatChannel == null) {
            sender.userError("No chat channel found with that name")
            return@asyncCommand
        }

        ChannelSelections.switchChannel(ChannelSelections[sender], chatChannel, sender)
    }
}
