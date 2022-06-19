package net.horizonsend.ion.server

import java.util.concurrent.CountDownLatch
import net.horizonsend.ion.server.listeners.bukkit.BlockFadeListener
import net.horizonsend.ion.server.listeners.bukkit.BlockFormListener
import net.horizonsend.ion.server.listeners.bukkit.ChunkLoadListener
import net.horizonsend.ion.server.listeners.bukkit.PlayerDeathListener
import net.horizonsend.ion.server.listeners.bukkit.PlayerFishListener
import net.horizonsend.ion.server.listeners.bukkit.PlayerItemConsumeListener
import net.horizonsend.ion.server.listeners.bukkit.PlayerJoinListener
import net.horizonsend.ion.server.listeners.bukkit.PlayerQuitListener
import net.horizonsend.ion.server.listeners.bukkit.PlayerTeleportListener
import net.horizonsend.ion.server.listeners.bukkit.PotionSplashListener
import net.horizonsend.ion.server.listeners.bukkit.PrepareAnvilListener
import net.horizonsend.ion.server.listeners.bukkit.PrepareItemEnchantListener

class AsyncInit(private val plugin: IonServer) : Thread() {
	val loadLock = CountDownLatch(1)

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
			ChunkLoadListener(plugin)
		).forEach {
			plugin.server.pluginManager.registerEvents(it, plugin)
		}

		loadLock.countDown()
	}
}