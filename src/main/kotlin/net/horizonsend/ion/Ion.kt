package net.horizonsend.ion

import co.aikar.commands.PaperCommandManager
import net.horizonsend.ion.commands.Restart
import net.horizonsend.ion.commands.ShrugCommand
import net.horizonsend.ion.ores.OreListener
import org.bukkit.Material.GLOWSTONE_DUST
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
		server.scheduler.runTaskAsynchronously(this, Runnable {
			OreListener(this) // Super important and is needed immediately.
			MiscellaneousListeners(this)

			commandManager = PaperCommandManager(this)

			@Suppress("DEPRECATION")
			commandManager.enableUnstableAPI("help")

			ShrugCommand(this)
			Restart(this)
		})

		server.addRecipe(FurnaceRecipe(NamespacedKey(this, "glowstonerecipe"), ItemStack(GLOWSTONE_DUST), REDSTONE, 1f, 400))
	}
}