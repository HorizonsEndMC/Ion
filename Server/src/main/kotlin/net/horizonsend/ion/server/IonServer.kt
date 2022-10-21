package net.horizonsend.ion.server

import co.aikar.commands.PaperCommandManager
import net.horizonsend.ion.common.database.closeDatabase
import net.horizonsend.ion.common.database.enums.Achievement
import net.horizonsend.ion.common.database.openDatabase
import net.starlegacy.legacyDisable
import net.starlegacy.legacyEnable
import org.bukkit.plugin.java.JavaPlugin
import java.util.concurrent.ForkJoinPool

@Suppress("Unused")
class IonServer : JavaPlugin() {
	init { plugin = this }

	companion object {
		@JvmStatic lateinit var plugin: IonServer private set
	}

	private val openDatabaseFuture = ForkJoinPool.commonPool().submit {
		openDatabase(dataFolder)
	}

	override fun onEnable() {
		openDatabaseFuture.join()

		val pluginManager = server.pluginManager
		val commandManager = PaperCommandManager(this)

		for (listener in listeners) pluginManager.registerEvents(listener, this) // Listeners

		commandManager.registerCommand(AchievementsCommand())
		commandManager.commandCompletions.registerStaticCompletion("achievements", Achievement.values().map { it.name })

		initializeCrafting()

		legacyEnable(commandManager)
	}

	override fun onDisable() {
		closeDatabase()

		legacyDisable()
	}
}