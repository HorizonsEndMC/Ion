package net.horizonsend.ion.server.command.misc

import co.aikar.commands.annotation.CommandAlias
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.miscellaneous.utils.slPlayerId
import org.bukkit.entity.Player
import org.litote.kmongo.setValue

@CommandAlias("enableprotectionmessages")
object EnableProtectionMessagesCommand : SLCommand() {
    @Suppress("unused")
    fun defaultCase(
        sender: Player
    ) {
        val protectionMessagesEnabled = !PlayerCache[sender].protectionMessagesEnabled
        SLPlayer.updateById(sender.slPlayerId, setValue(SLPlayer::protectionMessagesEnabled, protectionMessagesEnabled))
        PlayerCache[sender].protectionMessagesEnabled = protectionMessagesEnabled
        sender.success("Changed protection message visibility to $protectionMessagesEnabled")
    }
}