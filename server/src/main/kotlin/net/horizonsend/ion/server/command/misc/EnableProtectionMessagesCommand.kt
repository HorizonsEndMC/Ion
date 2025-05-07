package net.horizonsend.ion.server.command.misc

import co.aikar.commands.annotation.CommandAlias
import net.horizonsend.ion.common.database.schema.misc.PlayerSettings
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.cache.PlayerSettingsCache.getSetting
import net.horizonsend.ion.server.features.cache.PlayerSettingsCache.setSetting
import org.bukkit.entity.Player

@CommandAlias("enableprotectionmessages")
object EnableProtectionMessagesCommand : SLCommand() {
    fun defaultCase(
        sender: Player
    ) {
        val protectionMessagesEnabled = !sender.getSetting(PlayerSettings::protectionMessagesEnabled)
		sender.setSetting(PlayerSettings::protectionMessagesEnabled, protectionMessagesEnabled)

        sender.success("Changed protection message visibility to $protectionMessagesEnabled")
    }
}
