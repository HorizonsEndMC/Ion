package net.horizonsend.ion.server

import co.aikar.commands.PaperCommandManager
import net.horizonsend.ion.common.database.enums.Achievement
import net.horizonsend.ion.common.database.initializeDatabase
import net.horizonsend.ion.core.bridge
import net.horizonsend.ion.server.utilities.ionCore
import org.bukkit.plugin.java.JavaPlugin

// Special Exception Wildcard Imports
import net.horizonsend.ion.server.listeners.bukkit.*
import net.horizonsend.ion.server.listeners.ioncore.*

lateinit var plugin: IonServer private set

@Suppress("Unused")
class IonServer : JavaPlugin() {
	init { plugin = this }

	override fun onEnable() {
		initializeDatabase(dataFolder)

		bridge = BridgeImplementation()

		val pluginManager = server.pluginManager

		// Bukkit Listener Registration
		val listeners = arrayOf(
			BlockFadeListener(), BlockFormListener(), ChunkLoadListener(this), EnchantItemListener(),
			InventoryClickListener(), InventoryCloseListener(), InventoryDragListener(), InventoryMoveItemListener(),
			PlayerDeathListener(), PlayerFishListener(), PlayerItemConsumeListener(), PlayerJoinListener(this),
			PlayerLoginListener(), PlayerPickUpItemListener(), PlayerQuitListener(), PlayerResourcePackStatusListener(),
			PlayerTeleportListener(), PotionSplashListener(), PrepareItemCraftListener(), PrepareItemEnchantListener()
		)

		for (listener in listeners) {
			pluginManager.registerEvents(listener, this)
		}

		// IonCore Listener Registration
		ionCore {
			val ionCoreListeners = arrayOf(
				BuySpawnShuttleListener(), CaptureStationListener(), CompleteCargoRunListener(), CreateNationListener(),
				CreateNationOutpostListener(), CreateSettlementListener(), DetectShipListener(), EnterPlanetListener(),
				HyperspaceEnterListener(), LevelUpListener(), MultiblockDetectListener(), ShipKillListener(),
				StationSiegeBeginListener()
			)

			for (listener in ionCoreListeners) {
				pluginManager.registerEvents(listener, this)
			}
		}

		val commandManager = PaperCommandManager(this)

		commandManager.registerCommand(AchievementsCommand())

		commandManager.commandCompletions.registerStaticCompletion("achievements", Achievement.values().map { it.name })

		initializeCrafting()
	}
}