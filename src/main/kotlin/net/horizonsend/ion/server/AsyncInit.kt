package net.horizonsend.ion.server

import co.aikar.commands.PaperCommandManager
import net.horizonsend.ion.server.listeners.BlockFadeListener
import net.horizonsend.ion.server.listeners.BlockFormListener
import net.horizonsend.ion.server.listeners.PlayerDeathListener
import net.horizonsend.ion.server.listeners.PlayerFishListener
import net.horizonsend.ion.server.listeners.PlayerItemConsumeListener
import net.horizonsend.ion.server.listeners.PlayerJoinListener
import net.horizonsend.ion.server.listeners.PlayerQuitListener
import net.horizonsend.ion.server.listeners.PlayerTeleportListener
import net.horizonsend.ion.server.listeners.PotionSplashListener
import net.horizonsend.ion.server.listeners.PrepareAnvilListener
import net.horizonsend.ion.server.listeners.PrepareItemEnchantListener
import net.horizonsend.ion.server.ores.OreListener

class AsyncInit(private val plugin: Ion) : Thread() {
	override fun run() {
		arrayOf(
			BlockFadeListener(),
			BlockFormListener(),
			PlayerDeathListener(),
			PlayerFishListener(),
			PlayerItemConsumeListener(),
			PlayerJoinListener(),
			PlayerQuitListener(),
			PlayerTeleportListener(),
			PotionSplashListener(),
			PrepareAnvilListener(),
			PrepareItemEnchantListener(),
			OreListener(plugin)
		).forEach {
			plugin.server.pluginManager.registerEvents(it, plugin)
		}

		PaperCommandManager(plugin).apply {
			@Suppress("Deprecation", "RedundantSuppression")
			enableUnstableAPI("help")
		}
	}
}