package net.horizonsend.ion

import co.aikar.commands.PaperCommandManager
import net.horizonsend.ion.commands.Restart
import net.horizonsend.ion.commands.ShrugCommand
import net.horizonsend.ion.ores.OreListener
import org.bukkit.Material.DIORITE
import org.bukkit.Material.GLOWSTONE_DUST
import org.bukkit.Material.QUARTZ
import org.bukkit.Material.REDSTONE
import org.bukkit.NamespacedKey
import org.bukkit.inventory.FurnaceRecipe
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin

@Suppress("unused") // Plugin entrypoint
class Ion: JavaPlugin() {
	lateinit var commandManager: PaperCommandManager
		private set

	override fun onEnable() {
		OreListener(this) // Super important and is needed immediately.
		MiscellaneousListeners(this)

		// Everything unimportant goes here.
		server.scheduler.runTaskAsynchronously(this, Runnable {
			commandManager = PaperCommandManager(this)

			@Suppress("DEPRECATION")
			commandManager.enableUnstableAPI("help")

			server.addRecipe(FurnaceRecipe(NamespacedKey(this, "quartzrecipe"), ItemStack(QUARTZ), DIORITE, 1f, 400))
			server.addRecipe(FurnaceRecipe(NamespacedKey(this, "glowstonerecipe"), ItemStack(GLOWSTONE_DUST), REDSTONE, 1f, 400))

			MobSpawning(this)
			ShrugCommand(this)
			Restart(this)
		})
	}
}