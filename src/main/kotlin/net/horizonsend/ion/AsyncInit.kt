package net.horizonsend.ion

import co.aikar.commands.PaperCommandManager
import net.horizonsend.ion.features.ores.OreListener
import net.horizonsend.ion.miscellaneous.listeners.BlockFadeListener
import net.horizonsend.ion.miscellaneous.listeners.BlockFormListener
import net.horizonsend.ion.miscellaneous.listeners.PlayerDeathListener
import net.horizonsend.ion.miscellaneous.listeners.PlayerFishListener
import net.horizonsend.ion.miscellaneous.listeners.PlayerItemConsumeListener
import net.horizonsend.ion.miscellaneous.listeners.PlayerJoinListener
import net.horizonsend.ion.miscellaneous.listeners.PlayerQuitListener
import net.horizonsend.ion.miscellaneous.listeners.PlayerTeleportListener
import net.horizonsend.ion.miscellaneous.listeners.PotionSplashListener
import net.horizonsend.ion.miscellaneous.listeners.PrepareAnvilListener
import net.horizonsend.ion.miscellaneous.listeners.PrepareItemEnchantListener

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