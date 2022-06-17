package net.horizonsend.ion.server

import java.util.concurrent.CountDownLatch
import net.horizonsend.ion.server.listeners.BlockFadeListener
import net.horizonsend.ion.server.listeners.BlockFormListener
import net.horizonsend.ion.server.listeners.ChunkLoadListener
import net.horizonsend.ion.server.listeners.PlayerDeathListener
import net.horizonsend.ion.server.listeners.PlayerFishListener
import net.horizonsend.ion.server.listeners.PlayerItemConsumeListener
import net.horizonsend.ion.server.listeners.PlayerJoinListener
import net.horizonsend.ion.server.listeners.PlayerQuitListener
import net.horizonsend.ion.server.listeners.PlayerTeleportListener
import net.horizonsend.ion.server.listeners.PotionSplashListener
import net.horizonsend.ion.server.listeners.PrepareAnvilListener
import net.horizonsend.ion.server.listeners.PrepareItemEnchantListener

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