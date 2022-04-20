package net.horizonsend.ion

import co.aikar.commands.PaperCommandManager
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
	override fun onEnable() {
		val listenerCommands = setOf(
			Restart(this)
		)

		val commands = setOf(
			ShrugCommand()
		)

		val commandManager = PaperCommandManager(this)

		commands.forEach { commandManager.registerCommand(it) }
		listenerCommands.forEach { commandManager.registerCommand(it) }

		@Suppress("DEPRECATION")
		commandManager.enableUnstableAPI("help")

		server.pluginManager.registerEvents(MiscellaneousListeners(), this)
		server.pluginManager.registerEvents(MobSpawning(), this)
		server.pluginManager.registerEvents(OreListener(this), this)

		listenerCommands.forEach { server.pluginManager.registerEvents(it, this) }

		this.server.addRecipe(FurnaceRecipe(NamespacedKey(this, "quartzrecipe"), ItemStack(QUARTZ), DIORITE, 1f, 400))
		this.server.addRecipe(FurnaceRecipe(NamespacedKey(this, "glowstonerecipe"), ItemStack(GLOWSTONE_DUST), REDSTONE, 1f, 400))
	}
}