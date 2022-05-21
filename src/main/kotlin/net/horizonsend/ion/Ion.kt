package net.horizonsend.ion

import co.aikar.commands.PaperCommandManager
import net.horizonsend.ion.features.ores.OreListener
import net.horizonsend.ion.miscellaneous.ShrugCommand
import net.horizonsend.ion.miscellaneous.listeners.BlockFadeListener
import net.horizonsend.ion.miscellaneous.listeners.BlockFormListener
import net.horizonsend.ion.miscellaneous.listeners.PlayerFishListener
import net.horizonsend.ion.miscellaneous.listeners.PlayerItemConsumeListener
import net.horizonsend.ion.miscellaneous.listeners.PlayerJoinListener
import net.horizonsend.ion.miscellaneous.listeners.PlayerQuitListener
import net.horizonsend.ion.miscellaneous.listeners.PlayerTeleportListener
import net.horizonsend.ion.miscellaneous.listeners.PotionSplashListener
import net.horizonsend.ion.miscellaneous.listeners.PrepareAnvilListener
import net.horizonsend.ion.miscellaneous.listeners.PrepareItemEnchantListener
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.FurnaceRecipe
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin

@Suppress("unused") // Plugin entrypoint
class Ion: JavaPlugin() {
	override fun onEnable() {
		server.scheduler.runTaskAsynchronously(this, Runnable {
			setOf(
				BlockFadeListener(),
				BlockFormListener(),
				PlayerFishListener(),
				PlayerItemConsumeListener(),
				PlayerJoinListener(),
				PlayerQuitListener(),
				PlayerTeleportListener(),
				PotionSplashListener(),
				PrepareAnvilListener(),
				PrepareItemEnchantListener(),
				OreListener(this)
			).forEach {
				server.pluginManager.registerEvents(it, this)
			}

			PaperCommandManager(this).apply {
				@Suppress("deprecation")
				enableUnstableAPI("help")

				registerCommand(ShrugCommand())
			}
		})

		server.addRecipe(FurnaceRecipe(NamespacedKey(this, "glowstonerecipe"), ItemStack(Material.GLOWSTONE_DUST), Material.REDSTONE, 1f, 400))
	}
}