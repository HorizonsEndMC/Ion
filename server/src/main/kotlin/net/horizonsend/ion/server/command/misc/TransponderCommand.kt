package net.horizonsend.ion.server.command.misc

import co.aikar.commands.annotation.CommandAlias
import net.horizonsend.ion.common.database.schema.misc.PlayerSettings
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.cache.PlayerSettingsCache.getSettingOrThrow
import net.horizonsend.ion.server.features.cache.PlayerSettingsCache.setSetting
import net.horizonsend.ion.server.features.starship.active.ActiveStarshipMechanics
import org.bukkit.entity.Player

object TransponderCommand : SLCommand() {
	@CommandAlias("transponder")
	fun onExecute(sender: Player) {
		val enabled = !sender.getSettingOrThrow(PlayerSettings::dynmapTransponderEnabled)
		sender.setSetting(PlayerSettings::dynmapTransponderEnabled, enabled)
		ActiveStarshipMechanics.refreshDynmapVisibility(sender)

		if (enabled) {
			sender.success("Dynmap transponder enabled. You are now voluntarily visible on Dynmap.")
		} else {
			sender.success("Dynmap transponder disabled. You will still be visible when gameplay conditions require it.")
		}
	}
}
